package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Map;

public class StoryParserTest {
    @Test
    void findingSpecifiedStates_YieldsCorrectDates() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try (InputStream inputSteam = getClass().getClassLoader().getResourceAsStream("sampleStory.json")) {
            root = mapper.readTree(inputSteam);
        }

        StoryParser parser = new StoryParser(root, "Ready for Build, Build, Done");
        Map<String, LocalDate> history = parser.findStateChangeDates();

        assertThat(history.get("Ready for Build")).isEqualTo("2025-04-03");
        assertThat(history.get("Build")).isEqualTo("2025-06-01");
        assertThat(history.get("Done")).isEqualTo("2025-06-10");
    }

    @Test
    void findingStoryEstimate_YieldsCorrectEstimate() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try (InputStream inputSteam = getClass().getClassLoader().getResourceAsStream("sampleStory.json")) {
            root = mapper.readTree(inputSteam);
        }

        StoryParser parser = new StoryParser(root, "Ready for Build, Build, Done");
        float estimate = parser.findStoryEstimate();

        assertThat(estimate).isEqualTo(5.0f);
    }

    @Test
    void findingStoryEstimate_WhenNoEstimate_ReturnsNull() throws IOException {
        JsonNode root = JsonNodeFactory.instance.objectNode();

        StoryParser parser = new StoryParser(root, "Ready for Build, Build, Done");
        Float estimate = parser.findStoryEstimate();

        assertThat(estimate).isEqualTo(null);
    }
}
