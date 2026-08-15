import { Injectable, signal } from '@angular/core';

/** Wraps the app's localStorage-persisted identity/session state as Angular signals. */
@Injectable({ providedIn: 'root' })
export class StoreService {
  userName = signal<string | null>(localStorage.getItem('studio.user'));
  userId = signal<string | null>(localStorage.getItem('studio.userId'));
  topic = signal<string | null>(localStorage.getItem('studio.topic'));

  get isLoggedIn(): boolean {
    return !!this.userName() && !!this.userId();
  }

  setUser(name: string, id: string | number) {
    localStorage.setItem('studio.user', name);
    localStorage.setItem('studio.userId', String(id));
    this.userName.set(name);
    this.userId.set(String(id));
  }

  setTopic(topic: string) {
    localStorage.setItem('studio.topic', topic);
    this.topic.set(topic);
  }

  clearTopic() {
    localStorage.removeItem('studio.topic');
    this.topic.set(null);
  }

  logout() {
    ['studio.user', 'studio.userId', 'studio.topic'].forEach(k => localStorage.removeItem(k));
    this.userName.set(null);
    this.userId.set(null);
    this.topic.set(null);
  }
}
