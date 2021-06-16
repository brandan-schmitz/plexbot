package net.celestialdata.plexbot.clients.models.rdb.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public enum RdbStreamableEnum {
    @JsonProperty(value = "1")
    TRUE,

    @JsonProperty(value = "0")
    FALSE
}
