package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        StoryParser parser = new StoryParser(root);
        Map<String, LocalDate> history = parser.findStateChangeDates();

        assertThat(history.get("Ready for Build")).isEqualTo("2025-04-06");
        assertThat(history.get("Build")).isEqualTo("2025-06-01");
        assertThat(history.get("Done")).isEqualTo("2025-06-10");
    }
}
