package com.example.demo.service;

import com.example.demo.ai.TechnicalInterviewer;
import com.example.demo.domain.EvaluationLog;
import com.example.demo.domain.EvaluationRequest;
import com.example.demo.domain.EvaluationResponse;
import com.example.demo.repository.EvaluationLogRepository;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
    private final EvaluationLogRepository logRepository;
    private final Gson gson = new Gson();

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
        String rawResponse = technicalInterviewer.evaluate(
                request.question(),
                idealAnswer,
                request.userAnswer()
        );

        log.debug("Raw LLM response: {}", rawResponse);
        EvaluationResponse response = parseResponse(rawResponse);

        saveEvaluationLog(request, response);

        log.info("Evaluation completed successfully. Final score: {}", response.score());
        return response;
    }

    /**
     * Strips markdown code fences (```json ... ```) and any leading/trailing text
     * that Claude or other models sometimes add around the JSON object.
     */
    private EvaluationResponse parseResponse(String raw) {
        String cleaned = raw.strip();

        // Remove ```json ... ``` or ``` ... ``` wrappers
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            int lastFence    = cleaned.lastIndexOf("```");
            if (firstNewline != -1 && lastFence > firstNewline) {
                cleaned = cleaned.substring(firstNewline + 1, lastFence).strip();
            }
        }

        // Extract the JSON object: find first '{' and last '}'
        int start = cleaned.indexOf('{');
        int end   = cleaned.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }

        try {
            return gson.fromJson(cleaned, EvaluationResponse.class);
        } catch (JsonSyntaxException e) {
            log.error("Failed to parse LLM response after cleanup. Raw was: {}", raw);
            throw e;
        }
    }

    private void saveEvaluationLog(EvaluationRequest request, EvaluationResponse response) {
        EvaluationLog logEntry = EvaluationLog.builder()
                .question(request.question())
                .userAnswer(request.userAnswer())
                .score(response.score())
                .feedback(response.feedback())
                .strengths(response.strengths())
                .missedConcepts(response.missedConcepts())
                .build();

        logRepository.save(logEntry);
        log.debug("Saved evaluation log entry with ID: {}", logEntry.getId());
    }
}
