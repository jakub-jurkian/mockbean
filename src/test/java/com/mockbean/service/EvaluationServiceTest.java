package com.mockbean.service;

import com.mockbean.ai.TechnicalInterviewer;
import com.mockbean.config.MockBeanProperties;
import com.mockbean.domain.EvaluationLog;
import com.mockbean.domain.EvaluationMode;
import com.mockbean.domain.EvaluationRequest;
import com.mockbean.domain.EvaluationResult;
import com.mockbean.repository.EvaluationLogRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    private static final String QUESTION = "What is the difference between wait() and sleep()?";
    private static final String ANSWER = "sleep() keeps the lock, wait() releases it.";
    private static final String REFERENCE = "## Question: " + QUESTION + "\nAnswer: sleep() keeps the lock; wait() releases it.";
    private static final String VERDICT_JSON = """
            {"score": 0.9, "strengths": ["locks"], "missedConcepts": [], "feedback": "Good."}""";
    private static final double MIN_SCORE = 0.80;

    @Mock private EmbeddingStore<TextSegment> embeddingStore;
    @Mock private EmbeddingModel embeddingModel;
    @Mock private TechnicalInterviewer technicalInterviewer;
    @Mock private EvaluationLogRepository logRepository;

    private EvaluationService service;

    @BeforeEach
    void setUp() {
        service = new EvaluationService(embeddingStore, embeddingModel, technicalInterviewer, logRepository,
                new MockBeanProperties("test-judge", MIN_SCORE));
    }

    @Test
    void ragRequest_WithRelevantReference_GradesAgainstItAndRecordsIt() {
        givenRetrievalReturns(List.of(new EmbeddingMatch<>(0.91, "id-1", null, TextSegment.from(REFERENCE))));
        when(technicalInterviewer.evaluate(QUESTION, REFERENCE, ANSWER)).thenReturn(VERDICT_JSON);

        EvaluationResult result = service.evaluateCandidate(new EvaluationRequest(QUESTION, ANSWER, "rag"));

        assertThat(result.mode()).isEqualTo(EvaluationMode.RAG);
        assertThat(result.retrievedReference()).isEqualTo(REFERENCE);
        assertThat(result.retrievalScore()).isEqualTo(0.91);
        assertThat(result.score()).isEqualTo(0.9);

        EvaluationLog saved = savedLog();
        assertThat(saved.getJudgeModel()).isEqualTo("test-judge");
        assertThat(saved.getRequestedMode()).isEqualTo("rag");
        assertThat(saved.getEffectiveMode()).isEqualTo("rag");
        assertThat(saved.getRetrievedReference()).isEqualTo(REFERENCE);
        assertThat(saved.getRawResponse()).isEqualTo(VERDICT_JSON);
    }

    @Test
    void ragRequest_UsesTheConfiguredMinimumScore() {
        givenRetrievalReturns(List.of());
        when(technicalInterviewer.evaluatePure(QUESTION, ANSWER)).thenReturn(VERDICT_JSON);

        service.evaluateCandidate(new EvaluationRequest(QUESTION, ANSWER, "rag"));

        verify(embeddingStore).findRelevant(any(Embedding.class), eq(1), eq(MIN_SCORE));
    }

    @Test
    void ragRequest_WithNoRelevantReference_FallsBackToPureMode() {
        givenRetrievalReturns(List.of());
        when(technicalInterviewer.evaluatePure(QUESTION, ANSWER)).thenReturn(VERDICT_JSON);

        EvaluationResult result = service.evaluateCandidate(new EvaluationRequest(QUESTION, ANSWER, "rag"));

        assertThat(result.mode()).isEqualTo(EvaluationMode.PURE_FALLBACK);
        assertThat(result.retrievedReference()).isNull();
        assertThat(result.retrievalScore()).isNull();
        verify(technicalInterviewer, never()).evaluate(anyString(), anyString(), anyString());

        EvaluationLog saved = savedLog();
        assertThat(saved.getRequestedMode()).isEqualTo("rag");
        assertThat(saved.getEffectiveMode()).isEqualTo("pure-fallback");
    }

    @Test
    void pureRequest_SkipsRetrievalEntirely() {
        when(technicalInterviewer.evaluatePure(QUESTION, ANSWER)).thenReturn(VERDICT_JSON);

        EvaluationResult result = service.evaluateCandidate(new EvaluationRequest(QUESTION, ANSWER, "pure"));

        assertThat(result.mode()).isEqualTo(EvaluationMode.PURE);
        verifyNoInteractions(embeddingModel, embeddingStore);
        assertThat(savedLog().getEffectiveMode()).isEqualTo("pure");
    }

    @Test
    void fencedJsonFromTheModel_IsStillParsed() {
        when(technicalInterviewer.evaluatePure(QUESTION, ANSWER))
                .thenReturn("Here is my verdict:\n```json\n" + VERDICT_JSON + "\n```");

        EvaluationResult result = service.evaluateCandidate(new EvaluationRequest(QUESTION, ANSWER, "pure"));

        assertThat(result.score()).isEqualTo(0.9);
        assertThat(result.feedback()).isEqualTo("Good.");
    }

    private void givenRetrievalReturns(List<EmbeddingMatch<TextSegment>> matches) {
        when(embeddingModel.embed(QUESTION)).thenReturn(Response.from(Embedding.from(new float[]{1f, 0f})));
        when(embeddingStore.findRelevant(any(Embedding.class), anyInt(), anyDouble())).thenReturn(matches);
    }

    private EvaluationLog savedLog() {
        ArgumentCaptor<EvaluationLog> captor = ArgumentCaptor.forClass(EvaluationLog.class);
        verify(logRepository).save(captor.capture());
        return captor.getValue();
    }
}
