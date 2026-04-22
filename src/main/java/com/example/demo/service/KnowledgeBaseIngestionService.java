package com.example.demo.service;

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import org.springframework.boot.CommandLineRunner;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.data.document.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseIngestionService implements CommandLineRunner {

    // Spring injects the Vector Database connection
    private final EmbeddingStore<TextSegment> embeddingStore;
    // Spring injects the nomic-embed-text model
    private final EmbeddingModel embeddingModel;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Knowledge Base ingestion process...");

        // Locate the file
        Path documentPath = Paths.get("data/knowledge_base.md");
        if (!documentPath.toFile().exists()) {
            log.warn("Knowledge base file not found at {}. Skipping ingestion.", documentPath);
            return;
        }

        // Load the document
        Document document = FileSystemDocumentLoader.loadDocument(documentPath, new TextDocumentParser());

        // Configure the Ingestor
        // Split the document into chunks. Using a simple regex to split by "## Question:"
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(
                        300, // max segment size (chars)
                        0    // overlap
                ))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        // Ingest (Process and Save to Database)
        log.info("Embedding and saving documents to pgvector. This might take a moment...");
        ingestor.ingest(document);

        log.info("Ingestion completed successfully!");
    }
}
