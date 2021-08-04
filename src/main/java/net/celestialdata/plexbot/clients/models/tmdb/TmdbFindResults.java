package net.celestialdata.plexbot.clients.models.tmdb;

import com.fasterxml.jackson.annotation.JsonAlias;

import javax.annotation.Nullable;
import java.util.List;

public class TmdbFindResults {

    @JsonAlias(value = "movie_results")
    public List<TmdbMovie> movies;

    @JsonAlias(value = "tv_results")
    public List<TmdbShow> shows;

    @JsonAlias(value = "tv_episode_results")
    public List<TmdbEpisode> episodes;

    @Nullable
    public Boolean success;

    public boolean isSuccessful() {
        return success == null || success;
    }
}
