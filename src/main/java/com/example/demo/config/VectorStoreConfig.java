package com.example.demo.config;

import com.example.demo.ai.TechnicalInterviewer;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(PgVectorProperties.class)
public class VectorStoreConfig {

    private final PgVectorProperties pgVectorProperties;

    @Bean
    public EmbeddingStore<TextSegment> pgVectorStore() {
        return PgVectorEmbeddingStore.builder()
                .host(pgVectorProperties.host())
                .port(pgVectorProperties.port())
                .database(pgVectorProperties.database())
                .user(pgVectorProperties.user())
                .password(pgVectorProperties.password())
                .table(pgVectorProperties.table())
                .dimension(pgVectorProperties.dimension())
                .build();
    }

    @Bean
    public TechnicalInterviewer technicalInterviewer(ChatLanguageModel chatLanguageModel) {
        log.info(">>> LLM Judge wired to: {}", chatLanguageModel.getClass().getSimpleName());
        return AiServices.builder(TechnicalInterviewer.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }
}