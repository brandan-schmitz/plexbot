package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbSearchResult {
    public String name;
    public String type;

    @JsonAlias(value = "tvdb_id")
    public String tvdbId;
}
