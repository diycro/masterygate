import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { StoreService } from '../../services/store.service';
import { AppStateService } from '../../services/app-state.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-mock-start',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mock-start.component.html'
})
export class MockStartComponent {
  starting = false;

  constructor(
    public state: AppStateService,
    private api: ApiService,
    private store: StoreService,
    private router: Router,
    private toast: ToastService
  ) {}

  get focus() { return this.state.pickedMockFocus; }

  pick(f: string) { this.state.pickedMockFocus = f; }

  start() {
    this.starting = true;
    this.api.mockStart(this.state.pickedMockFocus, this.store.userId()).subscribe({
      next: d => {
        this.starting = false;
        this.state.mock = {
          sessionId: d.sessionId, roundIndex: d.roundIndex, totalRounds: d.totalRounds,
          round: d.round, focusTopicId: d.focusTopicId, timeLeft: null, timerId: null, lastAnswer: null
        };
        this.router.navigate(['/mock/round']);
      },
      error: e => { this.starting = false; this.toast.show('Could not start the mock interview: ' + e.message, 'error'); }
    });
  }
}
