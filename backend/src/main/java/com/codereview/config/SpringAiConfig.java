package com.codereview.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI Configuration
 * Skills: Server Side, RESTful API, Spring Boot
 *
 * Exposes a ChatClient bean built from the auto-configured ChatClient.Builder.
 * Spring AI's OpenAI starter auto-configures ChatClient.Builder from
 * spring.ai.openai.* properties â€” we just build the final client here.
 */
@Configuration
public class SpringAiConfig {

    /**
     * ChatClient â€” the primary Spring AI abstraction for interacting with
     * language models. Wired with default options from application.yml
     * (model gpt-4o, temperature 0.3, max-tokens 1500).
     *
     * Injected into AIService via constructor injection.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("You are an expert code reviewer. Return only valid JSON arrays as instructed.")
                .build();
    }
}
