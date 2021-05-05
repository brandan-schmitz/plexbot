package net.celestialdata.plexbot.clients.models.omdb;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public enum OmdbResultType {
    @JsonProperty("episode")
    EPISODE,

    @JsonProperty("series")
    SERIES,

    @JsonProperty("movie")
    MOVIE
}
