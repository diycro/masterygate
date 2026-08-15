package com.studio.web;

import com.studio.persist.ResourceProgressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/resource-progress")
public class ResourceProgressController {

    private final ResourceProgressService service;

    public ResourceProgressController(ResourceProgressService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, Boolean> get(@RequestParam Long userId, @RequestParam String moduleId) {
        return service.getDone(userId, moduleId);
    }

    public record SetRequest(Long userId, String moduleId, String url, boolean done) {}

    @PostMapping
    public Map<String, Object> set(@RequestBody SetRequest req) {
        service.setDone(req.userId(), req.moduleId(), req.url(), req.done());
        return Map.of("ok", true);
    }
}
