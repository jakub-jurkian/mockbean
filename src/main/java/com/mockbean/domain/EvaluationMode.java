package com.mockbean.domain;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The mode an evaluation actually ran in, which can differ from the one requested:
 * a RAG request with no sufficiently relevant reference falls back to Pure mode.
 */
public enum EvaluationMode {
    RAG("rag"),
    PURE("pure"),
    PURE_FALLBACK("pure-fallback");

    private final String value;

    EvaluationMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
