import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { StoreService } from '../../services/store.service';
import { AppStateService } from '../../services/app-state.service';
import { DashboardResponse } from '../../models';
import { RingComponent } from '../../shared/ring.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RingComponent],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  loading = true;
  error: string | null = null;
  data: DashboardResponse | null = null;
  totalQ = 0;
  pct = 0;
  resumeTrackTitle = '';

  constructor(
    public store: StoreService,
    private api: ApiService,
    private state: AppStateService,
    private router: Router
  ) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.error = null;
    const uid = this.store.userId();
    if (!uid) return;
    this.api.dashboard(uid).subscribe({
      next: d => {
        this.data = d;
        this.totalQ = d.tracks.reduce((a, t) => a + (t.interviewQuestions || 0), 0);
        this.pct = d.totalModules ? Math.round((d.totalPassed / d.totalModules) * 100) : 0;
        if (d.resume) {
          const track = d.tracks.find(t => t.topicId === d.resume!.topicId);
          this.resumeTrackTitle = track ? track.title : d.resume.topicId;
        }
        this.loading = false;
      },
      error: e => { this.error = e.message; this.loading = false; }
    });
  }

  trackPct(passed: number, total: number) { return total ? Math.round((passed / total) * 100) : 0; }
  startedTrackCount(): number { return this.data ? this.data.tracks.filter(t => t.started).length : 0; }

  goTrack(topicId: string) { this.store.setTopic(topicId); this.router.navigate(['/path']); }

  resume() {
    if (!this.data?.resume?.nextModuleId) return;
    this.openModuleDirect(this.data.resume.topicId, this.data.resume.nextModuleId);
  }

  review(topicId: string, moduleId: string) { this.openModuleDirect(topicId, moduleId); }

  private openModuleDirect(topicId: string, moduleId: string) {
    this.store.setTopic(topicId);
    const uid = this.store.userId()!;
    this.api.path(topicId).subscribe(data => {
      this.state.path = data;
      this.api.progress(uid, topicId).subscribe({
        next: prog => { this.state.progress = prog; this.finishOpenModule(moduleId); },
        error: () => { this.state.progress = { passed: {}, scores: {} }; this.finishOpenModule(moduleId); }
      });
    });
  }

  private finishOpenModule(moduleId: string) {
    this.state.moduleId = moduleId;
    this.router.navigate(['/module']);
  }
}
