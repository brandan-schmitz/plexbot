package net.celestialdata.plexbot.clients.models.tmdb;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.annotation.Nullable;

public class TmdbShow {

    @JsonAlias(value = "id")
    public Long tmdbId;

    public String name;

    public String overview;

    @Nullable
    @JsonAlias(value = "poster_path")
    public String poster;

    @Nullable
    public Boolean success;

    public boolean isSuccessful() {
        return success == null || success;
    }

    public String getPoster() {
        if (StringUtils.isBlank(this.poster)) {
            return ConfigProvider.getConfig().getValue("BotSettings.noPosterImageUrl", String.class);
        } else return "https://image.tmdb.org/t/p/original" + this.poster;
    }


}