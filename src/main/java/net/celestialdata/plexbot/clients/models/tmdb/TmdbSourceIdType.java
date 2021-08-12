package net.celestialdata.plexbot.clients.models.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@SuppressWarnings("unused")
@AllArgsConstructor
@Getter
public enum TmdbSourceIdType {
    @JsonProperty(value = "imdb_id")
    IMDB("imdb_id"),

    @JsonProperty(value = "tvdb_id")
    TVDB("tvdb_id");

    private final String value;
}