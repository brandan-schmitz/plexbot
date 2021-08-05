package net.celestialdata.plexbot.clients.models.tmdb;

import com.fasterxml.jackson.annotation.JsonAlias;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("unused")
public class TmdbMovieSearchResults {

    public Integer page;

    public List<TmdbMovie> results;

    @JsonAlias(value = "total_results")
    public Integer numResults;

    @JsonAlias(value = "total_pages")
    public Integer numPages;

    @Nullable
    public Boolean success;

    public boolean isSuccessful() {
        return success == null || success;
    }
}
