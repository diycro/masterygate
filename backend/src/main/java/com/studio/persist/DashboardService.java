package com.studio.persist;

import com.studio.exam.ModuleCatalog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates a learner's real progress across ALL topics into one dashboard payload — no fabricated
 * numbers. Everything here is derived from actual ProgressEntity/ActivityEntity/InterviewQA rows.
 */
@Service
public class DashboardService {

    private final ModuleCatalog catalog;
    private final ProgressRepository progressRepo;
    private final ActivityService activityService;
    private final InterviewQARepository qaRepo;

    public DashboardService(ModuleCatalog catalog, ProgressRepository progressRepo,
                            ActivityService activityService, InterviewQARepository qaRepo) {
        this.catalog = catalog;
        this.progressRepo = progressRepo;
        this.activityService = activityService;
        this.qaRepo = qaRepo;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> build(Long userId) {
        List<ProgressEntity> all = progressRepo.findByUserId(userId);
        Map<String, List<ProgressEntity>> byTopic = new LinkedHashMap<>();
        for (ProgressEntity p : all) byTopic.computeIfAbsent(p.getTopicId(), k -> new ArrayList<>()).add(p);

        List<Map<String, Object>> tracks = new ArrayList<>();
        int totalModules = 0, totalPassed = 0;

        for (ModuleCatalog.Topic topic : catalog.topics()) {
            List<ModuleCatalog.Module> mods = catalog.modulesFor(topic.id());
            List<ProgressEntity> tp = byTopic.getOrDefault(topic.id(), List.of());
            long passed = tp.stream().filter(ProgressEntity::isPassed).count();
            double avg = tp.stream().filter(ProgressEntity::isPassed).mapToInt(ProgressEntity::getScore).average().orElse(0);
            List<String> moduleIds = mods.stream().map(ModuleCatalog.Module::id).toList();
            long qaCount = moduleIds.isEmpty() ? 0 : qaRepo.countByModuleIdIn(moduleIds);

            Map<String, Object> t = new LinkedHashMap<>();
            t.put("topicId", topic.id());
            t.put("title", topic.title());
            t.put("totalModules", mods.size());
            t.put("passedModules", (int) passed);
            t.put("avgScore", Math.round(avg));
            t.put("interviewQuestions", qaCount);
            t.put("started", !tp.isEmpty());
            tracks.add(t);

            totalModules += mods.size();
            totalPassed += (int) passed;
        }

        // Resume: most recently touched progress row, wherever it left off.
        Map<String, Object> resume = null;
        var mostRecent = progressRepo.findFirstByUserIdOrderByUpdatedAtDesc(userId);
        if (mostRecent.isPresent()) {
            ProgressEntity p = mostRecent.get();
            List<ModuleCatalog.Module> mods = catalog.modulesFor(p.getTopicId());
            ModuleCatalog.Module next = null;
            for (int i = 0; i < mods.size(); i++) {
                if (mods.get(i).id().equals(p.getModuleId()) && i + 1 < mods.size()) { next = mods.get(i + 1); break; }
            }
            resume = new LinkedHashMap<>();
            resume.put("topicId", p.getTopicId());
            resume.put("lastModuleId", p.getModuleId());
            resume.put("nextModuleId", next != null ? next.id() : null);
            resume.put("nextModuleTitle", next != null ? next.title() : null);
        }

        // Weak spots: passed modules with a borderline score (<85), lowest first — real signal, not invented.
        List<Map<String, Object>> weak = all.stream()
                .filter(ProgressEntity::isPassed)
                .filter(p -> p.getScore() < 85)
                .sorted(Comparator.comparingInt(ProgressEntity::getScore))
                .limit(5)
                .map(p -> {
                    ModuleCatalog.Module mod = catalog.getModule(p.getModuleId());
                    Map<String, Object> w = new LinkedHashMap<>();
                    w.put("topicId", p.getTopicId());
                    w.put("moduleId", p.getModuleId());
                    w.put("moduleTitle", mod != null ? mod.title() : p.getModuleId());
                    w.put("score", p.getScore());
                    return w;
                }).toList();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("tracks", tracks);
        out.put("totalModules", totalModules);
        out.put("totalPassed", totalPassed);
        out.put("streak", activityService.currentStreak(userId));
        out.put("resume", resume);
        out.put("weak", weak);
        return out;
    }
}
