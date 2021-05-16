package net.celestialdata.plexbot.clients.models.omdb.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public enum OmdbResultTypeEnum {
    @JsonProperty("episode")
    EPISODE,

    @JsonProperty("series")
    SERIES,

    @JsonProperty("movie")
    MOVIE
}
