package com.fedex.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;

public class StoryParserTest {
    @Test
    void findingStartDate_YieldsCorrectDate() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try (InputStream inputSteam = getClass().getClassLoader().getResourceAsStream("sampleStory.json")) {
            root = mapper.readTree(inputSteam);
        }
        StoryParser parser = new StoryParser(root);
        String startDate = parser.findStartDate();

        assertThat(startDate).isEqualTo("2025-04-06");
    }

    @Test
    void findingEndDate_YieldsCorrectDate() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try (InputStream inputSteam = getClass().getClassLoader().getResourceAsStream("sampleStory.json")) {
            root = mapper.readTree(inputSteam);
        }
        StoryParser parser = new StoryParser(root);
        String endDate = parser.findEndDate();

        assertThat(endDate).isEqualTo("2025-06-10");
    }
}
