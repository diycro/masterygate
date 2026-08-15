package com.studio.web;

import com.studio.persist.ActivityService;
import com.studio.persist.UserEntity;
import com.studio.persist.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** Minimal name-based login (no password): find-or-create the learner, return their id. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final ActivityService activityService;

    public AuthController(UserService userService, ActivityService activityService) {
        this.userService = userService;
        this.activityService = activityService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        if (name.isEmpty()) throw new IllegalArgumentException("name is required");
        UserEntity u = userService.findOrCreate(name);
        activityService.recordToday(u.getId());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("userId", u.getId());
        out.put("name", u.getName());
        return out;
    }
}
