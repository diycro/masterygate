package com.studio.exam;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Runs a full mock-interview loop: one DSA round, one System Design round, and one "deep dive" round
 * drawn from the learner's chosen focus track (GenAI or Java Full-Stack). Questions are drawn from the
 * SAME curated question banks used by the module gates, so answers are graded to the same bar.
 */
@Service
public class MockInterviewService {

    private static final int DSA_TIME_SEC = 10 * 60;
    private static final int SYSTEM_DESIGN_TIME_SEC = 15 * 60;
    private static final int DEEP_DIVE_TIME_SEC = 10 * 60;

    private final ModuleCatalog catalog;
    private final AnswerGrader grader;
    private final Map<String, MockInterviewSession> sessions = new ConcurrentHashMap<>();

    public MockInterviewService(ModuleCatalog catalog, AnswerGrader grader) {
        this.catalog = catalog;
        this.grader = grader;
    }

    public MockInterviewSession start(String focusTopicId, Long userId) {
        String focus = (focusTopicId == null || catalog.modulesFor(focusTopicId).isEmpty()) ? "genai" : focusTopicId;

        Question dsaQ = randomQuestion("dsa");
        Question sdQ = randomQuestion("sysdesign");
        Question deepQ = randomQuestion(focus);
        if (dsaQ == null || sdQ == null || deepQ == null) {
            throw new IllegalStateException("Question bank unavailable — cannot start a mock interview.");
        }

        List<MockRound> rounds = new ArrayList<>();
        rounds.add(new MockRound("DSA", "Round 1 — Data Structures & Algorithms", dsaQ, DSA_TIME_SEC));
        rounds.add(new MockRound("SYSTEM_DESIGN", "Round 2 — System Design", sdQ, SYSTEM_DESIGN_TIME_SEC));
        rounds.add(new MockRound("DEEP_DIVE", "Round 3 — " + catalog.topics().stream()
                .filter(t -> t.id().equals(focus)).findFirst().map(ModuleCatalog.Topic::title).orElse("Deep Dive"),
                deepQ, DEEP_DIVE_TIME_SEC));

        MockInterviewSession session = new MockInterviewSession(rounds, userId, focus);
        sessions.put(session.id, session);
        return session;
    }

    private Question randomQuestion(String topicId) {
        List<Question> pool = new ArrayList<>();
        for (ModuleCatalog.Module m : catalog.modulesFor(topicId)) pool.addAll(m.questions());
        if (pool.isEmpty()) return null;
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    public MockInterviewSession get(String sessionId) {
        MockInterviewSession s = sessions.get(sessionId);
        if (s == null) throw new IllegalArgumentException("Unknown or expired mock interview session");
        return s;
    }

    public GradeResult answer(MockInterviewSession session, String answerText) {
        MockRound round = session.current();
        if (round == null) throw new IllegalStateException("Mock interview already complete");
        GradeResult result = grader.grade(round.question.text(), round.question.keyPoints(), answerText);
        round.answer = answerText;
        round.grade = result;
        session.index++;
        return result;
    }
}
