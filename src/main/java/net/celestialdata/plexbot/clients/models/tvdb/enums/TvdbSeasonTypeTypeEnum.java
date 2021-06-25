package net.celestialdata.plexbot.clients.models.tvdb.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public enum TvdbSeasonTypeTypeEnum {
    @JsonProperty(value = "official")
    OFFICIAL,

    @JsonProperty(value = "dvd")
    DVD,

    @JsonProperty(value = "absolute")
    ABSOLUTE,

    @JsonProperty(value = "alternate")
    ALTERNATE,

    @JsonProperty(value = "regional")
    REGIONAL,

    @JsonProperty(value = "altdvd")
    ALTDVD
}