package com.studio.persist;

import com.studio.exam.InterviewQASeedData;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Loads the curated interview-question seed data into the DB once, on first startup. */
@Component
public class InterviewQASeeder implements CommandLineRunner {

    private final InterviewQARepository repo;

    public InterviewQASeeder(InterviewQARepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        InterviewQASeedData.all().forEach((moduleId, questions) -> {
            if (repo.countByModuleId(moduleId) > 0) return;   // already seeded
            for (InterviewQASeedData.SeedQA sq : questions) {
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
