import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-ring',
  standalone: true,
  template: `<div class="ring"><svg width="56" height="56" viewBox="0 0 56 56">
    <circle cx="28" cy="28" r="22" fill="none" stroke="var(--surface-3)" stroke-width="5"/>
    <circle cx="28" cy="28" r="22" fill="none" stroke="var(--accent)" stroke-width="5" stroke-linecap="round"
      [attr.stroke-dasharray]="c" [attr.stroke-dashoffset]="offset"/>
  </svg><span class="pct">{{pct}}%</span></div>`
})
export class RingComponent {
  @Input() pct = 0;
  r = 22;
  c = 2 * Math.PI * this.r;
  get offset() { return this.c * (1 - this.pct / 100); }
}
