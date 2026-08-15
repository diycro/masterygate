package com.studio.exam;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates interview-style gate questions (with grading key points) for a module using the LLM,
 * grounded in the module's title, objectives, and — when available — its top video's title/description.
 * Returns an empty list on any failure so callers can fall back to curated questions.
 */
@Service
public class QuestionGenerator {

    private static final String SYSTEM = """
        You write interview-style CONCEPTUAL questions that test whether a learner truly understands a
        software/AI module — not trivia. Each question must be answerable in a few sentences and probe
        the "why/how", matching the module's level. For EACH question also list the KEY POINTS a correct
        answer must cover (these are used later to grade the learner strictly). Ground the questions in the
        provided objectives and, if given, the top video's topic. Do not ask about the video itself.
        """;

    private final ObjectProvider<ChatModel> chatModelProvider;

    public QuestionGenerator(ObjectProvider<ChatModel> chatModelProvider) {
        this.chatModelProvider = chatModelProvider;
    }

    public record GenQ(String question, List<String> keyPoints) {}
    public record GenQuiz(List<GenQ> questions) {}

    public List<Question> generate(String moduleTitle, List<String> objectives, String videoContext, int n) {
        ChatModel model = chatModelProvider.getIfAvailable();
        if (model == null) return List.of();

        String grounding = (videoContext == null || videoContext.isBlank())
                ? "" : "\nTOP VIDEO FOR THIS MODULE (for grounding the topic): " + videoContext;
        String user = """
            MODULE: %s
            OBJECTIVES: %s%s

            Generate exactly %d questions.
            """.formatted(moduleTitle, String.join("; ", objectives), grounding, n);

        try {
            GenQuiz quiz = ChatClient.create(model)
                    .prompt().system(SYSTEM).user(user)
                    .call().entity(GenQuiz.class);
            if (quiz == null || quiz.questions() == null) return List.of();

            List<Question> out = new ArrayList<>();
            int i = 1;
            for (GenQ g : quiz.questions()) {
                if (g.question() == null || g.question().isBlank()) continue;
                out.add(new Question("gen" + (i++), g.question(),
                        g.keyPoints() == null ? List.of() : g.keyPoints()));
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }
}
