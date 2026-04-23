package com.example.demo.service;

import com.example.demo.ai.TechnicalInterviewer;
import com.example.demo.domain.EvaluationRequest;
import com.example.demo.domain.EvaluationResponse;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final TechnicalInterviewer technicalInterviewer;

    public EvaluationResponse evaluateCandidate(EvaluationRequest request) {
        log.info("Starting evaluation process for question: {}", request.question());

        // Convert the question into a mathematical vector to search the database
        Embedding queryEmbedding = embeddingModel.embed(request.question()).content();

        // Perform Similarity Search in pgvector (fetch Top 1 result)
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, 1);

        String idealAnswer = "No reference pattern found in the database.";
        if (!matches.isEmpty()) {
            idealAnswer = matches.getFirst().embedded().text();
            log.info("Found matching reference context with score: {}", matches.getFirst().score());
        } else {
            log.warn("Warning: Proceeding with evaluation without reference context.");
        }

        // Send everything to the LLM Judge
        log.info("Sending prompt to the LLM Judge...");
        EvaluationResponse response = technicalInterviewer.evaluate(
                request.question(),
                idealAnswer,
                request.userAnswer()
        );

        log.info("Evaluation completed successfully. Final score: {}", response.score());
        return response;
    }
}
