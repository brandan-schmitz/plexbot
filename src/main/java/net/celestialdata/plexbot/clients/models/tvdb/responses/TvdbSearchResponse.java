package net.celestialdata.plexbot.clients.models.tvdb.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbSearchResult;

import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbSearchResponse {
    public String status;
    public String message;

    @JsonAlias(value = "data")
    public List<TvdbSearchResult> results;
}
