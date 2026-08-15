package com.studio.exam;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** In-memory state for one attempt at a module's gate. (Persisted to the DB in a later sprint.) */
public class GateSession {

    public final String id = UUID.randomUUID().toString();
    public final String moduleId;
    public final List<Question> questions;
    public int index = 0;
    public final List<GradeResult> grades = new ArrayList<>();
    public Long userId;        // who is taking the gate (for persisting the result); may be null
    public String topicId;     // the module's topic (for persisting the result)

    public GateSession(String moduleId, List<Question> questions) {
        this.moduleId = moduleId;
        this.questions = questions;
    }

    public Question current() {
        return index < questions.size() ? questions.get(index) : null;
    }

    public boolean done() {
        return index >= questions.size();
    }

    public int avgScore() {
        return grades.isEmpty() ? 0 : grades.stream().mapToInt(GradeResult::score).sum() / grades.size();
    }

    /** Pass rule for the MVP: average score >= 70 across all questions. */
    public boolean passed() {
        return done() && avgScore() >= 70;
    }
}
