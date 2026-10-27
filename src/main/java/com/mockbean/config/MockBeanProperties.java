package com.mockbean.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application settings under the {@code mockbean} prefix.
 *
 * @param judgeModel  label of the active judge model, stored with every evaluation
 *                    so results from different profiles can be told apart
 * @param ragMinScore minimum pgvector relevance score (0-1) for a retrieved reference;
 *                    below it the evaluation falls back to Pure mode instead of grading
 *                    against an unrelated answer
 */
@ConfigurationProperties(prefix = "mockbean")
public record MockBeanProperties(
        String judgeModel,
        double ragMinScore
) {}
