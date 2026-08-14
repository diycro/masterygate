package com.studio.exam;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The examiner brain: starts gate sessions and grades free-text answers with an LLM-as-judge.
 * Sessions are in-memory for the MVP, so this works even without the database.
 */
@Service
public class GateService {

    private static final String GRADER_SYSTEM = """
        You are a strict but fair senior technical interviewer grading a candidate's answer to a
        concept question. Judge whether the answer demonstrates REAL understanding of the key points.
        Be strict: vague, buzzword-only, or partially-correct answers must NOT be graded 'correct'.
        - verdict: exactly one of "correct", "partial", "incorrect".
        - score: 0-100 (correct ~80-100, partial ~40-79, incorrect ~0-39).
        - feedback: 1-2 sentences, specific — name what was right and what was wrong or missing.
        - missing: the key points the candidate did not cover (empty list if none).
        """;

    private final ModuleCatalog catalog;
    private final ObjectProvider<ChatModel> chatModelProvider;
    private final Map<String, GateSession> sessions = new ConcurrentHashMap<>();

    public GateService(ModuleCatalog catalog, ObjectProvider<ChatModel> chatModelProvider) {
        this.catalog = catalog;
        this.chatModelProvider = chatModelProvider;
    }

    public GateSession start(String moduleId) {
        ModuleCatalog.GateModule module = catalog.get(moduleId);
        if (module == null) throw new IllegalArgumentException("Unknown module: " + moduleId);
        GateSession session = new GateSession(moduleId, module.questions());
        sessions.put(session.id, session);
        return session;
    }

    public GateSession get(String sessionId) {
        GateSession s = sessions.get(sessionId);
        if (s == null) throw new IllegalArgumentException("Unknown or expired session");
        return s;
    }

    public GradeResult grade(GateSession session, String answer) {
        Question q = session.current();
        if (q == null) throw new IllegalStateException("Gate already complete");

        ChatModel model = chatModelProvider.getIfAvailable();
        GradeResult result;

        if (model == null) {
            result = new GradeResult("error", 0,
                    "LLM not configured — set your API key so the grader can run.", List.of());
        } else {
            String user = """
                QUESTION: %s

                KEY POINTS a correct answer should cover: %s

                CANDIDATE ANSWER: %s
                """.formatted(q.text(), String.join(" | ", q.keyPoints()),
                    answer == null || answer.isBlank() ? "(no answer given)" : answer);
            try {
                result = ChatClient.create(model)
                        .prompt()
                        .system(GRADER_SYSTEM)
                        .user(user)
                        .call()
                        .entity(GradeResult.class);
                if (result == null) {
                    result = new GradeResult("error", 0, "Grader returned no result.", List.of());
                }
            } catch (Exception e) {
                result = new GradeResult("error", 0, "Grading failed: " + e.getMessage(), List.of());
            }
        }

        session.grades.add(result);
        session.index++;
        return result;
    }
}
