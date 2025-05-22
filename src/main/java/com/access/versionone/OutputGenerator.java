package com.access.versionone;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OutputGenerator {
    void createCsvFile(List<StoryHistory> stories) throws IOException {
        boolean includePoints = PropertyFetcher.getProperty("includeStoryPoints").equals("true");
        boolean includeTeam = PropertyFetcher.getProperty("includeTeamName").equals("true");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("stories.csv"))) {
            String headerLine = "ID, " + PropertyFetcher.getProperty("states");
            if (includePoints) {
                headerLine += ", " + "Points";
            }
            if (includeTeam) {
                headerLine += ", " + "Team";
            }
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

                if (includePoints) {
                    builder.append(",");
                    builder.append(story.storyPoints());
                }

                if (includeTeam) {
                    builder.append(",");
                    builder.append(story.teamName());
                }

                writer.write(builder.toString());
                writer.newLine();
            }
        } catch (IOException _) {

        }
    }
}
