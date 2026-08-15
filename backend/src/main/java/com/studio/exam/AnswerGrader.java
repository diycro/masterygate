package com.studio.exam;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The shared LLM-as-judge grader: strictly grades a free-text answer against a question's key points.
 * Used by both the module gate (GateService) and Mock Interview mode, so both stay consistent and any
 * prompt tuning happens in exactly one place.
 */
@Service
public class AnswerGrader {

    private static final String GRADER_SYSTEM = """
        You are a strict but fair senior technical interviewer grading a candidate's answer to a
        concept question. Judge whether the answer demonstrates REAL understanding of the key points.
        Be strict: vague, buzzword-only, or partially-correct answers must NOT be graded 'correct'.
        - verdict: exactly one of "correct", "partial", "incorrect".
        - score: 0-100 (correct ~80-100, partial ~40-79, incorrect ~0-39).
        - feedback: 1-2 sentences, specific — name what was right and what was wrong or missing.
        - missing: the key points the candidate did not cover (empty list if none).
        """;

    private final ObjectProvider<ChatModel> chatModelProvider;

    public AnswerGrader(ObjectProvider<ChatModel> chatModelProvider) {
        this.chatModelProvider = chatModelProvider;
    }

    public boolean available() {
        return chatModelProvider.getIfAvailable() != null;
    }

    public GradeResult grade(String questionText, List<String> keyPoints, String answer) {
        ChatModel model = chatModelProvider.getIfAvailable();
        if (model == null) {
            return new GradeResult("error", 0, "LLM not configured — set your API key so the grader can run.", List.of());
        }
        String user = """
            QUESTION: %s

            KEY POINTS a correct answer should cover: %s

            CANDIDATE ANSWER: %s
            """.formatted(questionText, String.join(" | ", keyPoints),
                answer == null || answer.isBlank() ? "(no answer given)" : answer);
        try {
            GradeResult result = ChatClient.create(model)
                    .prompt()
                    .system(GRADER_SYSTEM)
                    .user(user)
                    .call()
                    .entity(GradeResult.class);
            return result != null ? result : new GradeResult("error", 0, "Grader returned no result.", List.of());
        } catch (Exception e) {
            return new GradeResult("error", 0, "Grading failed: " + e.getMessage(), List.of());
        }
    }
}
