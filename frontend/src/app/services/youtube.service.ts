import { Injectable, signal } from '@angular/core';

export interface YtVideo { title: string; provider: string; url: string; meta: string; }

const MIN_VIEWS = 30_000;   // "viewed by a large group" floor
const MAX_RESULTS = 4;

/**
 * Optional, learner-supplied YouTube Data API v3 key, called directly from the browser — same BYO-key
 * model as the LLM providers. With no key, the module page just shows a plain "browse on YouTube"
 * link (no feature is lost). With a key, this ranks real search results by view count so the module
 * page can show actual highly-watched tutorials instead of a blind search link. Free tier: 10,000
 * quota units/day (a module lookup costs ~100-130 units, so roughly 75-100 module loads/day).
 */
@Injectable({ providedIn: 'root' })
export class YoutubeService {
  private readonly KEY = 'studio.youtube.apiKey';
  private cache = new Map<string, YtVideo[]>();

  apiKey = signal<string>(this.load());

  private load(): string {
    try { return localStorage.getItem(this.KEY) || ''; } catch { return ''; }
  }

  save(key: string) {
    const trimmed = key.trim();
    localStorage.setItem(this.KEY, trimmed);
    this.apiKey.set(trimmed);
    this.cache.clear();
  }

  get configured(): boolean {
    return !!this.apiKey().trim();
  }

  async topVideos(moduleId: string, moduleTitle: string): Promise<YtVideo[]> {
    if (!this.configured) return [];
    const cached = this.cache.get(moduleId);
    if (cached) return cached;
    let result: YtVideo[] = [];
    try {
      result = await this.fetch(moduleTitle + ' tutorial');
    } catch (e) {
      console.error('YouTube fetch failed:', e);
      result = [];
    }
    if (result.length) this.cache.set(moduleId, result); // never cache a failure/empty — retry next time
    return result;
  }

  private async fetch(query: string): Promise<YtVideo[]> {
    const key = this.apiKey().trim();
    const searchUrl = `https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=15&order=relevance&q=${encodeURIComponent(query)}&key=${key}`;
    const searchRes = await fetch(searchUrl);
    if (!searchRes.ok) throw new Error(`YouTube search failed: HTTP ${searchRes.status} — ${await this.snippet(searchRes)}`);
    const search = await searchRes.json();
    const items: any[] = search.items || [];

    const byId = new Map<string, { title: string; channel: string }>();
    for (const item of items) {
      const vid = item?.id?.videoId;
      if (vid) byId.set(vid, { title: item.snippet?.title || '', channel: item.snippet?.channelTitle || '' });
    }
    if (!byId.size) return [];

    const statsUrl = `https://www.googleapis.com/youtube/v3/videos?part=statistics,contentDetails&id=${[...byId.keys()].join(',')}&key=${key}`;
    const statsRes = await fetch(statsUrl);
    if (!statsRes.ok) throw new Error(`YouTube videos.list failed: HTTP ${statsRes.status} — ${await this.snippet(statsRes)}`);
    const vids = await statsRes.json();
    const stats = new Map<string, any>();
    for (const v of vids.items || []) stats.set(v.id, v);

    // Relevance-first (byId preserves search order) — keeps videos on-topic.
    const ordered: { id: string; title: string; channel: string; views: number; meta: string }[] = [];
    for (const [id, info] of byId) {
      const v = stats.get(id);
      if (!v) continue;
      const views = Number(v.statistics?.viewCount || 0);
      const dur = this.formatDuration(v.contentDetails?.duration || '');
      const meta = '▶ ' + this.formatViews(views) + (dur ? ' · ' + dur : '');
      ordered.push({ id, title: info.title, channel: info.channel, views, meta });
    }

    // Relevance-first, but keep only well-viewed videos; if too few clear the bar,
    // fall back to relevance order so we always surface the best available.
    const qualified = ordered.filter(v => v.views >= MIN_VIEWS);
    const chosen = qualified.length >= 2 ? qualified : ordered;

    return chosen.slice(0, MAX_RESULTS).map(v => ({
      title: v.title, provider: v.channel, url: `https://www.youtube.com/watch?v=${v.id}`, meta: v.meta
    }));
  }

  private async snippet(res: Response): Promise<string> {
    try { return (await res.text()).slice(0, 200); } catch { return ''; }
  }

  private formatViews(v: number): string {
    if (v >= 1_000_000) return (v / 1_000_000).toFixed(1) + 'M views';
    if (v >= 1_000) return Math.round(v / 1_000) + 'K views';
    return v + ' views';
  }

  private formatDuration(iso: string): string {
    const m = /^PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?$/.exec(iso);
    if (!m) return '';
    const hours = Number(m[1] || 0), minutes = Number(m[2] || 0), seconds = Number(m[3] || 0);
    const totalMinutes = hours * 60 + minutes + (seconds >= 30 ? 1 : 0);
    return hours > 0 ? `${hours}h ${totalMinutes % 60}m` : `${totalMinutes} min`;
  }
}
