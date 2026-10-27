package com.mockbean.ai;

import com.mockbean.domain.EvaluationRequest;
import com.mockbean.domain.EvaluationResult;
import com.mockbean.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/evaluations")
@RequiredArgsConstructor
@Tag(name = "Evaluations", description = "LLM-as-a-Judge: evaluate candidate answers against the knowledge base")
public class EvaluationController {
    private final EvaluationService evaluationService;

    @PostMapping
    @Operation(
            summary = "Evaluate a candidate answer",
            description = "RAG mode (default) embeds the question and retrieves the closest question-and-answer pair " +
                          "from pgvector; if no pair is relevant enough it falls back to Pure mode. The configured LLM " +
                          "then scores the answer. The response includes the judge model, the mode that actually ran " +
                          "and the retrieved reference."
    )
    public ResponseEntity<EvaluationResult> evaluate(@RequestBody EvaluationRequest request) {
        log.info("Received new POST request at /api/v1/evaluations");
        EvaluationResult response = evaluationService.evaluateCandidate(request);
        return ResponseEntity.ok(response);
    }
}
