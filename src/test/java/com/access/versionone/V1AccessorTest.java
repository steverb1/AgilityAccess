package com.access.versionone;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class V1AccessorTest {
    @Test
    void testParsingArgsWithScope() {
        String[] args = {
                "--v1.token=xyz",
                "--v1.url=https://www16.v1host.com/api-examples",
                "--v1.planningLevel=Scope:1005",
                "--fromClosedDate=2025-01-01",
                "--states=\"Ready for Build, Build, Done\"",
                "--includeStoryPoints=true",
                "--includeTeamName=true"
        };

        Map<String, String> properties = V1Accessor.parseArgs(args);

        assertThat(properties.get("v1.token")).isEqualTo("xyz");
        assertThat(properties.get("v1.url")).isEqualTo("https://www16.v1host.com/api-examples");
        assertThat(properties.get("v1.planningLevel")).isEqualTo("Scope:1005");
        assertThat(properties.get("fromClosedDate")).isEqualTo("2025-01-01");
        assertThat(properties.get("states")).isEqualTo("Ready for Build, Build, Done");
        assertThat(properties.get("includeStoryPoints")).isEqualTo("true");
        assertThat(properties.get("includeTeamName")).isEqualTo("true");
    }

    @Test
    void testParsingArgsWithTeam() {
        String[] args = {
                "--v1.token=xyz",
                "--v1.url=https://www16.v1host.com/api-examples",
                "--v1.team=Team:1889",
                "--fromClosedDate=2025-01-01",
                "--states=\"Ready for Build, Build, Done\"",
                "--includeStoryPoints=true",
                "--includeTeamName=true"
        };

        Map<String, String> properties = V1Accessor.parseArgs(args);

        assertThat(properties.get("v1.token")).isEqualTo("xyz");
        assertThat(properties.get("v1.url")).isEqualTo("https://www16.v1host.com/api-examples");
        assertThat(properties.get("v1.team")).isEqualTo("Team:1889");
        assertThat(properties.get("fromClosedDate")).isEqualTo("2025-01-01");
        assertThat(properties.get("states")).isEqualTo("Ready for Build, Build, Done");
        assertThat(properties.get("includeStoryPoints")).isEqualTo("true");
        assertThat(properties.get("includeTeamName")).isEqualTo("true");
    }
}
