import { Injectable, signal } from '@angular/core';

export type LlmProvider = 'groq' | 'openai' | 'anthropic';

export interface LlmSettings {
  provider: LlmProvider;
  apiKey: string;
  model: string;
}

const DEFAULT_MODEL: Record<LlmProvider, string> = {
  // Groq deprecated llama-3.3-70b-versatile/llama-3.1-8b-instant on 2026-06-17 — current IDs need
  // the meta-llama/ prefix. Scout is the fast, tool-use-capable default; Maverick (larger, same
  // prefix) is a better-quality option worth setting manually if grading judgment matters more than speed.
  groq: 'meta-llama/llama-4-scout-17b-16e-instruct',
  openai: 'gpt-4o-mini',
  anthropic: 'claude-3-5-haiku-latest'
};

const OPENAI_COMPAT_BASE_URL: Record<'groq' | 'openai', string> = {
  groq: 'https://api.groq.com/openai/v1/chat/completions',
  openai: 'https://api.openai.com/v1/chat/completions'
};

/**
 * Calls the learner's own Groq, OpenAI, or Anthropic API key DIRECTLY from the browser — no backend
 * involved. Groq is the default: it's free (no card required, generous rate limits) and exposes an
 * OpenAI-compatible API, which is exactly what the original server-side version of this app used
 * (GROQ_API_KEY against api.groq.com/openai). The key is stored only in this browser's localStorage
 * and is never sent anywhere except straight to the chosen provider's API. Structured JSON output is
 * obtained via forced tool-use/function-calling on all three providers, far more reliable than asking
 * the model to "please reply with JSON."
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
    return { provider: 'groq', apiKey: '', model: DEFAULT_MODEL.groq };
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
   * Fetches the REAL, current list of models this key can use, straight from the provider — instead
   * of trusting any hardcoded ID (providers rotate/deprecate models regularly, which is exactly what
   * broke the old hardcoded Groq default). `override` lets the Settings page query with an
   * in-progress provider/key before it's been saved. Filters out obviously non-chat models (speech,
   * moderation, embeddings, safety classifiers) by name, since those can't do the tool-calling this
   * app needs for grading — but a provider can still list a model here that turns out not to support
   * tool calling, since that capability isn't exposed by any of these list-models APIs.
   */
  async listModels(override?: Partial<LlmSettings>): Promise<string[]> {
    const s = { ...this.settings(), ...override };
    if (!s.apiKey.trim()) throw new Error('Enter an API key first.');

    let ids: string[];
    if (s.provider === 'anthropic') {
      const res = await fetch('https://api.anthropic.com/v1/models?limit=1000', {
        headers: { 'x-api-key': s.apiKey, 'anthropic-version': '2023-06-01', 'anthropic-dangerous-direct-browser-access': 'true' }
      });
      if (!res.ok) throw new Error(await this.errText(res));
      const data = await res.json();
      ids = (data?.data || []).map((m: any) => m.id);
    } else {
      const base = s.provider === 'openai' ? 'https://api.openai.com/v1/models' : 'https://api.groq.com/openai/v1/models';
      const res = await fetch(base, { headers: { 'Authorization': `Bearer ${s.apiKey}` } });
      if (!res.ok) throw new Error(await this.errText(res));
      const data = await res.json();
      ids = (data?.data || []).map((m: any) => m.id);
    }

    const nonChat = /whisper|tts|speech|embed|moderation|guard|safety|dall-e|image|davinci|babbage|ada\b/i;
    const filtered = ids.filter(id => !nonChat.test(id)).sort();
    return filtered.length ? filtered : ids.sort();
  }

  /**
   * Calls the configured provider, forcing the model to return arguments matching `schema` via a
   * single forced tool call, and resolves with the parsed object. Throws with a readable message on
   * any failure (missing key, network error, malformed response) — callers should catch and show it.
   */
  async callStructured<T>(system: string, user: string, toolName: string, toolDescription: string, schema: any): Promise<T> {
    const s = this.settings();
    if (!s.apiKey.trim()) throw new Error('No API key configured — add one in Settings.');
    try {
      if (s.provider === 'anthropic') return await this.callAnthropic<T>(s, system, user, toolName, toolDescription, schema);
      return await this.callOpenAiCompatible<T>(s, system, user, toolName, toolDescription, schema);
    } catch (e: any) {
      // A raw "Failed to fetch"/"NetworkError" (no HTTP status attached) almost always means the
      // request never reached the provider at all — the browser blocked it, most commonly a CORS
      // rejection from that provider's API not allowing direct browser calls. Real API errors (bad
      // key, bad request, rate limit) instead go through errText() below and already have a specific
      // status-derived message, so they don't hit this branch.
      if (e instanceof TypeError) {
        throw new Error(`Request to ${s.provider} was blocked before it reached the server — this usually means that provider's API doesn't allow direct calls from a browser (CORS). Try a different provider in Settings.`);
      }
      throw e;
    }
  }

  /** Shared by Groq and OpenAI — both speak the identical OpenAI chat-completions + tool-calling format. */
  private async callOpenAiCompatible<T>(s: LlmSettings, system: string, user: string, toolName: string, toolDescription: string, schema: any): Promise<T> {
    const provider = s.provider === 'openai' ? 'openai' : 'groq';
    const res = await fetch(OPENAI_COMPAT_BASE_URL[provider], {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${s.apiKey}` },
      body: JSON.stringify({
        model: s.model || DEFAULT_MODEL[provider],
        messages: [{ role: 'system', content: system }, { role: 'user', content: user }],
        tools: [{ type: 'function', function: { name: toolName, description: toolDescription, parameters: schema } }],
        tool_choice: { type: 'function', function: { name: toolName } }
      })
    });
    if (!res.ok) throw new Error(await this.errText(res));
    const data = await res.json();
    const call = data?.choices?.[0]?.message?.tool_calls?.[0];
    if (!call) throw new Error(`${provider === 'groq' ? 'Groq' : 'OpenAI'} did not return a structured result.`);
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
