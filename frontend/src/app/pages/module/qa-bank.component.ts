import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { ToastService } from '../../services/toast.service';
import { InterviewQA } from '../../models';

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

  constructor(private api: ApiService, private toast: ToastService) {}

  ngOnChanges() {
    if (!this.moduleId) return;
    this.loading = true;
    this.error = false;
    this.items = [];
    this.openMap = {};
    this.api.interviewQuestions(this.moduleId).subscribe({
      next: items => { this.items = items; this.loading = false; },
      error: () => { this.error = true; this.loading = false; }
    });
  }

  toggle(id: number) { this.openMap[id] = !this.openMap[id]; }

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
