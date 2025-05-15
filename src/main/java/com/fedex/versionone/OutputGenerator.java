package com.fedex.versionone;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class OutputGenerator {
    void createCsvFile(List<Story> stories) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("stories.csv"))) {
            String line = "ID, Start, End";
            writer.write(line);
            writer.newLine();
            for (Story record : stories) {
                line = String.join(",", record.id(), record.startDate(), record.endDate());
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException _) {

        }
    }
}
