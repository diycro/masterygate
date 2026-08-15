package com.studio.exam;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fetches highly-viewed, on-topic FREE videos per module via the official YouTube Data API v3
 * (not scraping). Real view counts let us rank by popularity and filter to well-watched tutorials.
 * Results are cached per module. If no API key is configured, this returns nothing and the app
 * falls back to the curated resources — the learning flow never breaks.
 */
@Service
public class YouTubeResourceService {

    /** Curated, on-topic search queries so videos stay aligned to each module. */
    // Keep queries SHORT and natural — long, specific phrases return 0 YouTube results.
    private static final Map<String, String> QUERIES = Map.of(
        "M0", "openai api python tutorial",
        "M1", "prompt engineering tutorial",
        "M2", "vector embeddings tutorial",
        "M3", "RAG tutorial",
        "M4", "AI agents tutorial",
        "M5", "LLM in production",
        "M6", "build RAG application"
    );
    private static final long MIN_VIEWS = 30_000;   // "viewed by a large group" floor
    private static final int MAX_RESULTS = 4;

    private static final Logger log = LoggerFactory.getLogger(YouTubeResourceService.class);

    private final String apiKey;
    private final RestClient rc = RestClient.create();
    private final Map<String, List<ModuleCatalog.Resource>> cache = new ConcurrentHashMap<>();
    private final Map<String, String> ctxCache = new ConcurrentHashMap<>();

    public YouTubeResourceService(@Value("${youtube.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        log.info("YouTube integration {}", (apiKey != null && !apiKey.isBlank())
                ? "ENABLED (API key detected, length=" + apiKey.length() + ")"
                : "DISABLED (no YOUTUBE_API_KEY seen by the app)");
    }

    public boolean enabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    public List<ModuleCatalog.Resource> topVideos(String moduleId, String fallbackTitle) {
        if (!enabled()) return List.of();
        List<ModuleCatalog.Resource> cached = cache.get(moduleId);
        if (cached != null) return cached;
        String query = QUERIES.getOrDefault(moduleId, fallbackTitle + " tutorial");
        List<ModuleCatalog.Resource> result;
        try {
            result = fetch(query);
        } catch (Exception e) {
            logYt("search (topVideos)", e);
            result = List.of();
        }
        if (!result.isEmpty()) cache.put(moduleId, result);   // never cache a failure/empty — so it retries + re-logs
        return result;
    }

    /** Top video's "title — description" for a module, to ground LLM question generation. "" if none. */
    public String bestVideoContext(String moduleId) {
        if (!enabled()) return "";
        return ctxCache.computeIfAbsent(moduleId, k -> {
            try {
                String query = QUERIES.getOrDefault(moduleId, moduleId + " tutorial");
                String url = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/search")
                        .queryParam("part", "snippet").queryParam("type", "video")
                        .queryParam("maxResults", 1).queryParam("order", "relevance")
                        .queryParam("q", query).queryParam("key", apiKey).toUriString();
                JsonNode s = rc.get().uri(url).retrieve().body(JsonNode.class);
                JsonNode sn = s == null ? null : s.path("items").path(0).path("snippet");
                if (sn == null || sn.isMissingNode()) return "";
                String title = sn.path("title").asText("");
                String desc = sn.path("description").asText("");
                if (desc.length() > 500) desc = desc.substring(0, 500);
                return (title + " — " + desc).trim();
            } catch (Exception e) {
                logYt("search (bestVideoContext)", e);
                return "";
            }
        });
    }

    private void logYt(String where, Exception e) {
        if (e instanceof RestClientResponseException r) {
            log.warn("YouTube {} failed: HTTP {} — {}", where, r.getStatusCode(), r.getResponseBodyAsString());
        } else {
            log.warn("YouTube {} failed: {}", where, e.toString());
        }
    }

