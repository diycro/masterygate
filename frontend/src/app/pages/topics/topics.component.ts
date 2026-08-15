import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { StoreService } from '../../services/store.service';
import { AppStateService } from '../../services/app-state.service';
import { Topic } from '../../models';

@Component({
  selector: 'app-topics',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './topics.component.html'
})
export class TopicsComponent implements OnInit {
  topics: Topic[] = [];
  loading = true;
  error: string | null = null;

  constructor(private api: ApiService, private store: StoreService, private state: AppStateService, private router: Router) {}

  ngOnInit() {
    this.api.topics().subscribe({
      next: t => { this.topics = t; this.state.topics = t; this.loading = false; },
      error: e => { this.error = e.message; this.loading = false; }
    });
  }

  pick(topicId: string) { this.store.setTopic(topicId); this.router.navigate(['/path']); }
}
