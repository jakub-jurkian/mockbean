package com.mockbean.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks retrieval against a real pgvector database, with hand-made vectors so no embedding
 * model is needed. pgvector's relevance is (1 + cosine) / 2, which is what mockbean.rag-min-score
 * is measured in.
 */
@Testcontainers
class PgVectorRetrievalIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"));

    private static final double MIN_SCORE = 0.80;

    private static EmbeddingStore<TextSegment> store;

    @BeforeAll
    static void createStore() {
        store = PgVectorEmbeddingStore.builder()
                .host(POSTGRES.getHost())
                .port(POSTGRES.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT))
                .database(POSTGRES.getDatabaseName())
                .user(POSTGRES.getUsername())
                .password(POSTGRES.getPassword())
                .table("embeddings_test")
                .dimension(3)
                .build();

        store.addAll(
                List.of(vector(1, 0, 0), vector(0, 1, 0)),
                List.of(TextSegment.from("Java pair"), TextSegment.from("Spring pair")));
    }

    @Test
    void closeQuery_ReturnsTheNearestPairAboveTheThreshold() {
        List<EmbeddingMatch<TextSegment>> matches = store.findRelevant(vector(1, 0.1f, 0), 1, MIN_SCORE);

        assertThat(matches).singleElement().satisfies(match -> {
            assertThat(match.embedded().text()).isEqualTo("Java pair");
            assertThat(match.score()).isGreaterThan(MIN_SCORE);
        });
    }

    @Test
    void unrelatedQuery_ReturnsNothing_SoTheServiceFallsBackToPureMode() {
        // Orthogonal to both stored vectors: cosine 0, relevance 0.5.
        List<EmbeddingMatch<TextSegment>> matches = store.findRelevant(vector(0, 0, 1), 1, MIN_SCORE);

        assertThat(matches).isEmpty();
    }

    private static Embedding vector(float x, float y, float z) {
        return Embedding.from(new float[]{x, y, z});
    }
}
