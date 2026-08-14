package com.studio.web;

import com.studio.config.AuthProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sprint 0 acceptance endpoint — proves the app boots and DI works.
 * GET /api/health  ->  {"status":"ok",...}
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private final AuthProvider authProvider;

    public HealthController(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "ok");
        body.put("service", "learning-studio");
        body.put("currentUser", authProvider.currentUserId());
        body.put("time", Instant.now().toString());
        return body;
    }
}
