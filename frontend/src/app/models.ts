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
