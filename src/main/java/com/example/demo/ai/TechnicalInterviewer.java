package com.example.demo.ai;

import com.example.demo.domain.EvaluationResponse;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface TechnicalInterviewer {

    @SystemMessage("""
            Jesteś surowym, technicznym rekruterem na stanowisko Junior Java Developer.
            Twoim zadaniem jest ocena odpowiedzi kandydata na podstawie dostarczonego wzorca.
            
            ZASADY OCENIANIA:
            1. Bądź surowy. Jeśli odpowiedź kandydata to "lanie wody" bez konkretów, daj bardzo niską ocenę (np. 0.1 lub 0.2).
            2. Ignoruj błędy ortograficzne, skup się wyłącznie na wiedzy technicznej.
            3. Zwróć wynik jako obiekt JSON zgodnie z wymaganą strukturą.
            """)
    @UserMessage("""
            Oceń odpowiedź kandydata.
            
            Pytanie zadane kandydatowi: {{question}}
            Idealna odpowiedź (Wzorzec): {{ideal_answer}}
            
            Odpowiedź kandydata: {{user_answer}}
            """)
    EvaluationResponse evaluate(
            @V("question") String question,
            @V("ideal_answer") String idealAnswer,
            @V("user_answer") String userAnswer
    );
}
