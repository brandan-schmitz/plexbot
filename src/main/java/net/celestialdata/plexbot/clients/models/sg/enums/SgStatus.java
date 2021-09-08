package net.celestialdata.plexbot.clients.models.sg.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public enum SgStatus {
    @JsonProperty(value = "Wanted")
    WANTED("wanted"),

    @JsonProperty(value = "Skipped")
    SKIPPED("skipped"),

    @JsonProperty(value = "Archived")
    ARCHIVED("archived"),

    @JsonProperty(value = "Ignored")
    IGNORED("ignored"),

    @JsonProperty(value = "Failed")
    FAILED("failed"),

    @JsonProperty(value = "Snatched")
    SNATCHED("snatched"),

    @JsonProperty(value = "Downloaded")
    DOWNLOADED("downloaded");

    private final String value;

    SgStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}