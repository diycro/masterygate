import { Injectable } from '@angular/core';
import { InterviewQA, MockReportRound, MockRoundDto, PathResponse, ProgressMap, Topic } from '../models';

export interface MockState {
  sessionId: string; roundIndex: number; totalRounds: number; round: MockRoundDto; focusTopicId: string;
  timeLeft: number | null; timerId: any; lastAnswer: string | null;
}
export interface GateState {
  moduleId: string; sessionId: string; total: number; index: number; question: string;
}
export interface MockReportState { overallScore: number; rounds: MockReportRound[]; }

/** Transient, in-memory cross-page state — mirrors the vanilla app's global `state` object. */
@Injectable({ providedIn: 'root' })
export class AppStateService {
  topics: Topic[] = [];
  path: PathResponse | null = null;
  progress: ProgressMap = { passed: {}, scores: {} };
  moduleId: string | null = null;
  gate: GateState | null = null;
  mock: MockState | null = null;
  mockReport: MockReportState | null = null;
  qaItems: InterviewQA[] = [];
  pickedMockFocus = 'genai';
}
