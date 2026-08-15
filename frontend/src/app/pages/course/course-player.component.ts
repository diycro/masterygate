import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { StoreService } from '../../services/store.service';
import { AppStateService } from '../../services/app-state.service';
import { ToastService } from '../../services/toast.service';
import { CheckResult, CourseSegment, InterviewPlaybook, LessonView } from '../../models';
import { DiagramComponent } from '../../shared/diagram.component';

@Component({
  selector: 'app-course-player',
  standalone: true,
  imports: [CommonModule, DiagramComponent],
  templateUrl: './course-player.component.html'
})
export class CoursePlayerComponent implements OnInit, OnDestroy {
  loading = true;
  error: string | null = null;
  moduleId = '';
  lessons: LessonView[] = [];

  mode: 'lesson' | 'playbook' = 'lesson';
  activeLessonIdx = 0;
  segIdx = 0;
  playing = false;

  quizAnswers: (number | null)[] = [];
  quizResults: CheckResult[] | null = null;
  quizAllCorrect = false;
  grading = false;

  playbook: InterviewPlaybook | null = null;
  playbookLoading = false;

  private speechSupported = typeof window !== 'undefined' && 'speechSynthesis' in window;

  constructor(
    public state: AppStateService,
    private api: ApiService,
    private store: StoreService,
    private router: Router,
    private toast: ToastService
  ) {}

  ngOnInit() {
    if (!this.state.moduleId) { this.router.navigate(['/path']); return; }
    this.moduleId = this.state.moduleId;
    this.api.course(this.moduleId, this.store.userId()).subscribe({
      next: c => {
        if (!c.lessons.length) { this.router.navigate(['/module']); return; }
        this.lessons = c.lessons;
        const firstUnfinished = this.lessons.findIndex(l => !l.completed);
        this.activeLessonIdx = firstUnfinished >= 0 ? firstUnfinished : 0;
        this.segIdx = Math.min(this.lessons[this.activeLessonIdx].lastSegmentIndex, this.maxSegIdx());
        this.loading = false;
      },
      error: e => { this.error = e.message; this.loading = false; }
    });
  }

  ngOnDestroy() { this.cancelSpeech(); }

  get activeLesson(): LessonView | null { return this.lessons[this.activeLessonIdx] || null; }
  get segments(): CourseSegment[] { return this.activeLesson?.segments || []; }
  get hasQuiz(): boolean { return !!this.activeLesson && this.activeLesson.checks.length > 0; }
  get onQuiz(): boolean { return this.hasQuiz && this.segIdx >= this.segments.length; }
  get currentSegment(): CourseSegment | null { return this.onQuiz ? null : this.segments[this.segIdx] || null; }
  get totalSteps(): number { return this.segments.length + (this.hasQuiz ? 1 : 0); }
  get isLastLesson(): boolean { return this.activeLessonIdx === this.lessons.length - 1; }

  private maxSegIdx(): number { return Math.max(0, this.totalSteps - 1); }

  selectLesson(i: number) {
    if (i === this.activeLessonIdx && this.mode === 'lesson') return;
    this.cancelSpeech();
    this.mode = 'lesson';
    this.activeLessonIdx = i;
    this.segIdx = Math.min(this.lessons[i].lastSegmentIndex, this.maxSegIdx());
    this.resetQuizState();
  }

  openPlaybook() {
    this.cancelSpeech();
    this.mode = 'playbook';
    if (this.playbook || this.playbookLoading) return;
    const topicId = this.state.path?.topic;
    if (!topicId) return;
    this.playbookLoading = true;
    this.api.playbook(topicId).subscribe({
      next: p => { this.playbook = p; this.playbookLoading = false; },
      error: () => { this.playbookLoading = false; }
    });
  }

  backToLessons() { this.mode = 'lesson'; }

  private resetQuizState() {
    const n = this.activeLesson?.checks.length || 0;
    this.quizAnswers = new Array(n).fill(null);
    this.quizResults = null;
    this.quizAllCorrect = false;
  }

  prev() {
    if (this.segIdx <= 0) return;
    this.cancelSpeech();
    this.segIdx--;
    this.quizResults = null;
    this.savePosition();
  }

  next() {
    if (this.segIdx >= this.maxSegIdx()) return;
    this.cancelSpeech();
    this.segIdx++;
    if (this.onQuiz) this.resetQuizState();
    this.savePosition();
    if (this.playing && !this.onQuiz) this.speakCurrent();
    else this.playing = false;
  }

  jumpToSegment(i: number) {
    if (i === this.segIdx) return;
    this.cancelSpeech();
    this.segIdx = i;
    this.playing = false;
    this.savePosition();
  }

  private savePosition() {
    const uid = this.store.userId();
    if (!uid || !this.activeLesson) return;
    this.api.saveCoursePosition(Number(uid), this.moduleId, this.activeLesson.id, this.segIdx).subscribe({ error: () => {} });
  }

  togglePlay() {
    if (!this.speechSupported) {
      this.toast.show('Narration needs a browser with speech support (try Chrome).', 'info');
      return;
    }
    if (this.playing) {
      this.playing = false;
      this.cancelSpeech();
      return;
    }
    this.playing = true;
    this.speakCurrent();
  }

  private speakCurrent() {
    if (!this.speechSupported || this.onQuiz || !this.currentSegment) return;
    window.speechSynthesis.cancel();
    const utter = new SpeechSynthesisUtterance(this.currentSegment.narration);
    utter.rate = 1.0;
    utter.onend = () => {
      if (!this.playing) return;
      if (this.segIdx < this.maxSegIdx()) this.next();
      else this.playing = false;
    };
    utter.onerror = () => { this.playing = false; };
    window.speechSynthesis.speak(utter);
  }

  private cancelSpeech() {
    if (this.speechSupported) window.speechSynthesis.cancel();
  }

  pick(checkIdx: number, optIdx: number) {
    if (this.quizResults) this.quizResults = null;
    this.quizAnswers[checkIdx] = optIdx;
  }

  optClass(checkIdx: number, optIdx: number): string {
    if (this.quizResults) {
      const r = this.quizResults[checkIdx];
      if (optIdx === r.correctIndex) return 'correct';
      if (optIdx === this.quizAnswers[checkIdx] && optIdx !== r.correctIndex) return 'wrong';
      return '';
    }
    return this.quizAnswers[checkIdx] === optIdx ? 'picked' : '';
  }

  allAnswered(): boolean { return this.quizAnswers.every(a => a !== null); }

  submitQuiz() {
    if (!this.activeLesson || !this.allAnswered()) return;
    this.grading = true;
    const uid = this.store.userId();
    this.api.gradeChecks(uid ? Number(uid) : null, this.moduleId, this.activeLesson.id, this.quizAnswers as number[]).subscribe({
      next: res => {
        this.grading = false;
        this.quizResults = res.results;
        this.quizAllCorrect = res.allCorrect;
        if (res.lessonCompleted) {
          this.lessons[this.activeLessonIdx] = { ...this.activeLesson!, completed: true };
        }
      },
      error: e => { this.grading = false; this.toast.show('Could not grade the check: ' + e.message, 'error'); }
    });
  }

  continueNext() {
    if (this.isLastLesson) { this.router.navigate(['/module']); return; }
    this.selectLesson(this.activeLessonIdx + 1);
  }

  backToModule() { this.cancelSpeech(); this.router.navigate(['/module']); }
}
