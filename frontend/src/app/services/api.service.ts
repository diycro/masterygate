import { Injectable } from '@angular/core';
import { from, Observable, of } from 'rxjs';
import {
  CodeProblem, CourseView, DashboardResponse, GateAnswerResponse, GateStart, GradeChecksResponse,
  InterviewPlaybook, InterviewQA, MockAnswerResponse, MockStartResponse, PathResponse, ProgressMap,
  RunResult, Topic
} from '../models';
import { ContentService, GateQuestion } from './content.service';
import { LocalStoreService } from './local-store.service';
import { GradingService } from './grading.service';
import { CodeExecService } from './code-exec.service';

interface GateSessionState {
  moduleId: string; topicId: string; questions: GateQuestion[]; index: number; scores: number[];
}
interface MockRoundState { type: string; label: string; question: GateQuestion; answer?: string; score?: number; feedback?: string; }
interface MockSessionState { focusTopicId: string; rounds: MockRoundState[]; index: number; }

/**
 * The app's ENTIRE data layer, client-side: curriculum content comes from ContentService (static
 * JSON), progress from LocalStoreService (this browser's localStorage), grading/generation from
 * GradingService (the learner's own LLM API key), and code execution from CodeExecService (the free
 * public Piston API). Every method here keeps the exact same name/signature/return shape the old
 * HttpClient-backed version had, so no page component needed to change — only what's behind the
 * facade changed. "userId" is now always the fixed local profile id; it's kept as a parameter purely
 * for signature compatibility with the components that still pass it around.
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private gateSessions = new Map<string, GateSessionState>();
  private mockSessions = new Map<string, MockSessionState>();

  constructor(
    private content: ContentService,
    private local: LocalStoreService,
    private grading: GradingService,
    private codeExec: CodeExecService
  ) {}

  // ---- auth (local profile only — no server) ----
  login(name: string): Observable<{ userId: number; name: string }> {
    return of({ userId: 1, name });
  }

  // ---- curriculum ----
  topics(): Observable<Topic[]> { return from(this.content.topics()); }

  path(topic: string): Observable<PathResponse> {
    return from(this.content.modulesFor(topic).then(mods => ({
      topic,
      modules: mods.map(m => ({
        id: m.id, order: m.order, title: m.title, objectives: m.objectives,
        interviewFocus: m.interviewFocus, questionCount: m.questionCount, resources: m.resources
      }))
    })));
  }

  videos(_moduleId: string): Observable<any[]> { return of([]); } // no YouTube API in the static build

  // ---- progress ----
  progress(_userId: string, topic: string): Observable<ProgressMap> {
    const p = this.local.getTopicProgress(topic);
    return of({ passed: p.passed, scores: p.scores });
  }
  resourceProgress(_userId: string, moduleId: string): Observable<Record<string, boolean>> {
    return of(this.local.getResourceProgress(moduleId));
  }
  setResourceProgress(_userId: number, moduleId: string, url: string, done: boolean): Observable<any> {
    this.local.setResourceProgress(moduleId, url, done);
    return of({ ok: true });
  }

  // ---- dashboard ----
  dashboard(_userId: string): Observable<DashboardResponse> {
    return from(this.buildDashboard());
  }

  private async buildDashboard(): Promise<DashboardResponse> {
    const topics = await this.content.topics();
    const tracks = [];
    let totalModules = 0, totalPassed = 0;
    const weakCandidates: { topicId: string; moduleId: string; score: number }[] = [];

    for (const t of topics) {
      const mods = await this.content.modulesFor(t.id);
      const prog = this.local.getTopicProgress(t.id);
      const passedIds = Object.keys(prog.passed).filter(id => prog.passed[id]);
      const scores = passedIds.map(id => prog.scores[id] || 0);
      const avg = scores.length ? Math.round(scores.reduce((a, b) => a + b, 0) / scores.length) : 0;
      let qaCount = 0;
      for (const m of mods) qaCount += (await this.content.qaFor(m.id)).length + this.local.getExtraQa(m.id).length;

      tracks.push({
        topicId: t.id, title: t.title, totalModules: mods.length, passedModules: passedIds.length,
        avgScore: avg, interviewQuestions: qaCount, started: passedIds.length > 0 || Object.keys(prog.updatedAt).length > 0
      });
      totalModules += mods.length;
      totalPassed += passedIds.length;

      for (const id of passedIds) if ((prog.scores[id] || 0) < 85) weakCandidates.push({ topicId: t.id, moduleId: id, score: prog.scores[id] || 0 });
    }

    let resume = null;
    const touched = this.local.getLastTouched();
    if (touched) {
      const mods = await this.content.modulesFor(touched.topicId);
      const idx = mods.findIndex(m => m.id === touched.moduleId);
      const next = idx >= 0 && idx + 1 < mods.length ? mods[idx + 1] : null;
      resume = { topicId: touched.topicId, lastModuleId: touched.moduleId, nextModuleId: next?.id || null, nextModuleTitle: next?.title || null };
    }

    weakCandidates.sort((a, b) => a.score - b.score);
    const weak = [];
    for (const w of weakCandidates.slice(0, 5)) {
      const mod = await this.content.getModule(w.moduleId);
      weak.push({ topicId: w.topicId, moduleId: w.moduleId, moduleTitle: mod?.title || w.moduleId, score: w.score });
    }

    return { tracks, totalModules, totalPassed, streak: this.local.currentStreak(), resume, weak };
  }

  // ---- gate ----
  gateStart(moduleId: string, dynamic: boolean, _userId: string | null): Observable<GateStart> {
    return from(this.startGate(moduleId, dynamic));
  }

  private async startGate(moduleId: string, dynamic: boolean): Promise<GateStart> {
    const mod = await this.content.getModule(moduleId);
    if (!mod) throw new Error('Unknown module: ' + moduleId);

    let questions = mod.questions;
    if (dynamic) {
      const n = Math.max(4, mod.questions.length);
      const generated = await this.grading.generateGateQuestions(mod.title, mod.objectives, n);
      if (generated.length) {
        questions = generated.map((g, i) => ({ id: 'gen' + (i + 1), text: g.question, keyPoints: g.keyPoints || [] }));
      }
    }

    const sessionId = crypto.randomUUID();
    this.gateSessions.set(sessionId, { moduleId, topicId: mod.topicId, questions, index: 0, scores: [] });
    return { sessionId, moduleId, total: questions.length, index: 0, question: questions[0].text };
  }

  gateAnswer(sessionId: string, answer: string): Observable<GateAnswerResponse> {
    return from(this.answerGate(sessionId, answer));
  }

  private async answerGate(sessionId: string, answer: string): Promise<GateAnswerResponse> {
    const s = this.gateSessions.get(sessionId);
    if (!s) throw new Error('Unknown or expired session');
    const q = s.questions[s.index];
    const grade = await this.grading.grade(q.text, q.keyPoints, answer);
    s.scores.push(grade.score);
    s.index++;

    const done = s.index >= s.questions.length;
    if (!done) {
      return { grade, done, index: s.index, total: s.questions.length, question: s.questions[s.index].text };
    }
    const avgScore = Math.round(s.scores.reduce((a, b) => a + b, 0) / s.scores.length);
    const passed = avgScore >= 70;
    if (passed) this.local.recordPass(s.topicId, s.moduleId, avgScore);
    return { grade, done, passed, avgScore };
  }

  // ---- interview prep ----
  interviewQuestions(moduleId: string): Observable<InterviewQA[]> {
    return from(this.loadQa(moduleId));
  }

  private async loadQa(moduleId: string): Promise<InterviewQA[]> {
    const seed = await this.content.qaFor(moduleId);
    const extra = this.local.getExtraQa(moduleId) as InterviewQA[];
    return [...seed, ...extra];
  }

  generateMoreQA(moduleId: string, count = 4): Observable<{ added: InterviewQA[]; message?: string }> {
    return from(this.doGenerateMoreQa(moduleId, count));
  }

  private async doGenerateMoreQa(moduleId: string, count: number): Promise<{ added: InterviewQA[]; message?: string }> {
    const mod = await this.content.getModule(moduleId);
    if (!mod) return { added: [], message: 'Unknown module.' };
    const existing = await this.loadQa(moduleId);
    let generated;
    try {
      generated = await this.grading.generateMoreQa(mod.title, mod.objectives, existing.map(q => q.question), count);
    } catch (e: any) {
      return { added: [], message: 'Could not generate more questions: ' + (e?.message || 'unknown error') };
    }
    if (!generated.length) {
      return { added: [], message: "The model didn't return any new questions — try again, or try a different model in Settings." };
    }
    const added: InterviewQA[] = generated.map((g, i) => ({
      id: Date.now() + i, question: g.question, answer: g.answer, explanation: g.explanation,
      frequency: 'generated', source: 'llm'
    }));
    this.local.addExtraQa(moduleId, added);
    return { added };
  }

  // ---- code practice ----
  codeProblem(moduleId: string): Observable<CodeProblem> {
    return from(this.content.codeProblem(moduleId).then(p => p
      ? { available: true, id: p.id, title: p.title, description: p.description, starterCode: p.starterCode }
      : { available: false }));
  }
  runCode(moduleId: string, code: string): Observable<RunResult> {
    return from(this.doRunCode(moduleId, code));
  }
  private async doRunCode(moduleId: string, code: string): Promise<RunResult> {
    const p = await this.content.codeProblem(moduleId);
    if (!p) return { compiled: false, compileError: 'No code problem for this module.', timedOut: false, passCount: 0, totalCount: 0, tests: [] };
    return this.codeExec.run(p.harnessTemplate, code);
  }

  // ---- mock interview ----
  mockStart(focusTopic: string, _userId: string | null): Observable<MockStartResponse> {
    return from(this.doMockStart(focusTopic));
  }

  private async doMockStart(focusTopic: string): Promise<MockStartResponse> {
    const topicIds = await this.content.allTopicIds();
    const focus = focusTopic && topicIds.includes(focusTopic) ? focusTopic : 'genai';
    const [dsaPool, sdPool, focusPool] = await Promise.all([
      this.content.questionPoolFor('dsa'), this.content.questionPoolFor('sysdesign'), this.content.questionPoolFor(focus)
    ]);
    const dsaQ = this.pickRandom(dsaPool), sdQ = this.pickRandom(sdPool), deepQ = this.pickRandom(focusPool);
    if (!dsaQ || !sdQ || !deepQ) throw new Error('Question bank unavailable — cannot start a mock interview.');

    const topics = await this.content.topics();
    const focusTitle = topics.find(t => t.id === focus)?.title || 'Deep Dive';

    const rounds: MockRoundState[] = [
      { type: 'DSA', label: 'Round 1 — Data Structures & Algorithms', question: dsaQ },
      { type: 'SYSTEM_DESIGN', label: 'Round 2 — System Design', question: sdQ },
      { type: 'DEEP_DIVE', label: 'Round 3 — ' + focusTitle, question: deepQ }
    ];
    const sessionId = crypto.randomUUID();
    this.mockSessions.set(sessionId, { focusTopicId: focus, rounds, index: 0 });
    const timeBudget: Record<string, number> = { DSA: 600, SYSTEM_DESIGN: 900, DEEP_DIVE: 600 };
    return {
      sessionId, totalRounds: rounds.length, roundIndex: 0, focusTopicId: focus,
      round: { type: rounds[0].type, label: rounds[0].label, question: rounds[0].question.text, timeBudgetSec: timeBudget[rounds[0].type] }
    };
  }

  private pickRandom<T>(pool: T[]): T | null { return pool.length ? pool[Math.floor(Math.random() * pool.length)] : null; }

  mockAnswer(sessionId: string, answer: string): Observable<MockAnswerResponse> {
    return from(this.doMockAnswer(sessionId, answer));
  }

  private async doMockAnswer(sessionId: string, answer: string): Promise<MockAnswerResponse> {
    const s = this.mockSessions.get(sessionId);
    if (!s) throw new Error('Unknown or expired mock interview session');
    const round = s.rounds[s.index];
    const grade = await this.grading.grade(round.question.text, round.question.keyPoints, answer);
    round.answer = answer; round.score = grade.score; round.feedback = grade.feedback;
    s.index++;

    const done = s.index >= s.rounds.length;
    const timeBudget: Record<string, number> = { DSA: 600, SYSTEM_DESIGN: 900, DEEP_DIVE: 600 };
    if (!done) {
      const next = s.rounds[s.index];
      return { grade, done, roundIndex: s.index, totalRounds: s.rounds.length, round: { type: next.type, label: next.label, question: next.question.text, timeBudgetSec: timeBudget[next.type] } };
    }
    const scored = s.rounds.filter(r => r.score !== undefined);
    const overallScore = Math.round(scored.reduce((a, r) => a + (r.score || 0), 0) / scored.length);
    const rounds = s.rounds.map(r => ({ type: r.type, label: r.label, question: r.question.text, answer: r.answer || '', score: r.score || 0, feedback: r.feedback || '' }));
    this.local.addMockResult({
      focusTopicId: s.focusTopicId,
      dsaScore: s.rounds.find(r => r.type === 'DSA')?.score || 0,
      systemDesignScore: s.rounds.find(r => r.type === 'SYSTEM_DESIGN')?.score || 0,
      deepDiveScore: s.rounds.find(r => r.type === 'DEEP_DIVE')?.score || 0,
      overallScore, completedAt: new Date().toISOString()
    });
    return { grade, done, overallScore, rounds };
  }

  mockHistory(_userId: string): Observable<any[]> { return of(this.local.getMockHistory()); }

  // ---- interactive course ----
  course(moduleId: string, _userId: string | null): Observable<CourseView> {
    return from(this.buildCourse(moduleId));
  }

  private async buildCourse(moduleId: string): Promise<CourseView> {
    const lessons = await this.content.lessonsFor(moduleId);
    const progress = this.local.getCourseProgress(moduleId);
    let allComplete = lessons.length > 0;
    const views = lessons.map(l => {
      const p = progress[l.id];
      const completed = !!p?.completed;
      if (!completed) allComplete = false;
      return {
        id: l.id, order: l.order, title: l.title, subtitle: l.subtitle, minutes: l.minutes,
        segments: l.segments, checks: l.checks.map(c => ({ question: c.question, options: c.options })),
        completed, lastSegmentIndex: p?.lastSegmentIndex || 0
      };
    });
    return { moduleId, lessons: views, courseComplete: allComplete };
  }

  saveCoursePosition(_userId: number, moduleId: string, lessonId: string, segmentIndex: number): Observable<void> {
    this.local.saveCoursePosition(moduleId, lessonId, segmentIndex);
    return of(void 0);
  }

  gradeChecks(_userId: number | null, moduleId: string, lessonId: string, answers: number[]): Observable<GradeChecksResponse> {
    return from(this.doGradeChecks(moduleId, lessonId, answers));
  }

  private async doGradeChecks(moduleId: string, lessonId: string, answers: number[]): Promise<GradeChecksResponse> {
    const lessons = await this.content.lessonsFor(moduleId);
    const lesson = lessons.find(l => l.id === lessonId);
    if (!lesson) throw new Error('Unknown lesson: ' + lessonId);

    let allCorrect = true;
    const results = lesson.checks.map((c, i) => {
      const given = i < answers.length ? answers[i] : null;
      const correct = given !== null && given === c.correctIndex;
      if (!correct) allCorrect = false;
      return { correct, correctIndex: c.correctIndex, explanation: c.explanation };
    });

    if (allCorrect) this.local.markLessonCompleted(moduleId, lessonId);
    return { results, allCorrect, lessonCompleted: allCorrect };
  }

  playbook(topicId: string): Observable<InterviewPlaybook> {
    return from(this.content.playbook(topicId).then(p => { if (!p) throw new Error('No playbook for this topic.'); return p; }));
  }
}
