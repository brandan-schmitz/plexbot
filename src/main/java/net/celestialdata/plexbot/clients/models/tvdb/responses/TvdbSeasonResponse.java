package net.celestialdata.plexbot.clients.models.tvdb.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbSeason;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbSeasonResponse {
    public String status;
    public String message;

    @JsonAlias(value = "data")
    public TvdbSeason season;
}