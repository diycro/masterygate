import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { CourseSegment, InterviewPlaybook, Resource, Topic } from '../models';

export interface GateQuestion { id: string; text: string; keyPoints: string[]; }

export interface ModuleFull {
  id: string; topicId: string; order: number; title: string;
  objectives: string[]; interviewFocus: string[]; resources: Resource[];
  questionCount: number; questions: GateQuestion[];
}

export interface RawKnowledgeCheck { question: string; options: string[]; correctIndex: number; explanation: string; }
export interface RawCourseLesson {
  id: string; moduleId: string; order: number; title: string; subtitle: string; minutes: number;
  segments: CourseSegment[]; checks: RawKnowledgeCheck[];
}

export interface QaSeedItem { id: number; question: string; answer: string; explanation: string; frequency: string; source: string; }

export interface CodeProblemFull {
  id: string; moduleId: string; title: string; description: string; starterCode: string; harnessTemplate: string;
}

/**
 * Loads the app's entire curriculum (topics, modules, gate questions, courses, interview Q&A, code
 * problems) from static JSON shipped with the build — fetched once and cached in memory. This is the
 * ONLY "backend" the static build has: everything else (progress, LLM calls) happens in the browser.
 */
@Injectable({ providedIn: 'root' })
export class ContentService {
  private topics$: Promise<Topic[]> | null = null;
  private modules$: Promise<Record<string, ModuleFull[]>> | null = null;
  private qa$: Promise<Record<string, QaSeedItem[]>> | null = null;
  private courses$: Promise<{ lessonsByModule: Record<string, RawCourseLesson[]>; playbookByTopic: Record<string, InterviewPlaybook> }> | null = null;
  private codeProblems$: Promise<Record<string, CodeProblemFull>> | null = null;

  private moduleIndex: Map<string, ModuleFull> = new Map();
  private moduleIndexReady: Promise<void> | null = null;

  constructor(private http: HttpClient) {}

  private get<T>(path: string): Promise<T> {
    return firstValueFrom(this.http.get<T>(`data/${path}`));
  }

  topics(): Promise<Topic[]> {
    if (!this.topics$) this.topics$ = this.get<Topic[]>('topics.json');
    return this.topics$;
  }

  private modulesByTopic(): Promise<Record<string, ModuleFull[]>> {
    if (!this.modules$) this.modules$ = this.get<Record<string, ModuleFull[]>>('modules.json');
    return this.modules$;
  }

  async modulesFor(topicId: string): Promise<ModuleFull[]> {
    const all = await this.modulesByTopic();
    return all[topicId] || [];
  }

  private async ensureModuleIndex(): Promise<void> {
    if (this.moduleIndexReady) return this.moduleIndexReady;
    this.moduleIndexReady = this.modulesByTopic().then(all => {
      for (const mods of Object.values(all)) for (const m of mods) this.moduleIndex.set(m.id, m);
    });
    return this.moduleIndexReady;
  }

  async getModule(moduleId: string): Promise<ModuleFull | null> {
    await this.ensureModuleIndex();
    return this.moduleIndex.get(moduleId) || null;
  }

  async allTopicIds(): Promise<string[]> {
    return Object.keys(await this.modulesByTopic());
  }

  private qaByModule(): Promise<Record<string, QaSeedItem[]>> {
    if (!this.qa$) this.qa$ = this.get<Record<string, QaSeedItem[]>>('qa.json');
    return this.qa$;
  }

  async qaFor(moduleId: string): Promise<QaSeedItem[]> {
    const all = await this.qaByModule();
    return all[moduleId] || [];
  }

  private courses(): Promise<{ lessonsByModule: Record<string, RawCourseLesson[]>; playbookByTopic: Record<string, InterviewPlaybook> }> {
    if (!this.courses$) this.courses$ = this.get('courses.json');
    return this.courses$;
  }

  async lessonsFor(moduleId: string): Promise<RawCourseLesson[]> {
    const c = await this.courses();
    return c.lessonsByModule[moduleId] || [];
  }

  async playbook(topicId: string): Promise<InterviewPlaybook | null> {
    const c = await this.courses();
    return c.playbookByTopic[topicId] || null;
  }

  private codeProblemsByModule(): Promise<Record<string, CodeProblemFull>> {
    if (!this.codeProblems$) this.codeProblems$ = this.get<Record<string, CodeProblemFull>>('code-problems.json');
    return this.codeProblems$;
  }

  async codeProblem(moduleId: string): Promise<CodeProblemFull | null> {
    const all = await this.codeProblemsByModule();
    return all[moduleId] || null;
  }

  /** All gate/mock questions for every module in a topic, pooled together (used for mock-interview draws). */
  async questionPoolFor(topicId: string): Promise<GateQuestion[]> {
    const mods = await this.modulesFor(topicId);
    return mods.flatMap(m => m.questions);
  }
}
