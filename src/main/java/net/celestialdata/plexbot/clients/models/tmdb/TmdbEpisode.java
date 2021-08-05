package net.celestialdata.plexbot.clients.models.tmdb;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class TmdbEpisode {
    @JsonAlias(value = "id")
    public Long tmdbId;

    @JsonAlias(value = "show_id")
    public Long showId;

    @JsonAlias(value = "air_date")
    public String date;

    @JsonAlias(value = "episode_number")
    public Integer number;

    public String name;

    public String overview;

    @JsonAlias(value = "season_number")
    public Integer seasonNum;

    @Nullable
    @JsonAlias(value = "still_path")
    public String image;

    @Nullable
    public Boolean success;

    public boolean isSuccessful() {
        return success == null || success;
    }

    public String getImage() {
        if (StringUtils.isBlank(this.image)) {
            return ConfigProvider.getConfig().getValue("BotSettings.noPosterImageUrl", String.class);
        } else return "https://image.tmdb.org/t/p/original" + this.image;
    }
}
