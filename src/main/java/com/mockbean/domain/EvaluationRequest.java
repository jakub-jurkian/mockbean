package com.mockbean.domain;

public record EvaluationRequest(
        String question,
        String userAnswer,
        /**
         * Evaluation mode: "rag" (default) uses pgvector retrieval to supply
         * a reference answer; "pure" skips retrieval and lets the LLM judge
         * purely from its own knowledge.
         */
        String mode
) {
    public boolean isPurePrompt() {
        return "pure".equalsIgnoreCase(mode);
    }
}
