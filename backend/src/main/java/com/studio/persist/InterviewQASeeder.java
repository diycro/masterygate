package com.studio.persist;

import com.studio.exam.InterviewQASeedData;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Loads the curated interview-question seed data into the DB on every startup, adding only
 * questions that aren't already there (deduped per module by exact question text). This means
 * the seed bank can keep growing across app updates — an existing installation with an older,
 * smaller seed set for a module still picks up newly-added questions on the next restart,
 * instead of that module being frozen at whatever it had the first time it was ever seeded.
 */
@Component
public class InterviewQASeeder implements CommandLineRunner {

    private final InterviewQARepository repo;

    public InterviewQASeeder(InterviewQARepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        InterviewQASeedData.all().forEach((moduleId, questions) -> {
            for (InterviewQASeedData.SeedQA sq : questions) {
                if (repo.existsByModuleIdAndQuestion(moduleId, sq.question())) continue;
                InterviewQAEntity e = new InterviewQAEntity();
                e.setModuleId(moduleId);
                e.setQuestion(sq.question());
                e.setAnswer(sq.answer());
                e.setExplanation(sq.explanation());
                e.setFrequency(sq.frequency());
                e.setSource("curated");
                e.setCreatedAt(Instant.now());
                repo.save(e);
            }
        });
    }
}
