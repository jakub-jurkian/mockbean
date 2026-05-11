package com.example.demo.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Active only for the gemini-flash profile.
 * Gemini is accessed via the OpenAI-compatible endpoint, so the bean created
 * by the OpenAI starter is reused here and promoted to @Primary.
 */
@Configuration
@Profile("gemini-flash")
public class GeminiModelPrimaryConfig {

    @Bean
    @Primary
    public ChatLanguageModel primaryChatModel(
            @Qualifier("openAiChatModel") ChatLanguageModel openAiChatModel) {
        return openAiChatModel;
    }
}
