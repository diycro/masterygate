import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LlmProvider, LlmService } from '../../services/llm.service';
import { LocalStoreService } from '../../services/local-store.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html'
})
export class SettingsComponent {
  provider: LlmProvider;
  apiKey: string;
  model: string;
  showKey = false;
  confirmingReset = false;

  constructor(private llm: LlmService, private local: LocalStoreService, private toast: ToastService) {
    const s = this.llm.settings();
    this.provider = s.provider;
    this.apiKey = s.apiKey;
    this.model = s.model;
  }

  onProviderChange() {
    // swap in the new provider's sane default only if the model field still holds the OTHER provider's default
    const otherDefault = this.llm.defaultModelFor(this.provider === 'openai' ? 'anthropic' : 'openai');
    if (!this.model.trim() || this.model === otherDefault) this.model = this.llm.defaultModelFor(this.provider);
  }

  save() {
    this.llm.save({ provider: this.provider, apiKey: this.apiKey.trim(), model: this.model.trim() });
    this.toast.show('Settings saved — stored only in this browser.', 'info');
  }

  clearKey() {
    this.apiKey = '';
    this.llm.save({ provider: this.provider, apiKey: '', model: this.model.trim() });
    this.toast.show('API key removed.', 'info');
  }

  resetProgress() {
    if (!this.confirmingReset) { this.confirmingReset = true; return; }
    this.local.clearAll();
    this.confirmingReset = false;
    this.toast.show('All local progress cleared.', 'info');
  }
}
