package com.studio.persist;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ProgressService {

    private final ProgressRepository repo;

    public ProgressService(ProgressRepository repo) {
        this.repo = repo;
    }

    /** Record (or update) a cleared module, keeping the best score. */
    @Transactional
    public void recordPass(Long userId, String topicId, String moduleId, int score) {
        ProgressEntity p = repo.findByUserIdAndTopicIdAndModuleId(userId, topicId, moduleId)
                .orElseGet(ProgressEntity::new);
        p.setUserId(userId);
        p.setTopicId(topicId);
        p.setModuleId(moduleId);
        p.setPassed(true);
        p.setScore(Math.max(p.getScore(), score));
        p.setUpdatedAt(Instant.now());
        repo.save(p);
    }

    /** {passed: {moduleId: true}, scores: {moduleId: n}} for a user's topic. */
    @Transactional(readOnly = true)
    public Map<String, Object> getProgress(Long userId, String topicId) {
        Map<String, Boolean> passed = new LinkedHashMap<>();
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (ProgressEntity p : repo.findByUserIdAndTopicId(userId, topicId)) {
            if (p.isPassed()) {
                passed.put(p.getModuleId(), true);
                scores.put(p.getModuleId(), p.getScore());
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("passed", passed);
        out.put("scores", scores);
        return out;
    }
}
