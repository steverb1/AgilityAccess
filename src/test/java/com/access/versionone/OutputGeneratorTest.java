package com.access.versionone;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class OutputGeneratorTest {
    OutputGenerator generator = new OutputGenerator();
    List<StoryHistory> histories = new ArrayList<>();
    Map<String, LocalDate> stateDates = new LinkedHashMap<>();

    @Test
    void createCsvContent_optionsAllTrue_resultsInCorrectData() {
        stateDates.put("Ready for Build", LocalDate.of(2025, 5, 23));
        stateDates.put("Build", LocalDate.of(2025, 5, 25));
        stateDates.put("Done", LocalDate.of(2025, 5, 27));
        StoryHistory record1 = new StoryHistory("Story:123", stateDates, 3.0f, "Team Bob");
        StoryHistory record2 = new StoryHistory("Story:456", stateDates, null, "Team Bob");
        histories.add(record1);
        histories.add(record2);

        String result = generator.createCsvContent(histories, true, true, "Ready for Build, Build, Done");
        assertThat(result).isEqualTo("""
                ID, Ready for Build, Build, Done, Points, Team
                123,2025-05-23,2025-05-25,2025-05-27,3.0,Team Bob
                456,2025-05-23,2025-05-25,2025-05-27,,Team Bob
                """);
    }

    @Test
    void createCsvContent_optionsAllFalse_resultsInCorrectData() {
        stateDates.put("Ready for Build", LocalDate.of(2025, 5, 23));
        StoryHistory record1 = new StoryHistory("Story:123", stateDates, 3.0f, "Team Bob");
        stateDates.put("Build", LocalDate.of(2025, 5, 25));
        stateDates.put("Done", LocalDate.of(2025, 5, 27));
        histories.add(record1);

        String result = generator.createCsvContent(histories, false, false, "Ready for Build, Build, Done");
        assertThat(result).isEqualTo("""
                ID, Ready for Build, Build, Done
                123,2025-05-23,2025-05-25,2025-05-27
                """);
    }

    @Test
    void createCsvContent_nullDate_resultsInExtraCommas() {
        stateDates.put("Ready for Build", LocalDate.of(2025, 5, 23));
        stateDates.put("Build", null);
        stateDates.put("Done", null);
        StoryHistory record1 = new StoryHistory("Story:123", stateDates, 3.0f, "Team Bob");
        histories.add(record1);

        String result = generator.createCsvContent(histories, true, true, "Ready for Build, Build, Done");
        assertThat(result).isEqualTo("""
                ID, Ready for Build, Build, Done, Points, Team
                123,2025-05-23,,,3.0,Team Bob
                """);
    }
}
