import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, Router, NavigationEnd } from '@angular/router';
import { StoreService } from './services/store.service';
import { ToastService } from './services/toast.service';
import { ApiService } from './services/api.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  templateUrl: './app.component.html'
})
export class AppComponent {
  streak = 0;

  constructor(
    public store: StoreService,
    public toastService: ToastService,
    private api: ApiService,
    private router: Router
  ) {
    this.router.events.subscribe(e => {
      if (e instanceof NavigationEnd && this.store.isLoggedIn) this.refreshStreak();
    });
  }

  initials = computed(() => (this.store.userName() || '??').slice(0, 2).toUpperCase());

  refreshStreak() {
    const uid = this.store.userId();
    if (!uid) return;
    this.api.dashboard(uid).subscribe({
      next: d => this.streak = d.streak,
      error: () => {}
    });
  }

  logout() {
    this.store.logout();
    this.router.navigate(['/login']);
  }

  isActive(path: string): boolean {
    return this.router.url.startsWith(path);
  }
}
