package com.mockbean.service;

import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeBaseParserTest {

    @Test
    void parse_KeepsEveryQuestionWithItsWholeAnswer() {
        String markdown = """
                # Topic: Core Java
                ## Question: What is the difference between wait() and sleep()?
                Answer: sleep() keeps the lock.
                wait() releases the lock.

                ## Question: What is a record?
                Answer: An immutable data carrier.
                # Topic: Spring
                ## Question: What is a bean?
                Answer: An object managed by the container.
                """;

        List<TextSegment> pairs = KnowledgeBaseParser.parse(markdown);

        assertThat(pairs).hasSize(3);
        assertThat(pairs.get(0).text()).isEqualTo("""
                ## Question: What is the difference between wait() and sleep()?
                Answer: sleep() keeps the lock.
                wait() releases the lock.""");
        assertThat(pairs.get(1).text()).doesNotContain("# Topic");
        assertThat(topicOf(pairs.get(0))).isEqualTo("Core Java");
        assertThat(topicOf(pairs.get(1))).isEqualTo("Core Java");
        assertThat(topicOf(pairs.get(2))).isEqualTo("Spring");
    }

    @Test
    void parse_IgnoresTextBeforeTheFirstQuestion() {
        List<TextSegment> pairs = KnowledgeBaseParser.parse("""
                Intro text that belongs to no question.
                ## Question: Q?
                Answer: A.
                """);

        assertThat(pairs).singleElement()
                .extracting(TextSegment::text)
                .isEqualTo("## Question: Q?\nAnswer: A.");
    }

    @Test
    void parse_RealKnowledgeBase_ProducesOneCompleteSegmentPerPair() throws IOException {
        String markdown = Files.readString(Path.of("data/knowledge_base.md"));
        long questionCount = markdown.lines()
                .filter(line -> line.strip().startsWith(KnowledgeBaseParser.QUESTION_PREFIX))
                .count();

        List<TextSegment> pairs = KnowledgeBaseParser.parse(markdown);

        assertThat(pairs).hasSize((int) questionCount);
        assertThat(pairs).allSatisfy(pair -> {
            assertThat(pair.text()).startsWith(KnowledgeBaseParser.QUESTION_PREFIX);
            assertThat(pair.text()).contains("Answer:");
            assertThat(pair.text()).doesNotContain(KnowledgeBaseParser.TOPIC_PREFIX);
            assertThat(topicOf(pair)).isNotBlank();
        });
    }

    private static String topicOf(TextSegment segment) {
        return segment.metadata().get(KnowledgeBaseParser.TOPIC_METADATA_KEY);
    }
}
