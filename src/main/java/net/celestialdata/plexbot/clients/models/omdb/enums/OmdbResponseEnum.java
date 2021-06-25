package net.celestialdata.plexbot.clients.models.omdb.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public enum OmdbResponseEnum {
    @JsonProperty(value = "True")
    TRUE,

    @JsonProperty(value = "False")
    FALSE
}
