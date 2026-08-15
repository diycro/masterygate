package com.studio.exam;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** A full mock-interview loop: DSA -> System Design -> a topic deep-dive. In-memory, like GateSession. */
public class MockInterviewSession {
    public final String id = UUID.randomUUID().toString();
    public final List<MockRound> rounds;
    public final Long userId;
    public final String focusTopicId;
    public final Instant startedAt = Instant.now();
    public int index = 0;

    public MockInterviewSession(List<MockRound> rounds, Long userId, String focusTopicId) {
        this.rounds = rounds;
        this.userId = userId;
        this.focusTopicId = focusTopicId;
    }

    public MockRound current() {
        return index < rounds.size() ? rounds.get(index) : null;
    }

    public boolean done() {
        return index >= rounds.size();
    }

    public int overallScore() {
        return (int) Math.round(rounds.stream().filter(r -> r.grade != null)
                .mapToInt(r -> r.grade.score()).average().orElse(0));
    }
}
