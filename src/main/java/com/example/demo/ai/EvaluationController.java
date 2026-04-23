package com.example.demo.ai;

import com.example.demo.domain.EvaluationRequest;
import com.example.demo.domain.EvaluationResponse;
import com.example.demo.service.EvaluationService;
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
public class EvaluationController {
    private final EvaluationService evaluationService;

    @PostMapping
    public ResponseEntity<EvaluationResponse> evaluate(@RequestBody EvaluationRequest request) {
        log.info("Received new POST request at /api/v1/evaluations");
        EvaluationResponse response = evaluationService.evaluateCandidate(request);
        return ResponseEntity.ok(response);
    }
}
