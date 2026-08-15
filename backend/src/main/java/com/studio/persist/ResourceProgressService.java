package com.studio.persist;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ResourceProgressService {

    private final ResourceProgressRepository repo;

    public ResourceProgressService(ResourceProgressRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public Map<String, Boolean> getDone(Long userId, String moduleId) {
        Map<String, Boolean> out = new LinkedHashMap<>();
        for (ResourceProgressEntity e : repo.findByUserIdAndModuleId(userId, moduleId)) {
            if (e.isDone()) out.put(e.getResourceKey(), true);
        }
        return out;
    }

    @Transactional
    public void setDone(Long userId, String moduleId, String resourceKey, boolean done) {
        ResourceProgressEntity e = repo.findByUserIdAndModuleIdAndResourceKey(userId, moduleId, resourceKey)
                .orElseGet(ResourceProgressEntity::new);
        e.setUserId(userId);
        e.setModuleId(moduleId);
        e.setResourceKey(resourceKey);
        e.setDone(done);
        e.setUpdatedAt(Instant.now());
        repo.save(e);
    }
}
