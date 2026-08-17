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

  models: string[] = [];
  loadingModels = false;
  modelsError: string | null = null;

  constructor(private llm: LlmService, private local: LocalStoreService, private toast: ToastService) {
    const s = this.llm.settings();
    this.provider = s.provider;
    this.apiKey = s.apiKey;
    this.model = s.model;
  }

  private readonly allProviders: LlmProvider[] = ['groq', 'openai', 'anthropic'];

  onProviderChange() {
    // swap in the new provider's sane default only if the model field is empty or still holds
    // some OTHER provider's default (i.e. the user hasn't typed a custom model of their own)
    const isSomeOtherDefault = this.allProviders
      .filter(p => p !== this.provider)
      .some(p => this.model === this.llm.defaultModelFor(p));
    if (!this.model.trim() || isSomeOtherDefault) this.model = this.llm.defaultModelFor(this.provider);
    this.models = [];
    this.modelsError = null;
  }

  async refreshModels() {
    if (!this.apiKey.trim()) { this.toast.show('Enter an API key first.', 'info'); return; }
    this.loadingModels = true;
    this.modelsError = null;
    try {
      this.models = await this.llm.listModels({ provider: this.provider, apiKey: this.apiKey.trim() });
      if (this.models.length && !this.models.includes(this.model)) this.model = this.models[0];
      this.toast.show(`Found ${this.models.length} model(s) available to this key.`, 'info');
    } catch (e: any) {
      this.modelsError = e?.message || 'Could not fetch the model list.';
      this.toast.show('Could not fetch models: ' + this.modelsError, 'error');
    } finally {
      this.loadingModels = false;
    }
  }

  save() {
    this.llm.save({ provider: this.provider, apiKey: this.apiKey.trim(), model: this.model.trim() });
    this.toast.show('Settings saved — stored only in this browser.', 'info');
  }

  clearKey() {
    this.apiKey = '';
    this.models = [];
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