    private List<ModuleCatalog.Resource> fetch(String query) {
        String searchUrl = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/search")
                .queryParam("part", "snippet").queryParam("type", "video")
                .queryParam("maxResults", 15).queryParam("order", "relevance")
                .queryParam("q", query).queryParam("key", apiKey).toUriString();
        log.info("YouTube search URL: {}", searchUrl.replaceAll("key=[^&]+", "key=***"));
        JsonNode search = rc.get().uri(searchUrl).retrieve().body(JsonNode.class);
        int searchCount = (search == null) ? 0 : search.path("items").size();
        log.info("YouTube search '{}' -> {} items", query, searchCount);
        if (search == null || !search.has("items")) return List.of();

        Map<String, String[]> byId = new LinkedHashMap<>(); // videoId -> [title, channel]
        for (JsonNode item : search.get("items")) {
            String vid = item.path("id").path("videoId").asText(null);
            if (vid != null) {
                JsonNode sn = item.path("snippet");
                byId.put(vid, new String[]{sn.path("title").asText(""), sn.path("channelTitle").asText("")});
            }
        }
        if (byId.isEmpty()) {
            log.warn("YouTube: parsed 0 videoIds from search response: {}",
                    search.toString().substring(0, Math.min(400, search.toString().length())));
            return List.of();
        }

        String statsUrl = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/videos")
                .queryParam("part", "statistics,contentDetails")
                .queryParam("id", String.join(",", byId.keySet()))
                .queryParam("key", apiKey).toUriString();
        JsonNode vids = rc.get().uri(statsUrl).retrieve().body(JsonNode.class);
        int vidCount = (vids == null) ? 0 : vids.path("items").size();
        log.info("YouTube videos.list -> {} items", vidCount);
        if (vids == null || !vids.has("items")) return List.of();

        Map<String, JsonNode> stats = new java.util.HashMap<>();
        for (JsonNode v : vids.get("items")) stats.put(v.path("id").asText(), v);

        record Vid(String id, String title, String channel, long views, String meta) {}
        // Build in YouTube's RELEVANCE order (byId preserves search order) — keeps videos on-topic.
        List<Vid> ordered = new ArrayList<>();
        for (Map.Entry<String, String[]> e : byId.entrySet()) {
            JsonNode v = stats.get(e.getKey());
            if (v == null) continue;
            long views = v.path("statistics").path("viewCount").asLong(0);
            String dur = formatDuration(v.path("contentDetails").path("duration").asText(""));
            String label = "▶ " + formatViews(views) + (dur.isEmpty() ? "" : " · " + dur);
            ordered.add(new Vid(e.getKey(), e.getValue()[0], e.getValue()[1], views, label));
        }

        // Relevance-first, but keep only well-viewed videos; if too few clear the bar,
        // fall back to relevance order so we always surface the best available.
        List<Vid> qualified = ordered.stream().filter(v -> v.views() >= MIN_VIEWS).toList();
        List<Vid> chosen = qualified.size() >= 2 ? qualified : ordered;

        List<ModuleCatalog.Resource> out = new ArrayList<>();
        for (Vid v : chosen.stream().limit(MAX_RESULTS).toList()) {
            out.add(new ModuleCatalog.Resource(v.title(), v.channel(),
                    "https://www.youtube.com/watch?v=" + v.id(), v.meta(), true));
        }
        log.info("YouTube module videos: {} chosen (from {} fetched, relevance-first)", out.size(), ordered.size());
        return out;
    }

    private static String formatViews(long v) {
        if (v >= 1_000_000) return String.format("%.1fM views", v / 1_000_000.0);
        if (v >= 1_000) return String.format("%.0fK views", v / 1_000.0);
        return v + " views";
    }

    private static String formatDuration(String iso) {
        try {
            Duration d = Duration.parse(iso);
            long minutes = d.toMinutes();
            return minutes >= 60 ? d.toHours() + "h " + (minutes % 60) + "m" : minutes + " min";
        } catch (Exception e) {
            return "";
        }
    }
}
