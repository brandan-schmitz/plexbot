package net.celestialdata.plexbot.clients.models.rdb;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public enum RdbStreamableEnum {
    @JsonProperty(value = "0")
    TRUE,

    @JsonProperty(value = "1")
    FALSE
}
