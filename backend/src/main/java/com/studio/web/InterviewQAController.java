package com.studio.web;

import com.studio.exam.ModuleCatalog;
import com.studio.persist.InterviewQAEntity;
import com.studio.persist.InterviewQAService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Interview-prep Q&A bank per module: a growing set of real questions with full answers + explanations. */
@RestController
@RequestMapping("/api/interview")
public class InterviewQAController {

    private final InterviewQAService service;
    private final ModuleCatalog catalog;

    public InterviewQAController(InterviewQAService service, ModuleCatalog catalog) {
        this.service = service;
        this.catalog = catalog;
    }

    @GetMapping("/questions")
    public List<Map<String, Object>> list(@RequestParam String moduleId) {
        return toDto(service.list(moduleId));
    }

    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestParam String moduleId,
                                        @RequestParam(defaultValue = "4") int count) {
        ModuleCatalog.Module m = catalog.getModule(moduleId);
        Map<String, Object> out = new LinkedHashMap<>();
        if (m == null) {
            out.put("added", List.of());
            out.put("message", "Unknown module.");
            return out;
        }
        List<InterviewQAEntity> added = service.generateMore(moduleId, m.title(), m.objectives(), count);
        out.put("added", toDto(added));
        if (added.isEmpty()) {
            out.put("message", "Couldn't generate new questions right now (LLM unavailable, or no fresh ones found). Try again shortly.");
        }
        return out;
    }

    private List<Map<String, Object>> toDto(List<InterviewQAEntity> list) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (InterviewQAEntity e : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("question", e.getQuestion());
            m.put("answer", e.getAnswer());
            m.put("explanation", e.getExplanation());
            m.put("frequency", e.getFrequency());
            m.put("source", e.getSource());
            out.add(m);
        }
        return out;
    }
}
