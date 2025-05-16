package com.fedex.versionone;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class OutputGenerator {
    void createCsvFile(List<StoryHistory> stories) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("stories.csv"))) {
            String line = "ID, Ready for Build, Build, Done";
            writer.write(line);
            writer.newLine();
            for (StoryHistory record : stories) {
                line = String.join(",", record.id(), record.readyForBuild(), record.build(), record.done());
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException _) {

        }
    }
}
