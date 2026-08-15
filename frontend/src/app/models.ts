export interface Topic { id: string; title: string; subtitle: string; moduleCount: number; }

export interface Resource { title: string; provider: string; url: string; meta: string; free: boolean; }

export interface ModuleSummary {
  id: string; order: number; title: string; objectives: string[]; interviewFocus: string[];
  questionCount: number; resources: Resource[];
}

export interface PathResponse { topic: string; modules: ModuleSummary[]; }

export interface ProgressMap { passed: Record<string, boolean>; scores: Record<string, number>; }

export interface DashboardTrack {
  topicId: string; title: string; totalModules: number; passedModules: number;
  avgScore: number; interviewQuestions: number; started: boolean;
}
export interface DashboardResume {
  topicId: string; lastModuleId: string; nextModuleId: string | null; nextModuleTitle: string | null;
}
export interface DashboardWeak { topicId: string; moduleId: string; moduleTitle: string; score: number; }
export interface DashboardResponse {
  tracks: DashboardTrack[]; totalModules: number; totalPassed: number; streak: number;
  resume: DashboardResume | null; weak: DashboardWeak[];
}

export interface GateStart { sessionId: string; moduleId: string; total: number; index: number; question: string; }
export interface GradeResult { verdict: string; score: number; feedback: string; missing: string[]; }
export interface GateAnswerResponse {
  grade: GradeResult; done: boolean; passed?: boolean; avgScore?: number;
  index?: number; total?: number; question?: string;
}

export interface InterviewQA {
  id: number; question: string; answer: string; explanation: string; frequency: string; source: string;
}

export interface CodeProblem { available: boolean; id?: string; title?: string; description?: string; starterCode?: string; }
export interface TestOutcome { index: number; pass: boolean; detail: string; }
export interface RunResult {
  compiled: boolean; compileError: string | null; timedOut: boolean;
  passCount: number; totalCount: number; tests: TestOutcome[]; rawOutput?: string;
}

export interface MockRoundDto { type: string; label: string; question: string; timeBudgetSec?: number; }
export interface MockStartResponse {
  sessionId: string; totalRounds: number; roundIndex: number; focusTopicId: string; round: MockRoundDto;
}
export interface MockAnswerResponse {
  grade: GradeResult; done: boolean; roundIndex?: number; totalRounds?: number; round?: MockRoundDto;
  overallScore?: number; rounds?: MockReportRound[];
}
export interface MockReportRound {
  type: string; label: string; question: string; answer: string; score: number; feedback: string;
}

// ---- interactive course ----
export interface DiagramNode { label: string; sub: string | null; }
export interface CompareColumn { heading: string; points: string[]; }
export interface Diagram {
  kind: 'flow' | 'cycle' | 'stack' | 'compare';
  caption: string | null;
  nodes: DiagramNode[] | null;
  columns: CompareColumn[] | null;
}
export type SegmentType = 'CONCEPT' | 'DIAGRAM' | 'CODE' | 'STORY' | 'CALLOUT';
export interface CourseSegment {
  id: string; type: SegmentType; title: string; narration: string;
  diagram: Diagram | null; code: string | null; codeLang: string | null;
}
export interface CheckView { question: string; options: string[]; }
export interface LessonView {
  id: string; order: number; title: string; subtitle: string; minutes: number;
  segments: CourseSegment[]; checks: CheckView[]; completed: boolean; lastSegmentIndex: number;
}
export interface CourseView { moduleId: string; lessons: LessonView[]; courseComplete: boolean; }

export interface CheckResult { correct: boolean; correctIndex: number; explanation: string; }
export interface GradeChecksResponse { results: CheckResult[]; allCorrect: boolean; lessonCompleted: boolean; }

export interface InterviewRound {
  name: string; duration: string; whatTheyTest: string; sampleQuestions: string[]; proTip: string;
}
export interface CompanyTrack { company: string; levelNote: string; rounds: InterviewRound[]; }
export interface InterviewPlaybook {
  topicId: string; title: string; intro: string; companies: CompanyTrack[];
  commonPitfalls: string[]; prepChecklist: string[];
}
