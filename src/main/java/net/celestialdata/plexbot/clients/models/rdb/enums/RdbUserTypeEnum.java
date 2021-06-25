package net.celestialdata.plexbot.clients.models.rdb.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public enum RdbUserTypeEnum {
    @JsonProperty(value = "premium")
    PREMIUM,

    @JsonProperty(value = "free")
    FREE
}