import { Injectable } from '@angular/core';

interface TopicProgress { passed: Record<string, boolean>; scores: Record<string, number>; updatedAt: Record<string, string> }
interface CourseLessonProgress { completed: boolean; lastSegmentIndex: number }
export interface MockHistoryEntry {
  focusTopicId: string; dsaScore: number; systemDesignScore: number; deepDiveScore: number;
  overallScore: number; completedAt: string;
}

const PREFIX = 'studio.';

/**
 * Replaces the server-side database entirely: every piece of "your progress" (gate passes/scores,
 * resource checkboxes, course-lesson completion/position, mock-interview history, daily activity for
 * the streak, and any LLM-generated extra Q&A) lives only in THIS browser's localStorage. Clearing
 * browser data clears progress — same trade-off as any purely local, no-account app.
 */
@Injectable({ providedIn: 'root' })
export class LocalStoreService {

  // ---------------- progress (gate pass/fail + score), per topic ----------------
  private progressKey(topic: string) { return `${PREFIX}progress.${topic}`; }

  getTopicProgress(topic: string): TopicProgress {
    return this.readJson(this.progressKey(topic), { passed: {}, scores: {}, updatedAt: {} });
  }

  recordPass(topic: string, moduleId: string, score: number) {
    const p = this.getTopicProgress(topic);
    p.passed[moduleId] = true;
    p.scores[moduleId] = score;
    p.updatedAt[moduleId] = new Date().toISOString();
    this.writeJson(this.progressKey(topic), p);
    this.recordActivityToday();
    this.recordTouched(topic, moduleId);
  }

  // ---------------- "most recently touched" module, across all topics (for dashboard resume) ----------------
  private touchedKey = `${PREFIX}lastTouched`;
  private recordTouched(topicId: string, moduleId: string) {
    this.writeJson(this.touchedKey, { topicId, moduleId, at: new Date().toISOString() });
  }
  getLastTouched(): { topicId: string; moduleId: string; at: string } | null {
    return this.readJson<any>(this.touchedKey, null);
  }

  // ---------------- resource checkboxes ----------------
  private resourceKey(moduleId: string) { return `${PREFIX}resource.${moduleId}`; }

  getResourceProgress(moduleId: string): Record<string, boolean> {
    return this.readJson(this.resourceKey(moduleId), {});
  }
  setResourceProgress(moduleId: string, url: string, done: boolean) {
    const m = this.getResourceProgress(moduleId);
    if (done) m[url] = true; else delete m[url];
    this.writeJson(this.resourceKey(moduleId), m);
  }

  // ---------------- course lesson completion/position ----------------
  private courseKey(moduleId: string) { return `${PREFIX}course.${moduleId}`; }

  getCourseProgress(moduleId: string): Record<string, CourseLessonProgress> {
    return this.readJson(this.courseKey(moduleId), {});
  }
  saveCoursePosition(moduleId: string, lessonId: string, segmentIndex: number) {
    const m = this.getCourseProgress(moduleId);
    const existing = m[lessonId] || { completed: false, lastSegmentIndex: 0 };
    m[lessonId] = { completed: existing.completed, lastSegmentIndex: segmentIndex };
    this.writeJson(this.courseKey(moduleId), m);
  }
  markLessonCompleted(moduleId: string, lessonId: string) {
    const m = this.getCourseProgress(moduleId);
    const existing = m[lessonId] || { completed: false, lastSegmentIndex: 0 };
    m[lessonId] = { completed: true, lastSegmentIndex: existing.lastSegmentIndex };
    this.writeJson(this.courseKey(moduleId), m);
  }
  isCourseComplete(moduleId: string, lessonIds: string[]): boolean {
    if (!lessonIds.length) return true;
    const m = this.getCourseProgress(moduleId);
    return lessonIds.every(id => m[id]?.completed);
  }

  // ---------------- LLM-generated extra Q&A (persisted so "generate more" survives a reload) ----------------
  private qaExtraKey(moduleId: string) { return `${PREFIX}qaExtra.${moduleId}`; }
  getExtraQa(moduleId: string): any[] { return this.readJson(this.qaExtraKey(moduleId), []); }
  addExtraQa(moduleId: string, items: any[]) {
    const existing = this.getExtraQa(moduleId);
    this.writeJson(this.qaExtraKey(moduleId), existing.concat(items));
  }

  // ---------------- mock interview history ----------------
  private mockHistoryKey = `${PREFIX}mockHistory`;
  getMockHistory(): MockHistoryEntry[] { return this.readJson(this.mockHistoryKey, []); }
  addMockResult(entry: MockHistoryEntry) {
    const h = this.getMockHistory();
    h.unshift(entry);
    this.writeJson(this.mockHistoryKey, h.slice(0, 50));
  }

  // ---------------- daily activity / streak ----------------
  private activityKey = `${PREFIX}activityDates`;
  recordActivityToday() {
    const today = new Date().toISOString().slice(0, 10);
    const days: string[] = this.readJson(this.activityKey, []);
    if (!days.includes(today)) { days.push(today); this.writeJson(this.activityKey, days); }
  }
  currentStreak(): number {
    const days: string[] = this.readJson(this.activityKey, []);
    const set = new Set(days);
    let cursor = new Date();
    const iso = (d: Date) => d.toISOString().slice(0, 10);
    if (!set.has(iso(cursor))) {
      cursor.setDate(cursor.getDate() - 1);
      if (!set.has(iso(cursor))) return 0;
    }
    let streak = 0;
    while (set.has(iso(cursor))) { streak++; cursor.setDate(cursor.getDate() - 1); }
    return streak;
  }

  // ---------------- reset (for a "clear my data" button, if ever wanted) ----------------
  clearAll() {
    const toRemove: string[] = [];
    for (let i = 0; i < localStorage.length; i++) {
      const k = localStorage.key(i);
      if (k && k.startsWith(PREFIX)) toRemove.push(k);
    }
    toRemove.forEach(k => localStorage.removeItem(k));
  }

  private readJson<T>(key: string, fallback: T): T {
    try {
      const raw = localStorage.getItem(key);
      return raw ? JSON.parse(raw) as T : fallback;
    } catch { return fallback; }
  }
  private writeJson(key: string, value: any) {
    localStorage.setItem(key, JSON.stringify(value));
  }
}
