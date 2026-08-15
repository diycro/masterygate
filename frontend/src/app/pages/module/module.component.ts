import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { StoreService } from '../../services/store.service';
import { AppStateService } from '../../services/app-state.service';
import { ToastService } from '../../services/toast.service';
import { ModuleSummary, Resource } from '../../models';
import { QaBankComponent } from './qa-bank.component';
import { CodePracticeComponent } from './code-practice.component';

interface ResRow extends Resource { done: boolean; }
interface VideoItem { title: string; provider: string; url: string; meta: string; }

@Component({
  selector: 'app-module',
  standalone: true,
  imports: [CommonModule, FormsModule, QaBankComponent, CodePracticeComponent],
  templateUrl: './module.component.html'
})
export class ModuleComponent implements OnInit {
  m: ModuleSummary | null = null;
  passed = false;
  score = 0;
  freeR: ResRow[] = [];
  paidR: ResRow[] = [];
  videosLoading = true;
  videos: VideoItem[] = [];
  dynGate = false;
  gateStarting = false;

  constructor(
    public state: AppStateService,
    private api: ApiService,
    public store: StoreService,
    private router: Router,
    private toast: ToastService
  ) {}

  ngOnInit() {
    if (!this.state.path || !this.state.moduleId) { this.router.navigate(['/path']); return; }
    this.m = this.state.path.modules.find(x => x.id === this.state.moduleId) || null;
    if (!this.m) { this.router.navigate(['/path']); return; }
    const prog = this.state.progress;
    this.passed = !!prog.passed[this.m.id];
    this.score = prog.scores[this.m.id] || 0;

    const uid = this.store.userId()!;
    this.api.resourceProgress(uid, this.m.id).subscribe({
      next: doneMap => this.buildResources(doneMap),
      error: () => this.buildResources({})
    });
    this.loadVideos();
  }

  private buildResources(doneMap: Record<string, boolean>) {
    if (!this.m) return;
    this.freeR = this.m.resources.filter(r => r.free).map(r => ({ ...r, done: !!doneMap[r.url] }));
    this.paidR = this.m.resources.filter(r => !r.free).map(r => ({ ...r, done: !!doneMap[r.url] }));
  }

  toggleResource(row: ResRow) {
    if (!this.m) return;
    const next = !row.done;
    row.done = next;
    this.api.setResourceProgress(Number(this.store.userId()), this.m.id, row.url, next).subscribe({
      error: () => { row.done = !next; this.toast.show("Couldn't save that — try again.", 'error'); }
    });
  }

  loadVideos() {
    if (!this.m) return;
    this.videosLoading = true;
    this.api.videos(this.m.id).subscribe({
      next: vids => { this.videos = vids; this.videosLoading = false; },
      error: () => { this.videosLoading = false; }
    });
  }

  stripPlay(meta: string) { return (meta || '').replace(/^▶ ?/, ''); }

  get youtubeSearchUrl(): string {
    const q = encodeURIComponent(((this.m && this.m.title) || this.state.moduleId || '') + ' tutorial');
    return `https://www.youtube.com/results?search_query=${q}`;
  }

  back() { this.router.navigate(['/path']); }

  startGate() {
    if (!this.m) return;
    this.gateStarting = true;
    this.api.gateStart(this.m.id, this.dynGate, this.store.userId()).subscribe({
      next: d => {
        this.gateStarting = false;
        this.state.gate = { moduleId: this.m!.id, sessionId: d.sessionId, total: d.total, index: d.index, question: d.question };
        this.router.navigate(['/gate']);
      },
      error: e => { this.gateStarting = false; this.toast.show('Could not start the gate: ' + e.message, 'error'); }
    });
  }
}
