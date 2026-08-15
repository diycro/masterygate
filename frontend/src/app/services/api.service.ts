import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CodeProblem, DashboardResponse, GateAnswerResponse, GateStart, InterviewQA,
  MockAnswerResponse, MockStartResponse, PathResponse, ProgressMap, RunResult, Topic
} from '../models';

const API = '/api';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  // ---- auth ----
  login(name: string): Observable<{ userId: number; name: string }> {
    return this.http.post<{ userId: number; name: string }>(`${API}/auth/login`, { name });
  }

  // ---- curriculum ----
  topics(): Observable<Topic[]> { return this.http.get<Topic[]>(`${API}/curriculum/topics`); }
  path(topic: string): Observable<PathResponse> {
    return this.http.get<PathResponse>(`${API}/curriculum/path`, { params: { topic } });
  }
  videos(moduleId: string): Observable<any[]> {
    return this.http.get<any[]>(`${API}/curriculum/videos`, { params: { moduleId } });
  }

  // ---- progress ----
  progress(userId: string, topic: string): Observable<ProgressMap> {
    return this.http.get<ProgressMap>(`${API}/progress`, { params: { userId, topic } });
  }
  resourceProgress(userId: string, moduleId: string): Observable<Record<string, boolean>> {
    return this.http.get<Record<string, boolean>>(`${API}/resource-progress`, { params: { userId, moduleId } });
  }
  setResourceProgress(userId: number, moduleId: string, url: string, done: boolean): Observable<any> {
    return this.http.post(`${API}/resource-progress`, { userId, moduleId, url, done });
  }

  // ---- dashboard ----
  dashboard(userId: string): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(`${API}/dashboard`, { params: { userId } });
  }

  // ---- gate ----
  gateStart(moduleId: string, dynamic: boolean, userId: string | null): Observable<GateStart> {
    let params: any = { moduleId, dynamic: String(dynamic) };
    if (userId) params.userId = userId;
    return this.http.post<GateStart>(`${API}/gate/start`, null, { params });
  }
  gateAnswer(sessionId: string, answer: string): Observable<GateAnswerResponse> {
    return this.http.post<GateAnswerResponse>(`${API}/gate/answer`, { sessionId, answer });
  }

  // ---- interview prep ----
  interviewQuestions(moduleId: string): Observable<InterviewQA[]> {
    return this.http.get<InterviewQA[]>(`${API}/interview/questions`, { params: { moduleId } });
  }
  generateMoreQA(moduleId: string, count = 4): Observable<{ added: InterviewQA[]; message?: string }> {
    return this.http.post<{ added: InterviewQA[]; message?: string }>(
      `${API}/interview/generate`, null, { params: { moduleId, count: String(count) } });
  }

  // ---- code practice ----
  codeProblem(moduleId: string): Observable<CodeProblem> {
    return this.http.get<CodeProblem>(`${API}/code/problem`, { params: { moduleId } });
  }
  runCode(moduleId: string, code: string): Observable<RunResult> {
    return this.http.post<RunResult>(`${API}/code/run`, { moduleId, code });
  }

  // ---- mock interview ----
  mockStart(focusTopic: string, userId: string | null): Observable<MockStartResponse> {
    let params: any = { focusTopic };
    if (userId) params.userId = userId;
    return this.http.post<MockStartResponse>(`${API}/mock/start`, null, { params });
  }
  mockAnswer(sessionId: string, answer: string): Observable<MockAnswerResponse> {
    return this.http.post<MockAnswerResponse>(`${API}/mock/answer`, { sessionId, answer });
  }
  mockHistory(userId: string): Observable<any[]> {
    return this.http.get<any[]>(`${API}/mock/history`, { params: { userId } });
  }
}
