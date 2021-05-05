package net.celestialdata.plexbot.clients.models.omdb;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public enum OmdbResponseEnum {
    @JsonProperty("True")
    TRUE,

    @JsonProperty("False")
    FALSE
}
