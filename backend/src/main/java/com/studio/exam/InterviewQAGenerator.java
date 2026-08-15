package com.studio.exam;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates NEW interview-prep Q&A (full answer + explanation) for a module via the LLM, grounded in
 * the module's objectives and avoiding questions already in the bank. Used by the "Get more questions"
 * feature so the bank keeps growing over time. Returns an empty list on any failure (never breaks the UI).
 */
@Service
public class InterviewQAGenerator {

    private static final String SYSTEM = """
        You write REAL, frequently-asked technical interview questions for the given module — the kind
        that actually appear in industry interviews (as compiled by sites like InterviewBit, GeeksforGeeks,
        Exponent, DataCamp). For EACH question, provide: a complete, correct ANSWER (2-4 sentences, as a
        strong candidate would say it out loud), and a short EXPLANATION of why interviewers ask this /
        what it signals. Match the module's level and topic exactly. Do not repeat any question in the
        AVOID list (rephrasing an avoided question is also not allowed — pick a genuinely different one).
        """;

    private final ObjectProvider<ChatModel> chatModelProvider;

    public InterviewQAGenerator(ObjectProvider<ChatModel> chatModelProvider) {
        this.chatModelProvider = chatModelProvider;
    }

    public record GenItem(String question, String answer, String explanation) {}
    public record GenBank(List<GenItem> items) {}

    public List<GenItem> generate(String moduleTitle, List<String> objectives, List<String> avoidQuestions, int n) {
        ChatModel model = chatModelProvider.getIfAvailable();
        if (model == null) return List.of();

        String avoid = avoidQuestions.isEmpty() ? "(none yet)" : String.join(" | ", avoidQuestions);
        String user = """
            MODULE: %s
            OBJECTIVES: %s
            AVOID (already asked): %s

            Generate exactly %d new interview questions with answers and explanations.
            """.formatted(moduleTitle, String.join("; ", objectives), avoid, n);

        try {
            GenBank bank = ChatClient.create(model)
                    .prompt().system(SYSTEM).user(user)
                    .call().entity(GenBank.class);
            if (bank == null || bank.items() == null) return List.of();

            List<GenItem> out = new ArrayList<>();
            for (GenItem it : bank.items()) {
                if (it.question() != null && !it.question().isBlank()
                        && it.answer() != null && !it.answer().isBlank()) {
                    out.add(it);
                }
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }
}
