package com.mockbean.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Active when no external model profile is selected
 * (i.e. default local Ollama run). Marks the Ollama chat model as @Primary
 * so Spring knows which ChatLanguageModel to inject when multiple starters
 * are on the classpath.
 */
@Configuration
@Profile("!claude-sonnet-4-6 & !gemini-flash")
public class OllamaModelPrimaryConfig {

    @Bean
    @Primary
    public ChatLanguageModel primaryChatModel(
            @Qualifier("ollamaChatModel") ChatLanguageModel ollamaChatModel) {
        return ollamaChatModel;
    }
}
