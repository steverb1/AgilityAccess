package com.access.versionone;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OutputGenerator {
    String createCsvContent(List<StoryHistory> stories, boolean includePoints, boolean includeTeamName, String states) {
        StringBuilder csvContent = new StringBuilder();

        String headerLine = "ID, " + states;
        if (includePoints) {
            headerLine += ", " + "Points";
        }
        if (includeTeamName) {
            headerLine += ", " + "Team";
        }
        csvContent.append(headerLine).append("\n");

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

            csvContent.append(builder).append("\n");
        }

        return csvContent.toString();
    }
}
