package com.example.demo.ai;

import com.example.demo.domain.EvaluationResponse;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface TechnicalInterviewer {

    @SystemMessage("""
            You are a strict technical interviewer evaluating candidates for a Junior Java Developer position.
            Your task is to assess the candidate's answer based on the provided reference answer.

            SCORING RULES:
            1. Be strict. If the candidate's answer is vague or contains no concrete technical details, assign a very low score (e.g. 0.1 or 0.2).
            2. Ignore spelling mistakes — focus solely on technical correctness and depth.
            3. Return the result as a JSON object matching the required structure.
            """)
    @UserMessage("""
            Evaluate the candidate's answer.

            Question asked to the candidate: {{question}}
            Reference answer (ideal): {{ideal_answer}}

            Candidate's answer: {{user_answer}}
            """)
    EvaluationResponse evaluate(
            @V("question") String question,
            @V("ideal_answer") String idealAnswer,
            @V("user_answer") String userAnswer
    );
}
