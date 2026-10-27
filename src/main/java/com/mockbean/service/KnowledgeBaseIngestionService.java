package com.mockbean.service;

import com.mockbean.config.PgVectorProperties;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Rebuilds the pgvector knowledge base from {@code data/knowledge_base.md} on every start.
 *
 * <p>Rebuilding (instead of appending) means restarts never duplicate vectors and edits to the
 * file are always picked up. Disabled with {@code mockbean.ingestion.enabled=false}, e.g. in tests
 * that run without Ollama.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mockbean.ingestion.enabled", havingValue = "true", matchIfMissing = true)
public class KnowledgeBaseIngestionService implements CommandLineRunner {

    private static final Path KNOWLEDGE_BASE = Path.of("data/knowledge_base.md");
    private static final Pattern SAFE_TABLE_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;
    private final PgVectorProperties pgVectorProperties;

    @Override
    public void run(String... args) throws IOException {
        if (!Files.exists(KNOWLEDGE_BASE)) {
            log.warn("Knowledge base file not found at {}. Skipping ingestion.", KNOWLEDGE_BASE);
            return;
        }

        List<TextSegment> pairs = KnowledgeBaseParser.parse(Files.readString(KNOWLEDGE_BASE));
        log.info("Embedding {} question-and-answer pairs...", pairs.size());

        // Embed before touching the table, so an unreachable Ollama leaves the old data in place.
        List<Embedding> embeddings = embeddingModel.embedAll(pairs).content();

        jdbcTemplate.execute("TRUNCATE TABLE " + embeddingsTable());
        embeddingStore.addAll(embeddings, pairs);
        log.info("Knowledge base rebuilt: {} pairs stored in pgvector.", pairs.size());
    }

    // The table name comes from configuration, but it is concatenated into SQL, so validate it.
    private String embeddingsTable() {
        String table = pgVectorProperties.table();
        if (table == null || !SAFE_TABLE_NAME.matcher(table).matches()) {
            throw new IllegalStateException("Invalid pgvector table name: " + table);
        }
        return table;
    }
}
