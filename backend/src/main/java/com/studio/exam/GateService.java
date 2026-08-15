package com.studio.exam;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The examiner brain: starts gate sessions and grades free-text answers with an LLM-as-judge (via the
 * shared AnswerGrader). Sessions are in-memory for the MVP, so this works even without the database.
 */
@Service
public class GateService {

    private final ModuleCatalog catalog;
    private final AnswerGrader grader;
    private final YouTubeResourceService youtube;
    private final QuestionGenerator generator;
    private final Map<String, GateSession> sessions = new ConcurrentHashMap<>();

    public GateService(ModuleCatalog catalog, AnswerGrader grader,
                       YouTubeResourceService youtube, QuestionGenerator generator) {
        this.catalog = catalog;
        this.grader = grader;
        this.youtube = youtube;
        this.generator = generator;
    }

    public GateSession start(String moduleId) {
        return start(moduleId, false, null);
    }

    public GateSession start(String moduleId, boolean dynamic) {
        return start(moduleId, dynamic, null);
    }

    /**
     * Start a gate. When {@code dynamic} is true, generate fresh questions with the LLM grounded in the
     * module's objectives and its top video's topic (falling back to the curated questions if generation
     * fails). Fresh questions each time also make the gate harder to game.
     */
    public GateSession start(String moduleId, boolean dynamic, Long userId) {
        ModuleCatalog.Module module = catalog.getModule(moduleId);
        if (module == null) throw new IllegalArgumentException("Unknown module: " + moduleId);

        List<Question> questions;
        if (dynamic) {
            int n = Math.max(4, module.questions().size());
            questions = generator.generate(module.title(), module.objectives(),
                    youtube.bestVideoContext(moduleId), n);
            if (questions.isEmpty()) questions = module.questions();   // graceful fallback
        } else {
            questions = module.questions();
        }

        GateSession session = new GateSession(moduleId, questions);
        session.userId = userId;
        session.topicId = module.topicId();
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

        GradeResult result = grader.grade(q.text(), q.keyPoints(), answer);
        session.grades.add(result);
        session.index++;
        return result;
    }
}
