package com.mockbean.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface TechnicalInterviewer {

    // ── RAG mode: judge has a retrieved reference answer ──────────────────────

    @SystemMessage("""
            You are a strict technical interviewer evaluating candidates for a Junior Java Developer position.
            Your task is to assess the candidate's answer based on the provided reference answer.

            SCORING RULES:
            1. Be strict. If the candidate's answer is vague or contains no concrete technical details, assign a very low score (e.g. 0.1 or 0.2).
            2. Ignore spelling mistakes — focus solely on technical correctness and depth.
            3. You MUST respond with ONLY a valid JSON object — no markdown, no code fences, no explanation before or after.

            The JSON object must have exactly these four fields:
            {
              "score": <number between 0.0 and 1.0>,
              "strengths": [<string>, ...],
              "missedConcepts": [<string>, ...],
              "feedback": "<string>"
            }
            """)
    @UserMessage("""
            Evaluate the candidate's answer.

            Question asked to the candidate: {{question}}
            Reference answer (ideal): {{ideal_answer}}

            Candidate's answer: {{user_answer}}
            """)
    String evaluate(
            @V("question") String question,
            @V("ideal_answer") String idealAnswer,
            @V("user_answer") String userAnswer
    );

    // ── Pure Prompt mode: judge relies solely on its own knowledge ─────────────

    @SystemMessage("""
            You are a strict technical interviewer evaluating candidates for a Junior Java Developer position.
            Use your own knowledge of Java to assess the correctness and depth of the candidate's answer.
            No reference answer is provided — rely entirely on your expertise.

            SCORING RULES:
            1. Be strict. If the candidate's answer is vague or contains no concrete technical details, assign a very low score (e.g. 0.1 or 0.2).
            2. Ignore spelling mistakes — focus solely on technical correctness and depth.
            3. You MUST respond with ONLY a valid JSON object — no markdown, no code fences, no explanation before or after.

            The JSON object must have exactly these four fields:
            {
              "score": <number between 0.0 and 1.0>,
              "strengths": [<string>, ...],
              "missedConcepts": [<string>, ...],
              "feedback": "<string>"
            }
            """)
    @UserMessage("""
            Evaluate the candidate's answer using your own Java knowledge.

            Question asked to the candidate: {{question}}

            Candidate's answer: {{user_answer}}
            """)
    String evaluatePure(
            @V("question") String question,
            @V("user_answer") String userAnswer
    );
}
