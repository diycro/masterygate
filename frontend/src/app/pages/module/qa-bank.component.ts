import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { ToastService } from '../../services/toast.service';
import { InterviewQA } from '../../models';

export interface AnswerPart { type: 'text' | 'code'; content: string; lang?: string; }

@Component({
  selector: 'app-qa-bank',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './qa-bank.component.html'
})
export class QaBankComponent implements OnChanges {
  @Input() moduleId!: string;
  items: InterviewQA[] = [];
  openMap: Record<number, boolean> = {};
  loading = true;
  error = false;
  generating = false;
  private partsCache = new Map<number, AnswerPart[]>();

  constructor(private api: ApiService, private toast: ToastService) {}

  ngOnChanges() {
    if (!this.moduleId) return;
    this.loading = true;
    this.error = false;
    this.items = [];
    this.openMap = {};
    this.partsCache.clear();
    this.api.interviewQuestions(this.moduleId).subscribe({
      next: items => { this.items = items; this.loading = false; },
      error: () => { this.error = true; this.loading = false; }
    });
  }

  toggle(id: number) { this.openMap[id] = !this.openMap[id]; }

  /** Splits an answer on ```lang\n...\n``` fences so code examples render in monospace. */
  answerParts(item: InterviewQA): AnswerPart[] {
    const cached = this.partsCache.get(item.id);
    if (cached) return cached;
    const text = item.answer || '';
    const parts: AnswerPart[] = [];
    const fence = /```(\w*)\n?([\s\S]*?)```/g;
    let last = 0;
    let m: RegExpExecArray | null;
    while ((m = fence.exec(text))) {
      if (m.index > last) parts.push({ type: 'text', content: text.slice(last, m.index).trim() });
      parts.push({ type: 'code', lang: m[1] || undefined, content: m[2].replace(/\n$/, '') });
      last = fence.lastIndex;
    }
    if (last < text.length) {
      const rest = text.slice(last).trim();
      if (rest) parts.push({ type: 'text', content: rest });
    }
    const result = parts.filter(p => p.content.length > 0);
    this.partsCache.set(item.id, result);
    return result;
  }

  generateMore() {
    this.generating = true;
    this.api.generateMoreQA(this.moduleId, 4).subscribe({
      next: d => {
        this.generating = false;
        if (d.added && d.added.length) this.items = this.items.concat(d.added);
        else if (d.message) this.toast.show(d.message, 'info');
      },
      error: e => { this.generating = false; this.toast.show('Could not generate more questions: ' + e.message, 'error'); }
    });
  }
}
