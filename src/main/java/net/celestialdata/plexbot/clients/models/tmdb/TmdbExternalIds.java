package net.celestialdata.plexbot.clients.models.tmdb;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;


@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbExternalIds {

    @JsonAlias(value = "id")
    public Long tmdbId;

    @Nullable
    @JsonAlias(value = "imdb_id")
    public String imdbId;

    @Nullable
    @JsonAlias(value = "tvdb_id")
    public Long tvdbId;

    @Nullable
    public Boolean success;

    public boolean isSuccessful() {
        return success == null || success;
    }

    public String getImdbId() {
        if (StringUtils.isBlank(imdbId)) {
            return "N/A";
        } else return imdbId;
    }

    public long tvdbId() {
        if (tvdbId == null) {
            return 0;
        } else return tvdbId;
    }
}