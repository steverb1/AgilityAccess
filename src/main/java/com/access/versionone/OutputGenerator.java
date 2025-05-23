package com.access.versionone;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OutputGenerator {
    Writer output;

    public OutputGenerator(Writer output) {
        this.output = output;
    }

    void createCsvFile(List<StoryHistory> stories, boolean includePoints, boolean includeTeamName) {
        try (BufferedWriter writer = new BufferedWriter(output)) {
            String headerLine = "ID, " + PropertyFetcher.getProperty("states");
            if (includePoints) {
                headerLine += ", " + "Points";
            }
            if (includeTeamName) {
                headerLine += ", " + "Team";
            }
            writer.write(headerLine);
            writer.newLine();

            for (StoryHistory story : stories) {
                Map<String, LocalDate> stateDates = story.stateDates();

                StringBuilder builder = new StringBuilder(story.storyId().substring(6) + ",");

                Iterator<String> iterator = stateDates.keySet().iterator();
                while (iterator.hasNext()) {
                    String state = iterator.next();
                    if (stateDates.get(state) != null) {
                        String date = stateDates.get(state).toString();
                        builder.append(date);
                    }
                    if (iterator.hasNext()) {
                        builder.append(",");
                    }
                }

                if (includePoints) {
                    builder.append(",");
                    builder.append(story.storyPoints());
                }

                if (includeTeamName) {
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
