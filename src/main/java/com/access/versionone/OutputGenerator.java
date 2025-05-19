package com.access.versionone;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OutputGenerator {
    void createCsvFile(List<StoryHistory> stories) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("stories.csv"))) {
            String headerLine = "ID, " + PropertyFetcher.getProperty("states");
            writer.write(headerLine);
            writer.newLine();
            for (StoryHistory story : stories) {
                Map<String, LocalDate> stateDates = story.stateDates();
                List<String> lineContents = new ArrayList<>();
                for (String state : stateDates.keySet()) {
                    if (stateDates.get(state) == null) {
                        lineContents.add("");
                        continue;
                    }
                    String date = stateDates.get(state).toString();
                    lineContents.add(date);
                }

                StringBuilder builder = new StringBuilder(story.storyId().substring(6) + ",");
                for (int i = 0; i < lineContents.size(); i++) {
                    builder.append(lineContents.get(i));
                    if (i < lineContents.size() - 1) {
                        builder.append(",");
                    }
                }
                writer.write(builder.toString());
                writer.newLine();
            }
        } catch (IOException _) {

        }
    }
}
