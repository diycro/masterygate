package com.studio.web;

import com.studio.exam.ModuleCatalog;
import com.studio.exam.YouTubeResourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only curriculum for the UI: available topics and, per topic, the ordered modules with their
 * objectives, curated resources, and interview focus. Never returns gate questions or answer keys.
 */
@RestController
@RequestMapping("/api/curriculum")
public class CurriculumController {

    private final ModuleCatalog catalog;
    private final YouTubeResourceService youtube;

    public CurriculumController(ModuleCatalog catalog, YouTubeResourceService youtube) {
        this.catalog = catalog;
        this.youtube = youtube;
    }

    /** Highly-viewed free videos for a module, fetched from the YouTube Data API (empty if no key). */
    @GetMapping("/videos")
    public List<Map<String, Object>> videos(@RequestParam String moduleId) {
        ModuleCatalog.Module m = catalog.getModule(moduleId);
        if (m == null) return List.of();
        List<Map<String, Object>> out = new ArrayList<>();
        for (ModuleCatalog.Resource r : youtube.topVideos(moduleId, m.title())) {
            Map<String, Object> rm = new LinkedHashMap<>();
            rm.put("title", r.title());
            rm.put("provider", r.provider());
            rm.put("url", r.url());
            rm.put("meta", r.meta());
            rm.put("free", true);
            out.add(rm);
        }
        return out;
    }

    @GetMapping("/topics")
    public List<Map<String, Object>> topics() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ModuleCatalog.Topic topic : catalog.topics()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", topic.id());
            m.put("title", topic.title());
            m.put("subtitle", topic.subtitle());
            m.put("moduleCount", catalog.modulesFor(topic.id()).size());
            out.add(m);
        }
        return out;
    }

    @GetMapping("/path")
    public Map<String, Object> path(@RequestParam String topic) {
        List<Map<String, Object>> mods = new ArrayList<>();
        for (ModuleCatalog.Module module : catalog.modulesFor(topic)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", module.id());
            m.put("order", module.order());
            m.put("title", module.title());
            m.put("objectives", module.objectives());
            m.put("interviewFocus", module.interviewFocus());
            m.put("questionCount", module.questions().size());

            List<Map<String, Object>> resources = new ArrayList<>();
            for (ModuleCatalog.Resource r : module.resources()) {
                Map<String, Object> rm = new LinkedHashMap<>();
                rm.put("title", r.title());
                rm.put("provider", r.provider());
                rm.put("url", r.url());
                rm.put("meta", r.meta());
                rm.put("free", r.free());
                resources.add(rm);
            }
            m.put("resources", resources);
            mods.add(m);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("topic", topic);
        out.put("modules", mods);
        return out;
    }
}
