package com.mockbean.config;

import com.mockbean.domain.ErrorResponse;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bean validation failed (e.g. @NotBlank on EvaluationRequest fields)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> "'" + fe.getField() + "' " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed for request [{}]: {}", request.getRequestURI(), message);
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    // 2. Malformed JSON body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Malformed JSON received at [{}]", request.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request body.", request);
    }

    // 3. LLM returned a non-JSON response (Gson failed to parse it)
    @ExceptionHandler(JsonSyntaxException.class)
    public ResponseEntity<ErrorResponse> handleJsonSyntax(
            JsonSyntaxException ex, HttpServletRequest request) {

        log.error("LLM returned an unparseable response at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_GATEWAY,
                "The LLM returned an unexpected response format. Try again or check Ollama logs.", request);
    }

    // 4. Upstream service unavailable (Ollama down, pgvector unreachable, etc.)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(
            RuntimeException ex, HttpServletRequest request) {

        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        boolean isConnectionIssue = msg.contains("connection refused")
                || msg.contains("connect timed out")
                || msg.contains("failed to connect")
                || msg.contains("unable to obtain connection");

        if (isConnectionIssue) {
            log.error("Upstream service unreachable at [{}]: {}", request.getRequestURI(), ex.getMessage());
            return build(HttpStatus.SERVICE_UNAVAILABLE,
                    "A required service (LLM or database) is currently unavailable. " +
                    "Ensure Ollama and PostgreSQL are running.", request);
        }

        log.error("Unhandled RuntimeException at [{}]", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request);
    }

    // 5. Catch-all fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception at [{}]", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(body);
    }
}
