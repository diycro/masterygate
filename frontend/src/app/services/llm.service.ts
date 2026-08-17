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
 * obtained via forced tool-use/function-calling when the model supports it (far more reliable than
 * asking the model to "please reply with JSON"), automatically falling back to a plain JSON-in-text
 * prompt for models that reject tool-calling — so the model you pick, from any of the three
 * providers, isn't itself a constraint on whether this app can use it.
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
      // Tool-calling can fail two different ways, and both mean the same thing for us: this model
      // can't be trusted to produce a valid structured tool call, so fall back to a plain-JSON
      // prompt instead. (1) The model doesn't support tool-calling AT ALL ("tool calling is not
      // supported with this model"). (2) The model attempted it but produced malformed arguments,
      // which the PROVIDER'S OWN SERVER rejects before it even reaches us ("Failed to parse tool
      // call arguments as JSON" — seen from Groq with openai/gpt-oss-120b). Neither is the user's
      // fault or something a different max_tokens/retry on the SAME approach would reliably fix.
      if (this.looksLikeToolCallingFailure(e)) {
        return await this.callPlainJsonWithRetry<T>(s, system, user, schema);
      }
      throw e;
    }
  }

  private looksLikeToolCallingFailure(e: any): boolean {
    const msg = String(e?.message || '').toLowerCase();
    if (!msg.includes('tool')) return false;
    return /not support|not enabled|not available|fail(?:ed)? to parse|invalid|malformed/.test(msg);
  }

  /**
   * Universal fallback: no tools/function-calling at all, just a plain completion asked to emit
   * JSON, parsed leniently. Retries once on a parse failure — LLM output is stochastic, and a
   * malformed/truncated response on one attempt often comes back clean on the next, especially from
   * smaller models. Only the SECOND failure's error (with a raw-output snippet attached) propagates.
   */
  private async callPlainJsonWithRetry<T>(s: LlmSettings, system: string, user: string, schema: any): Promise<T> {
    try {
      return await this.callPlainJson<T>(s, system, user, schema);
    } catch (e) {
      return await this.callPlainJson<T>(s, system, user, schema);
    }
  }

  private async callPlainJson<T>(s: LlmSettings, system: string, user: string, schema: any): Promise<T> {
    const jsonSystem = `${system}

Respond with ONLY a single valid JSON object — no markdown code fences, no commentary before or
after, nothing but the JSON itself — matching this JSON schema:
${JSON.stringify(schema)}

For example, a response with this exact shape (adjust the fields/values to match the schema above
and the actual content requested — this is only showing the FORMAT, not real content):
${this.exampleFor(schema)}`;
    // A generous, explicit cap — many providers default to a short completion length when
    // max_tokens is omitted, which truncates a multi-item JSON response mid-object and makes it
    // unparseable. This is likely why plain-JSON responses were failing to parse at all.
    const maxTokens = 4096;
    let content: string;
    if (s.provider === 'anthropic') {
      const res = await fetch('https://api.anthropic.com/v1/messages', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'x-api-key': s.apiKey,
          'anthropic-version': '2023-06-01',
          'anthropic-dangerous-direct-browser-access': 'true'
        },
        body: JSON.stringify({
          model: s.model || DEFAULT_MODEL.anthropic, max_tokens: maxTokens,
          system: jsonSystem, messages: [{ role: 'user', content: user }]
        })
      });
      if (!res.ok) throw new Error(await this.errText(res));
      const data = await res.json();
      content = (data?.content || []).filter((c: any) => c.type === 'text').map((c: any) => c.text).join('');
    } else {
      const provider = s.provider === 'openai' ? 'openai' : 'groq';
      const res = await fetch(OPENAI_COMPAT_BASE_URL[provider], {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${s.apiKey}` },
        body: JSON.stringify({
          model: s.model || DEFAULT_MODEL[provider], max_tokens: maxTokens,
          messages: [{ role: 'system', content: jsonSystem }, { role: 'user', content: user }]
        })
      });
      if (!res.ok) throw new Error(await this.errText(res));
      const data = await res.json();
      content = data?.choices?.[0]?.message?.content || '';
    }
    return this.parseLenientJson<T>(content);
  }

  /** A tiny, generic instance of a JSON Schema object — just enough to show the model the shape, not real content. */
  private exampleFor(schema: any): string {
    const exampleValue = (s: any): any => {
      if (!s || typeof s !== 'object') return null;
      if (s.type === 'object') {
        const out: any = {};
        for (const k of Object.keys(s.properties || {})) out[k] = exampleValue(s.properties[k]);
        return out;
      }
      if (s.type === 'array') return [exampleValue(s.items)];
      if (s.type === 'string') return s.enum ? s.enum[0] : '...';
      if (s.type === 'integer' || s.type === 'number') return 0;
      return null;
    };
    try { return JSON.stringify(exampleValue(schema)); } catch { return '{}'; }
  }

  private parseLenientJson<T>(text: string): T {
    let s = text.trim();
    const fence = /```(?:json)?\s*([\s\S]*?)```/i.exec(s);
    if (fence) s = fence[1].trim();
    if (!s.startsWith('{')) {
      const start = s.indexOf('{');
      const end = s.lastIndexOf('}');
      if (start !== -1 && end !== -1 && end > start) s = s.slice(start, end + 1);
    }
    try { return JSON.parse(s) as T; }
    catch {
      const snippet = text.length > 200 ? text.slice(0, 200) + '…' : text;
      throw new Error(`The model's response wasn't valid JSON: "${snippet || '(empty response)'}"`);
    }
  }

  /** Shared by Groq and OpenAI — both speak the identical OpenAI chat-completions + tool-calling format. */
  private async callOpenAiCompatible<T>(s: LlmSettings, system: string, user: string, toolName: string, toolDescription: string, schema: any): Promise<T> {
    const provider = s.provider === 'openai' ? 'openai' : 'groq';
    const res = await fetch(OPENAI_COMPAT_BASE_URL[provider], {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${s.apiKey}` },
      body: JSON.stringify({
        model: s.model || DEFAULT_MODEL[provider], max_tokens: 4096,
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
        max_tokens: 4096,
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
