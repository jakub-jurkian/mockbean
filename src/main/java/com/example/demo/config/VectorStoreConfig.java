package com.example.demo.config;

import com.example.demo.ai.TechnicalInterviewer;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

    @Bean
    public EmbeddingStore<TextSegment> pgVectorStore() {
        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5432)
                .database("mockbean_db")
                .user("mockbean_user")
                .password("mockbean_password")
                .table("embeddings")
                .dimension(768)
                .build();
    }

    @Bean
    public TechnicalInterviewer technicalInterviewer(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(TechnicalInterviewer.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }
}