package net.celestialdata.plexbot.clients.models.rdb;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public enum RdbUserType {
    @JsonProperty("premium")
    PREMIUM,

    @JsonProperty("free")
    FREE
}