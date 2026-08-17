import { Injectable, signal } from '@angular/core';

export type LlmProvider = 'openai' | 'anthropic';

export interface LlmSettings {
  provider: LlmProvider;
  apiKey: string;
  model: string;
}

const DEFAULT_MODEL: Record<LlmProvider, string> = {
  openai: 'gpt-4o-mini',
  anthropic: 'claude-3-5-haiku-latest'
};

/**
 * Calls the learner's own OpenAI or Anthropic API key DIRECTLY from the browser — no backend involved.
 * The key is stored only in this browser's localStorage and is never sent anywhere except straight to
 * the chosen provider's API. Structured JSON output is obtained via forced tool-use/function-calling on
 * both providers, which is far more reliable than asking the model to "please reply with JSON."
 */
@Injectable({ providedIn: 'root' })
export class LlmService {
  private readonly KEY = 'studio.llm.settings';

  settings = signal<LlmSettings>(this.load());

  private load(): LlmSettings {
    try {
      const raw = localStorage.getItem(this.KEY);
      if (raw) return JSON.parse(raw);
    } catch { /* ignore corrupt storage */ }
    return { provider: 'openai', apiKey: '', model: DEFAULT_MODEL.openai };
  }

  save(next: LlmSettings) {
    localStorage.setItem(this.KEY, JSON.stringify(next));
    this.settings.set(next);
  }

  get configured(): boolean {
    return !!this.settings().apiKey.trim();
  }

  defaultModelFor(provider: LlmProvider) { return DEFAULT_MODEL[provider]; }

  /**
   * Calls the configured provider, forcing the model to return arguments matching `schema` via a
   * single forced tool call, and resolves with the parsed object. Throws with a readable message on
   * any failure (missing key, network error, malformed response) — callers should catch and show it.
   */
  async callStructured<T>(system: string, user: string, toolName: string, toolDescription: string, schema: any): Promise<T> {
    const s = this.settings();
    if (!s.apiKey.trim()) throw new Error('No API key configured — add one in Settings.');
    if (s.provider === 'openai') return this.callOpenAi<T>(s, system, user, toolName, toolDescription, schema);
    return this.callAnthropic<T>(s, system, user, toolName, toolDescription, schema);
  }

  private async callOpenAi<T>(s: LlmSettings, system: string, user: string, toolName: string, toolDescription: string, schema: any): Promise<T> {
    const res = await fetch('https://api.openai.com/v1/chat/completions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${s.apiKey}` },
      body: JSON.stringify({
        model: s.model || DEFAULT_MODEL.openai,
        messages: [{ role: 'system', content: system }, { role: 'user', content: user }],
        tools: [{ type: 'function', function: { name: toolName, description: toolDescription, parameters: schema } }],
        tool_choice: { type: 'function', function: { name: toolName } }
      })
    });
    if (!res.ok) throw new Error(await this.errText(res));
    const data = await res.json();
    const call = data?.choices?.[0]?.message?.tool_calls?.[0];
    if (!call) throw new Error('OpenAI did not return a structured result.');
    try { return JSON.parse(call.function.arguments) as T; }
    catch { throw new Error('Could not parse the model\'s response.'); }
  }

  private async callAnthropic<T>(s: LlmSettings, system: string, user: string, toolName: string, toolDescription: string, schema: any): Promise<T> {
    const res = await fetch('https://api.anthropic.com/v1/messages', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-api-key': s.apiKey,
        'anthropic-version': '2023-06-01',
        'anthropic-dangerous-direct-browser-access': 'true'
      },
      body: JSON.stringify({
        model: s.model || DEFAULT_MODEL.anthropic,
        max_tokens: 2048,
        system,
        messages: [{ role: 'user', content: user }],
        tools: [{ name: toolName, description: toolDescription, input_schema: schema }],
        tool_choice: { type: 'tool', name: toolName }
      })
    });
    if (!res.ok) throw new Error(await this.errText(res));
    const data = await res.json();
    const block = (data?.content || []).find((c: any) => c.type === 'tool_use');
    if (!block) throw new Error('Anthropic did not return a structured result.');
    return block.input as T;
  }

  private async errText(res: Response): Promise<string> {
    try {
      const j = await res.json();
      return j?.error?.message || `Request failed (${res.status})`;
    } catch { return `Request failed (${res.status})`; }
  }
}
