package net.celestialdata.plexbot.clients.models.tvdb.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbExtendedSeason;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbExtendedSeasonResponse {
    public String status;

    @JsonAlias(value = "data")
    public TvdbExtendedSeason extendedSeason;
}