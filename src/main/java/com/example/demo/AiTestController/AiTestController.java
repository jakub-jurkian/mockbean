package com.example.demo.AiTestController;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class AiTestController {
    // Spring will inject there the connection with Ollama thanks to settings from application.yml
    private final ChatLanguageModel chatLanguageModel;

    @GetMapping("/ask")
    public ResponseEntity<String> askAi(@RequestParam String question) {
        log.info("Wysyłam pytanie do lokalnego modelu: {}", question);
        // Now Java will block n wait for generated text from Ollama
        String response = chatLanguageModel.generate(question);
        log.info("Model responded correctly.");
        return ResponseEntity.ok(response);
    }
}
