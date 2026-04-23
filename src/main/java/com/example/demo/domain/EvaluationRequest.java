package com.example.demo.domain;

public record EvaluationRequest(
        String question,
        String userAnswer
) {
}
