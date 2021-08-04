package net.celestialdata.plexbot.clients.models.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TmdbSourceIdType {
    @JsonProperty(value = "imdb_id")
    IMDB,

    @JsonProperty(value = "tvdb_id")
    TVDB
}