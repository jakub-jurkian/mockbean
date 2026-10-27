package com.mockbean.domain;

import java.util.List;

/**
 * The API response: the judge's verdict plus how it was produced, so every evaluation can be
 * audited - which model judged it, which mode actually ran, and the exact reference it saw.
 */
public record EvaluationResult(
        double score,
        List<String> strengths,
        List<String> missedConcepts,
        String feedback,
        String judgeModel,
        EvaluationMode mode,
        Double retrievalScore,      // null unless a reference was retrieved
        String retrievedReference   // null unless a reference was retrieved
) {
    public static EvaluationResult of(EvaluationResponse verdict, String judgeModel, EvaluationMode mode,
                                      Double retrievalScore, String retrievedReference) {
        return new EvaluationResult(
                verdict.score(),
                verdict.strengths(),
                verdict.missedConcepts(),
                verdict.feedback(),
                judgeModel,
                mode,
                retrievalScore,
                retrievedReference
        );
    }
}
