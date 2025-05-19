package com.access.versionone;

import java.time.LocalDate;
import java.util.Map;

public record StoryHistory(String storyId, Map<String, LocalDate> stateDates) {
}
