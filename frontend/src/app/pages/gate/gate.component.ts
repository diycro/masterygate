import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { StoreService } from '../../services/store.service';
import { AppStateService } from '../../services/app-state.service';
import { GradeResult } from '../../models';

@Component({
  selector: 'app-gate',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gate.component.html'
})
export class GateComponent {
  answer = '';
  submitting = false;
  grade: GradeResult | null = null;
  done = false;
  answered = false;
  phase: 'question' | 'result' = 'question';
  passed = false;
  avgScore = 0;
  error: string | null = null;
  private pendingNext: { index: number; total: number; question: string } | null = null;

  constructor(
    public state: AppStateService,
    private api: ApiService,
    private store: StoreService,
    private router: Router
  ) {
    if (!this.state.gate) this.router.navigate(['/module']);
  }

  get g() { return this.state.gate!; }
  get pct() { return Math.round((this.g.index / this.g.total) * 100); }

  verdictLabel(v?: string) {
    const x = (v || 'error').toLowerCase();
    return x === 'correct' ? 'Correct' : x === 'partial' ? 'Partial' : x === 'incorrect' ? 'Not quite' : 'Error';
  }
  verdictClass(v?: string) {
    const x = (v || 'error').toLowerCase();
    return x === 'correct' ? 'pass' : x === 'partial' ? 'current' : 'locked';
  }

  back() { this.state.gate = null; this.router.navigate(['/module']); }

  submit() {
    const answer = this.answer.trim();
    if (!answer) return;
    this.submitting = true;
    this.error = null;
    this.api.gateAnswer(this.g.sessionId, answer).subscribe({
      next: d => {
        this.grade = d.grade;
        this.done = d.done;
        this.answered = true;
        this.submitting = false;
        if (d.done) { this.passed = !!d.passed; this.avgScore = d.avgScore || 0; }
        else { this.pendingNext = { index: d.index!, total: d.total!, question: d.question! }; }
      },
      error: e => { this.submitting = false; this.error = e.message; }
    });
  }

  next() {
    if (!this.pendingNext) return;
    this.g.index = this.pendingNext.index;
    this.g.total = this.pendingNext.total;
    this.g.question = this.pendingNext.question;
    this.pendingNext = null;
    this.answer = '';
    this.grade = null;
    this.answered = false;
  }

  seeResult() { this.phase = 'result'; }

  retake() {
    this.api.gateStart(this.g.moduleId, false, this.store.userId()).subscribe(d => {
      this.state.gate = { moduleId: this.g.moduleId, sessionId: d.sessionId, total: d.total, index: d.index, question: d.question };
      this.answer = '';
      this.grade = null;
      this.done = false;
      this.answered = false;
      this.phase = 'question';
    });
  }

  toPath() { this.router.navigate(['/path']); }
  toModule() { this.router.navigate(['/module']); }
}
