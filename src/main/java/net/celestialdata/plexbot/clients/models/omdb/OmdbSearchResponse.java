package net.celestialdata.plexbot.clients.models.omdb;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OmdbSearchResponse {

    @JsonAlias(value = "Search")
    public List<OmdbResult> search;

    public String totalResults;

    @JsonAlias(value = "Response")
    public OmdbResponseEnum response;

    @JsonAlias(value = "error")
    public String error;
}