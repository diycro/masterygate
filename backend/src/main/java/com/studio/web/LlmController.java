package com.studio.web;

import org.springframework.ai.chat.client.ChatClient;
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
 *  The scaffolding is done for you. Your job (the part that teaches you):
 *    1. Set your LLM key so a ChatClient bean exists (see README).
 *    2. Implement the TODO below — call the model and return its reply.
 *    3. Then answer your tutor's Module 0 gate questions, e.g.:
 *         - What is a token? How would you find this call's token usage?
 *         - Why can the SAME prompt return different text?
 *         - When would you choose Java (Spring AI) vs Python for this?
 *
 *  ChatClient basics:  chatClient.prompt().user(prompt).call().content()
 *  Token usage:        ...call().chatResponse().getMetadata().getUsage()
 * ============================================================================
 */
@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final ObjectProvider<ChatClient> chatClientProvider;

    public LlmController(ObjectProvider<ChatClient> chatClientProvider) {
        this.chatClientProvider = chatClientProvider;
    }

    @GetMapping("/hello")
    public Map<String, Object> hello(
            @RequestParam(defaultValue = "Say hello from Spring AI in one short sentence.") String prompt) {

        ChatClient chatClient = chatClientProvider.getIfAvailable();

        Map<String, Object> body = new LinkedHashMap<>();
        if (chatClient == null) {
            body.put("enabled", false);
            body.put("hint", "Set OPENAI_API_KEY (or your provider key) and restart to enable the model.");
            return body;
        }

        // TODO(Module 0): replace this placeholder with a real call, e.g.:
        //     String answer = chatClient.prompt().user(prompt).call().content();
        // Bonus: also return the token usage from the response metadata.
        String answer = "TODO: implement the ChatClient call (Module 0 exercise).";

        body.put("enabled", true);
        body.put("prompt", prompt);
        body.put("answer", answer);
        // body.put("tokens", ...);  // <- add token usage once you wire the call
        return body;
    }
}
