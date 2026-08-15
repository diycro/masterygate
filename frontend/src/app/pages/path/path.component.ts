import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { StoreService } from '../../services/store.service';
import { AppStateService } from '../../services/app-state.service';
import { ModuleSummary, PathResponse } from '../../models';
import { RingComponent } from '../../shared/ring.component';

interface ModRow {
  m: ModuleSummary;
  state: 'pass' | 'current' | 'locked';
  clickable: boolean;
  nodeInner: string;
  prevId: string;
}

@Component({
  selector: 'app-path',
  standalone: true,
  imports: [CommonModule, RingComponent],
  templateUrl: './path.component.html'
})
export class PathComponent implements OnInit {
  loading = true;
  error: string | null = null;
  title = '';
  rows: ModRow[] = [];
  passedCount = 0;
  pct = 0;
  total = 0;

  constructor(
    private api: ApiService,
    private store: StoreService,
    public state: AppStateService,
    private router: Router
  ) {}

  ngOnInit() {
    const topic = this.store.topic();
    if (!topic) { this.router.navigate(['/topics']); return; }
    this.loading = true;
    this.api.path(topic).subscribe({
      next: data => {
        this.state.path = data;
        this.total = data.modules.length;
        const uid = this.store.userId()!;
        this.api.progress(uid, topic).subscribe({
          next: prog => { this.state.progress = prog; this.build(data, topic); },
          error: () => { this.state.progress = { passed: {}, scores: {} }; this.build(data, topic); }
        });
      },
      error: e => { this.error = e.message; this.loading = false; }
    });
  }

  private build(data: PathResponse, topic: string) {
    const prog = this.state.progress;
    const mods = data.modules;
    this.passedCount = mods.filter(m => prog.passed[m.id]).length;
    this.pct = mods.length ? Math.round((this.passedCount / mods.length) * 100) : 0;
    const tp = this.state.topics.find(x => x.id === topic);
    this.title = tp ? tp.title : topic;

    let currentAssigned = false;
    this.rows = mods.map((m, i) => {
      const passed = !!prog.passed[m.id];
      const prevPassed = i === 0 || prog.passed[mods[i - 1].id];
      let st: 'pass' | 'current' | 'locked' = 'locked';
      if (passed) st = 'pass';
      else if (prevPassed && !currentAssigned) { st = 'current'; currentAssigned = true; }
      const clickable = st !== 'locked';
      const nodeInner = st === 'pass' ? '✓' : st === 'locked' ? '🔒' : m.id;
      return { m, state: st, clickable, nodeInner, prevId: i > 0 ? mods[i - 1].id : '' };
    });
    this.loading = false;
  }

  score(id: string) { return this.state.progress.scores[id] || 0; }

  openModule(id: string) {
    this.state.moduleId = id;
    this.router.navigate(['/module']);
  }
}
