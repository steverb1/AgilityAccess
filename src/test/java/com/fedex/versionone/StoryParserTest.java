package com.fedex.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

public class StoryParserTest {
    @Test
    void findingStartDate_YieldsCorrectDate() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try (InputStream inputSteam = getClass().getClassLoader().getResourceAsStream("sampleStory.json")) {
            root = mapper.readTree(inputSteam);
        }
        StoryParser parser = new StoryParser(root);
        LocalDate startDate = parser.findStartDate();

        LocalDate expectedDate = LocalDate.of(2025, 4, 6);
        assertThat(startDate).isEqualTo(expectedDate);
    }

    @Test
    void findingEndDate_YieldsCorrectDate() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try (InputStream inputSteam = getClass().getClassLoader().getResourceAsStream("sampleStory.json")) {
            root = mapper.readTree(inputSteam);
        }
        StoryParser parser = new StoryParser(root);
        LocalDate endDate = parser.findEndDate();

        LocalDate expectedDate = LocalDate.of(2025, 6, 10);
        assertThat(endDate).isEqualTo(expectedDate);
    }
}
