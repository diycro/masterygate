package com.studio.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ============================================================================
 *  MODULE 0 EXERCISE — "Hello LLM" in Java (Spring AI)
 * ============================================================================
 *  Spring AI auto-configures a {@link ChatModel} bean when you set an API key
 *  (spring.ai.openai.api-key). We inject it via ObjectProvider so the app STILL
 *  boots with no key (the endpoint just reports enabled:false) — BYOK-friendly.
 *
 *  The learning line is the ChatClient call below:
 *    chatClient.prompt().user(prompt).call().content()
 *  That's the Java equivalent of Python's create(...) + choices[0].message.content.
 * ============================================================================
 */
@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final ObjectProvider<ChatModel> chatModelProvider;

    public LlmController(ObjectProvider<ChatModel> chatModelProvider) {
        this.chatModelProvider = chatModelProvider;
    }

    @GetMapping("/hello")
    public Map<String, Object> hello(
            @RequestParam(defaultValue = "Say hello from Spring AI in one short sentence.") String prompt) {

        Map<String, Object> body = new LinkedHashMap<>();

        // Resolved lazily at request time, so the model bean is fully registered by now.
        ChatModel model = chatModelProvider.getIfAvailable();
        if (model == null) {
            body.put("enabled", false);
            body.put("hint", "No LLM model configured. Set GROQ_API_KEY (or your provider key) and "
                    + "check spring.ai.openai in application.yml, then restart.");
            return body;
        }

        try {
            String answer = ChatClient.create(model)
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
            body.put("enabled", true);
            body.put("prompt", prompt);
            body.put("answer", answer);
        } catch (Exception e) {
            // Model is wired but the call failed — usually a bad/absent key (401) or wrong model/base-url (404).
            body.put("enabled", true);
            body.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            body.put("hint", "The model is wired but the call failed — check your API key, model name, and base-url.");
        }
        return body;
    }
}
