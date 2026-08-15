import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AppStateService } from '../../services/app-state.service';
import { ToastService } from '../../services/toast.service';
import { GradeResult, MockRoundDto } from '../../models';

@Component({
  selector: 'app-mock-round',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mock-round.component.html'
})
export class MockRoundComponent implements OnInit, OnDestroy {
  answer = '';
  submitting = false;
  reviewed = false;
  grade: GradeResult | null = null;
  done = false;
  timeLeft = 0;
  private timerId: any = null;
  private pendingNext: { roundIndex: number; round: MockRoundDto } | null = null;

  constructor(
    public state: AppStateService,
    private api: ApiService,
    private router: Router,
    private toast: ToastService
  ) {
    if (!this.state.mock) this.router.navigate(['/mock/start']);
  }

  get mk() { return this.state.mock!; }

  ngOnInit() {
    if (!this.state.mock) return;
    this.timeLeft = this.mk.timeLeft != null ? this.mk.timeLeft : (this.mk.round.timeBudgetSec || 0);
    this.mk.timeLeft = this.timeLeft;
    this.startTimer();
  }

  ngOnDestroy() { this.stopTimer(); }

  private startTimer() {
    this.stopTimer();
    this.timerId = setInterval(() => {
      this.timeLeft = Math.max(0, this.timeLeft - 1);
      this.mk.timeLeft = this.timeLeft;
      if (this.timeLeft <= 0) { this.stopTimer(); this.submit(true); }
    }, 1000);
  }
  private stopTimer() { if (this.timerId) { clearInterval(this.timerId); this.timerId = null; } }

  fmtTime(sec: number) { const m = Math.floor(sec / 60), s = sec % 60; return m + ':' + String(s).padStart(2, '0'); }

  submit(auto = false) {
    this.stopTimer();
    const answer = this.answer.trim();
    this.mk.lastAnswer = answer;
    this.submitting = true;
    this.api.mockAnswer(this.mk.sessionId, answer).subscribe({
      next: d => {
        this.submitting = false;
        this.grade = d.grade;
        this.done = d.done;
        this.reviewed = true;
        if (d.done) {
          this.state.mockReport = { overallScore: d.overallScore!, rounds: d.rounds! };
        } else {
          this.pendingNext = { roundIndex: d.roundIndex!, round: d.round! };
        }
      },
      error: e => { this.submitting = false; this.toast.show('Could not submit: ' + e.message, 'error'); }
    });
  }

  next() {
    if (this.done) { this.router.navigate(['/mock/report']); return; }
    if (!this.pendingNext) return;
    this.mk.roundIndex = this.pendingNext.roundIndex;
    this.mk.round = this.pendingNext.round;
    this.mk.timeLeft = null;
    this.pendingNext = null;
    this.reviewed = false;
    this.grade = null;
    this.answer = '';
    this.mk.lastAnswer = null;
    this.ngOnInit();
  }

  verdictLabel(v?: string) {
    const x = (v || 'error').toLowerCase();
    return x === 'correct' ? 'Correct' : x === 'partial' ? 'Partial' : x === 'incorrect' ? 'Not quite' : 'Error';
  }
  verdictClass(v?: string) {
    const x = (v || 'error').toLowerCase();
    return x === 'correct' ? 'pass' : x === 'partial' ? 'current' : 'locked';
  }
}
