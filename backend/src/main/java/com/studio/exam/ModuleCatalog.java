package com.studio.exam;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The seed content for gates. For the MVP this is hardcoded; later it moves to the DB and
 * questions can be LLM-generated. M1 reproduces the exact "LLM Fundamentals" gate your tutor ran.
 */
@Component
public class ModuleCatalog {

    public record GateModule(String id, String title, List<Question> questions) {}

    private final Map<String, GateModule> modules = new LinkedHashMap<>();

    public ModuleCatalog() {
        modules.put("M1", new GateModule("M1", "LLM Fundamentals", List.of(
            new Question("q1",
                "Why is an LLM call's token count higher than the number of words in your message?",
                List.of("tokens include the role/formatting scaffolding (system/user markers), not just your words",
                        "tokenization splits text into sub-word pieces",
                        "punctuation and spaces also count as tokens")),
            new Question("q2",
                "What does the 'temperature' setting control, and what do higher vs lower values do?",
                List.of("controls the randomness of token sampling, NOT accuracy or correctness",
                        "low temperature = focused, repeatable, near-deterministic output",
                        "high temperature = more varied and creative output",
                        "it reshapes the probability distribution before sampling")),
            new Question("q3",
                "You pay per token in two buckets — name both, and say which is usually priced higher.",
                List.of("input / prompt tokens", "output / completion tokens", "output tokens are priced higher")),
            new Question("q4",
                "Mechanically, why can the exact same prompt return different text on different runs?",
                List.of("the model produces a probability distribution over the next token",
                        "it samples (a weighted random draw) instead of always taking the top token",
                        "so different runs pick different tokens and diverge")),
            new Question("q5",
                "What is a system message vs a user message, and when would you use a system message?",
                List.of("user message = the end-user's actual request or question",
                        "system message = standing instructions/persona/rules/format for the whole conversation",
                        "use a system message to set behavior or output format, e.g. 'act as a strict interviewer, reply in JSON'"))
        )));
    }

    public GateModule get(String id) { return modules.get(id); }

    public Collection<GateModule> all() { return modules.values(); }
}
