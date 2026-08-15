import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AppStateService } from '../../services/app-state.service';

@Component({
  selector: 'app-mock-report',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mock-report.component.html'
})
export class MockReportComponent {
  constructor(public state: AppStateService, private router: Router) {
    if (!this.state.mockReport) this.router.navigate(['/mock/start']);
  }

  get r() { return this.state.mockReport!; }
  get good() { return this.r.overallScore >= 70; }

  again() { this.state.mock = null; this.state.mockReport = null; this.router.navigate(['/mock/start']); }
  home() { this.state.mock = null; this.state.mockReport = null; this.router.navigate(['/dashboard']); }
}
