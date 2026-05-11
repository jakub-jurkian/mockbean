package com.example.demo.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Active only for the claude-haiku profile.
 * Marks the Anthropic chat model as @Primary so it wins over the Ollama
 * chat model when Spring resolves the ChatLanguageModel injection point.
 */
@Configuration
@Profile("claude-haiku")
public class AnthropicModelPrimaryConfig {

    @Bean
    @Primary
    public ChatLanguageModel primaryChatModel(
            @Qualifier("anthropicChatModel") ChatLanguageModel anthropicChatModel) {
        return anthropicChatModel;
    }
}
