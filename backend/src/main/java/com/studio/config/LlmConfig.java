package com.studio.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires a {@link ChatClient} — but ONLY when a {@link ChatModel} exists, which happens
 * once you configure an LLM API key. This means the app boots fine with no key
 * (health check works); the /api/llm/hello endpoint just reports "not enabled" until
 * you add your key. That keeps the LLM layer BYOK-ready (see the plan).
 */
@Configuration
public class LlmConfig {

    @Bean
    @ConditionalOnBean(ChatModel.class)
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}
