package net.celestialdata.plexbot.clients.models.tvdb.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbEpisode;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbEpisodeResponse {
    public String status;
    public String message;

    @JsonAlias(value = "data")
    public TvdbEpisode episode;
}