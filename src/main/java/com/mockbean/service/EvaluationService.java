package com.mockbean.service;

import com.mockbean.ai.TechnicalInterviewer;
import com.mockbean.config.MockBeanProperties;
import com.mockbean.domain.EvaluationLog;
import com.mockbean.domain.EvaluationMode;
import com.mockbean.domain.EvaluationRequest;
import com.mockbean.domain.EvaluationResponse;
import com.mockbean.domain.EvaluationResult;
import com.mockbean.repository.EvaluationLogRepository;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final TechnicalInterviewer technicalInterviewer;
    private final EvaluationLogRepository logRepository;
    private final MockBeanProperties properties;
    private final Gson gson = new Gson();

    public EvaluationResult evaluateCandidate(EvaluationRequest request) {
        log.info("Starting evaluation process for question: {}", request.question());

        Optional<EmbeddingMatch<TextSegment>> reference = request.isPurePrompt()
                ? Optional.empty()
                : retrieveReference(request.question());

        EvaluationMode mode;
        String rawResponse;

        if (request.isPurePrompt()) {
            mode = EvaluationMode.PURE;
            rawResponse = technicalInterviewer.evaluatePure(request.question(), request.userAnswer());
        } else if (reference.isPresent()) {
            mode = EvaluationMode.RAG;
            rawResponse = technicalInterviewer.evaluate(
                    request.question(),
                    reference.get().embedded().text(),
                    request.userAnswer()
            );
        } else {
            // Grading against an unrelated reference is worse than grading without one.
            mode = EvaluationMode.PURE_FALLBACK;
            rawResponse = technicalInterviewer.evaluatePure(request.question(), request.userAnswer());
        }

        log.debug("Raw LLM response: {}", rawResponse);
        EvaluationResponse verdict = parseResponse(rawResponse);

        Double retrievalScore = reference.map(EmbeddingMatch::score).orElse(null);
        String retrievedReference = reference.map(match -> match.embedded().text()).orElse(null);

        saveEvaluationLog(request, verdict, mode, retrievalScore, retrievedReference, rawResponse);

        log.info("Evaluation completed: judge={}, mode={}, score={}",
                properties.judgeModel(), mode.value(), verdict.score());
        return EvaluationResult.of(verdict, properties.judgeModel(), mode, retrievalScore, retrievedReference);
    }

    /**
     * Returns the closest knowledge-base pair, or empty if even the best match is below
     * {@code mockbean.rag-min-score}.
     */
    private Optional<EmbeddingMatch<TextSegment>> retrieveReference(String question) {
        Embedding queryEmbedding = embeddingModel.embed(question).content();
        List<EmbeddingMatch<TextSegment>> matches =
                embeddingStore.findRelevant(queryEmbedding, 1, properties.ragMinScore());

        if (matches.isEmpty() || matches.getFirst().embedded() == null) {
            log.warn("No reference with relevance >= {}; falling back to Pure mode.", properties.ragMinScore());
            return Optional.empty();
        }

        EmbeddingMatch<TextSegment> best = matches.getFirst();
        log.info("Retrieved reference (relevance {}): {}",
                String.format("%.3f", best.score()), firstLine(best.embedded().text()));
        return Optional.of(best);
    }

    private static String firstLine(String text) {
        int newline = text.indexOf('\n');
        return newline == -1 ? text : text.substring(0, newline);
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

    private void saveEvaluationLog(EvaluationRequest request, EvaluationResponse verdict, EvaluationMode mode,
                                   Double retrievalScore, String retrievedReference, String rawResponse) {
        EvaluationLog logEntry = EvaluationLog.builder()
                .question(request.question())
                .userAnswer(request.userAnswer())
                .score(verdict.score())
                .feedback(verdict.feedback())
                .strengths(verdict.strengths())
                .missedConcepts(verdict.missedConcepts())
                .judgeModel(properties.judgeModel())
                .requestedMode(request.isPurePrompt() ? EvaluationMode.PURE.value() : EvaluationMode.RAG.value())
                .effectiveMode(mode.value())
                .retrievalScore(retrievalScore)
                .retrievedReference(retrievedReference)
                .rawResponse(rawResponse)
                .build();

        logRepository.save(logEntry);
        log.debug("Saved evaluation log entry with ID: {}", logEntry.getId());
    }
}
