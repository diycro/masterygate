import { Injectable, signal } from '@angular/core';

export interface Toast { id: number; message: string; kind: 'info' | 'error'; }

@Injectable({ providedIn: 'root' })
export class ToastService {
  toasts = signal<Toast[]>([]);
  private nextId = 1;

  show(message: string, kind: 'info' | 'error' = 'info') {
    const id = this.nextId++;
    this.toasts.update(list => [...list, { id, message, kind }]);
    setTimeout(() => this.dismiss(id), 6000);
  }

  dismiss(id: number) {
    this.toasts.update(list => list.filter(t => t.id !== id));
  }
}
