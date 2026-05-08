package com.example.demo.ai;

import com.example.demo.domain.EvaluationRequest;
import com.example.demo.domain.EvaluationResponse;
import com.example.demo.service.EvaluationService;
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
            description = "Embeds the question, retrieves the closest reference answer from pgvector, " +
                          "then prompts the configured LLM to score the candidate's answer and return structured feedback."
    )
    public ResponseEntity<EvaluationResponse> evaluate(@RequestBody EvaluationRequest request) {
        log.info("Received new POST request at /api/v1/evaluations");
        EvaluationResponse response = evaluationService.evaluateCandidate(request);
        return ResponseEntity.ok(response);
    }
}
