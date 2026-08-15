package com.studio.persist;

import com.studio.exam.InterviewQAGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class InterviewQAService {

    private final InterviewQARepository repo;
    private final InterviewQAGenerator generator;

    public InterviewQAService(InterviewQARepository repo, InterviewQAGenerator generator) {
        this.repo = repo;
        this.generator = generator;
    }

    @Transactional(readOnly = true)
    public List<InterviewQAEntity> list(String moduleId) {
        return repo.findByModuleIdOrderByIdAsc(moduleId);
    }

    /**
     * Ask the LLM for N new questions (avoiding ones already in the bank) and PERMANENTLY save the
     * ones that are genuinely new — the bank only ever grows. Returns the newly added entities.
     */
    @Transactional
    public List<InterviewQAEntity> generateMore(String moduleId, String moduleTitle, List<String> objectives, int n) {
        List<InterviewQAEntity> existing = repo.findByModuleIdOrderByIdAsc(moduleId);
        List<String> avoid = existing.stream().map(InterviewQAEntity::getQuestion).toList();

        List<InterviewQAGenerator.GenItem> generated = generator.generate(moduleTitle, objectives, avoid, n);

        List<InterviewQAEntity> added = new ArrayList<>();
        for (InterviewQAGenerator.GenItem g : generated) {
            if (repo.existsByModuleIdAndQuestion(moduleId, g.question())) continue;   // skip exact dupes
            InterviewQAEntity e = new InterviewQAEntity();
            e.setModuleId(moduleId);
            e.setQuestion(g.question());
            e.setAnswer(g.answer());
            e.setExplanation(g.explanation() == null ? "" : g.explanation());
            e.setFrequency("medium");
            e.setSource("generated");
            e.setCreatedAt(Instant.now());
            added.add(repo.save(e));
        }
        return added;
    }
}
