package com.mockbean.service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits the knowledge base into one segment per question-and-answer pair.
 *
 * <p>A fixed-length splitter cut most pairs apart: the chunk holding the question often had no
 * answer at all, so RAG retrieved the bare question as the "reference answer". Keeping each pair
 * whole guarantees the judge always receives a complete reference. {@code # Topic:} lines are not
 * part of any segment; they label the pairs that follow.
 */
public final class KnowledgeBaseParser {

    static final String QUESTION_PREFIX = "## Question:";
    static final String TOPIC_PREFIX = "# Topic:";
    static final String TOPIC_METADATA_KEY = "topic";
    private static final String DEFAULT_TOPIC = "General";

    private KnowledgeBaseParser() {
    }

    public static List<TextSegment> parse(String markdown) {
        List<TextSegment> segments = new ArrayList<>();
        String topic = DEFAULT_TOPIC;
        String pairTopic = DEFAULT_TOPIC;
        StringBuilder pair = null;

        for (String rawLine : markdown.split("\\R")) {
            String line = rawLine.strip();

            if (line.startsWith(TOPIC_PREFIX)) {
                addPair(segments, pair, pairTopic);
                pair = null;
                topic = line.substring(TOPIC_PREFIX.length()).strip();
            } else if (line.startsWith(QUESTION_PREFIX)) {
                addPair(segments, pair, pairTopic);
                pair = new StringBuilder(line);
                pairTopic = topic;
            } else if (pair != null && !line.isEmpty()) {
                pair.append('\n').append(line);
            }
        }
        addPair(segments, pair, pairTopic);

        return segments;
    }

    private static void addPair(List<TextSegment> segments, StringBuilder pair, String topic) {
        if (pair != null) {
            segments.add(TextSegment.from(pair.toString(), Metadata.from(TOPIC_METADATA_KEY, topic)));
        }
    }
}
