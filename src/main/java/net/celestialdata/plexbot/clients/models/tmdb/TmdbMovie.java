package net.celestialdata.plexbot.clients.models.tmdb;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.annotation.Nullable;

public class TmdbMovie {

    @JsonAlias(value = "id")
    public Long tmdbId;

    @Nullable
    @JsonAlias(value = "imdb_id")
    public String imdbId;

    public String title;

    @Nullable
    public String overview;

    @JsonAlias(value = "release_date")
    public String releaseDate;

    @Nullable
    @JsonAlias(value = "poster_path")
    public String poster;

    @Nullable
    public Boolean success;

    public boolean isSuccessful() {
        return success == null || success;
    }

    public String getImdbId() {
        if (StringUtils.isBlank(imdbId)) {
            return "N/A";
        } else return this.imdbId;
    }

    public String getOverview() {
        if (StringUtils.isBlank(this.overview)) {
            return "N/A";
        } else return this.overview;
    }

    public String getPoster() {
        if (StringUtils.isBlank(this.poster)) {
            return ConfigProvider.getConfig().getValue("BotSettings.noPosterImageUrl", String.class);
        } else return "https://image.tmdb.org/t/p/original" + this.poster;
    }

    public String getYear() {
        return releaseDate.substring(0, 3);
    }
}