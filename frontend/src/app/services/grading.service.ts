import { Injectable } from '@angular/core';
import { LlmService } from './llm.service';
import { GradeResult } from '../models';

const GRADER_SYSTEM = `You are a strict but fair senior technical interviewer grading a candidate's answer to a
concept question. Judge whether the answer demonstrates REAL understanding of the key points.
Be strict: vague, buzzword-only, or partially-correct answers must NOT be graded 'correct'.
- verdict: exactly one of "correct", "partial", "incorrect".
- score: 0-100 (correct ~80-100, partial ~40-79, incorrect ~0-39).
- feedback: 1-2 sentences, specific — name what was right and what was wrong or missing.
- missing: the key points the candidate did not cover (empty list if none).`;

const GRADE_SCHEMA = {
  type: 'object',
  properties: {
    verdict: { type: 'string', enum: ['correct', 'partial', 'incorrect'] },
    score: { type: 'integer' },
    feedback: { type: 'string' },
    missing: { type: 'array', items: { type: 'string' } }
  },
  required: ['verdict', 'score', 'feedback', 'missing']
};

const QA_GEN_SYSTEM = `You write REAL, frequently-asked technical interview questions for the given module — the kind
that actually appear in industry interviews (as compiled by sites like InterviewBit, GeeksforGeeks,
Exponent, DataCamp). For EACH question, provide: a complete, correct ANSWER (2-4 sentences, as a
strong candidate would say it out loud), and a short EXPLANATION of why interviewers ask this /
what it signals. Match the module's level and topic exactly. Do not repeat any question in the
AVOID list (rephrasing an avoided question is also not allowed — pick a genuinely different one).`;

const QA_GEN_SCHEMA = {
  type: 'object',
  properties: {
    items: {
      type: 'array',
      items: {
        type: 'object',
        properties: { question: { type: 'string' }, answer: { type: 'string' }, explanation: { type: 'string' } },
        required: ['question', 'answer', 'explanation']
      }
    }
  },
  required: ['items']
};

const GATE_GEN_SYSTEM = `You write interview-style CONCEPTUAL questions that test whether a learner truly understands a
software/AI module — not trivia. Each question must be answerable in a few sentences and probe
the "why/how", matching the module's level. For EACH question also list the KEY POINTS a correct
answer must cover (these are used later to grade the learner strictly). Ground the questions in the
provided objectives. Do not ask about any external video.`;

const GATE_GEN_SCHEMA = {
  type: 'object',
  properties: {
    questions: {
      type: 'array',
      items: {
        type: 'object',
        properties: { question: { type: 'string' }, keyPoints: { type: 'array', items: { type: 'string' } } },
        required: ['question', 'keyPoints']
      }
    }
  },
  required: ['questions']
};

export interface GenQaItem { question: string; answer: string; explanation: string; }
export interface GenGateQuestion { question: string; keyPoints: string[]; }

/**
 * The client-side "LLM-as-judge" — a line-for-line port of the backend's AnswerGrader /
 * InterviewQAGenerator / QuestionGenerator prompts, now calling the learner's own API key via
 * LlmService instead of a server-managed model.
 */
@Injectable({ providedIn: 'root' })
export class GradingService {
  constructor(private llm: LlmService) {}

  async grade(questionText: string, keyPoints: string[], answer: string): Promise<GradeResult> {
    if (!this.llm.configured) {
      return { verdict: 'error', score: 0, feedback: 'No API key configured — add one in Settings so the grader can run.', missing: [] };
    }
    const user = `QUESTION: ${questionText}\n\nKEY POINTS a correct answer should cover: ${keyPoints.join(' | ')}\n\nCANDIDATE ANSWER: ${answer && answer.trim() ? answer : '(no answer given)'}`;
    try {
      return await this.llm.callStructured<GradeResult>(GRADER_SYSTEM, user, 'submit_grade', 'Submit the grading verdict, score, feedback and missing key points.', GRADE_SCHEMA);
    } catch (e: any) {
      return { verdict: 'error', score: 0, feedback: 'Grading failed: ' + e.message, missing: [] };
    }
  }

  /** Throws (with the real provider error message) rather than swallowing failures — callers decide how to surface it. */
  async generateMoreQa(moduleTitle: string, objectives: string[], avoidQuestions: string[], n: number): Promise<GenQaItem[]> {
    if (!this.llm.configured) throw new Error('No API key configured — add one in Settings.');
    const avoid = avoidQuestions.length ? avoidQuestions.join(' | ') : '(none yet)';
    const user = `MODULE: ${moduleTitle}\nOBJECTIVES: ${objectives.join('; ')}\nAVOID (already asked): ${avoid}\n\nGenerate exactly ${n} new interview questions with answers and explanations.`;
    try {
      const bank = await this.llm.callStructured<{ items: GenQaItem[] }>(QA_GEN_SYSTEM, user, 'submit_questions', 'Submit the generated interview questions.', QA_GEN_SCHEMA);
      return (bank.items || []).filter(it => it.question?.trim() && it.answer?.trim());
    } catch (e: any) {
      console.error('generateMoreQa failed:', e);
      throw e;
    }
  }

  /** Silently falls back to [] on failure — the caller (gate start) is designed to fall back to curated questions. */
  async generateGateQuestions(moduleTitle: string, objectives: string[], n: number): Promise<GenGateQuestion[]> {
    if (!this.llm.configured) return [];
    const user = `MODULE: ${moduleTitle}\nOBJECTIVES: ${objectives.join('; ')}\n\nGenerate exactly ${n} questions.`;
    try {
      const quiz = await this.llm.callStructured<{ questions: GenGateQuestion[] }>(GATE_GEN_SYSTEM, user, 'submit_quiz', 'Submit the generated quiz questions.', GATE_GEN_SCHEMA);
      return (quiz.questions || []).filter(q => q.question?.trim());
    } catch (e) {
      console.error('generateGateQuestions failed, falling back to curated questions:', e);
      return [];
    }
  }
}
