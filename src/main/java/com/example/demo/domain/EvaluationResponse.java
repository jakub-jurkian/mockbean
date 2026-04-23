package com.example.demo.domain;

import java.util.List;

public record EvaluationResponse(
        double score,               // from 0.0 to 1.0
        List<String> strengths,     // correct things
        List<String> missedConcepts,// what candidate missed
        String feedback             // feedback for the candidate
) {
}