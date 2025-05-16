package com.fedex.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;

public class StoryParserTest {
    @Test
    void findingSpecifiedStates_YieldsCorrectDates() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try (InputStream inputSteam = getClass().getClassLoader().getResourceAsStream("sampleStory.json")) {
            root = mapper.readTree(inputSteam);
        }

        StoryParser parser = new StoryParser(root);
        StoryHistory history = parser.findHistory("");

        assertThat(history.readyForBuild()).isEqualTo("2025-04-06");
        assertThat(history.build()).isEqualTo("2025-06-01");
        assertThat(history.done()).isEqualTo("2025-06-10");
    }
}
