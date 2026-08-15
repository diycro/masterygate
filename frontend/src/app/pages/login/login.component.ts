import { Component, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { StoreService } from '../../services/store.service';
import { ToastService } from '../../services/toast.service';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent implements AfterViewInit {
  name = '';
  @ViewChild('nameInput') nameInput!: ElementRef<HTMLInputElement>;

  constructor(private store: StoreService, private toast: ToastService, private api: ApiService, private router: Router) {
    this.name = this.store.userName() || '';
  }

  ngAfterViewInit() { this.nameInput.nativeElement.focus(); }

  submit() {
    const n = this.name.trim();
    if (!n) { this.nameInput.nativeElement.focus(); return; }
    this.api.login(n).subscribe({
      next: u => { this.store.setUser(u.name, u.userId); this.router.navigate(['/dashboard']); },
      error: e => this.toast.show(`Login failed — is the app running? (${e.message})`, 'error')
    });
  }

  onKeydown(e: KeyboardEvent) { if (e.key === 'Enter') this.submit(); }
}
