import { Component, ElementRef, Input, OnChanges, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { CodeProblem, RunResult } from '../../models';

@Component({
  selector: 'app-code-practice',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './code-practice.component.html'
})
export class CodePracticeComponent implements OnChanges {
  @Input() moduleId!: string;
  @ViewChild('codebox') codebox?: ElementRef<HTMLTextAreaElement>;

  problem: CodeProblem | null = null;
  code = '';
  running = false;
  result: RunResult | null = null;
  requestError: string | null = null;

  constructor(private api: ApiService) {}

  ngOnChanges() {
    if (!this.moduleId) return;
    this.problem = null;
    this.result = null;
    this.requestError = null;
    this.api.codeProblem(this.moduleId).subscribe({
      next: p => { if (p.available) { this.problem = p; this.code = p.starterCode || ''; } },
      error: () => { this.problem = null; }
    });
  }

  onTab(e: Event) {
    const box = this.codebox?.nativeElement;
    if (!box) return;
    e.preventDefault();
    const s = box.selectionStart ?? 0;
    const en = box.selectionEnd ?? 0;
    this.code = this.code.slice(0, s) + '    ' + this.code.slice(en);
    setTimeout(() => { box.selectionStart = box.selectionEnd = s + 4; });
  }

  run() {
    this.running = true;
    this.result = null;
    this.requestError = null;
    this.api.runCode(this.moduleId, this.code).subscribe({
      next: r => { this.result = r; this.running = false; },
      error: e => { this.requestError = e.message; this.running = false; }
    });
  }

  get allPass() { return !!this.result && this.result.passCount === this.result.totalCount; }
}
