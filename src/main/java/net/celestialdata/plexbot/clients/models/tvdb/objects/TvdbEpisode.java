package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbEpisode {
    public String aired;
    public long id;
    public String image;
    public int imageType;
    public long isMovie;
    public String name;
    public List<String> nameTranslations;
    public int number;
    public List<String> overviewTranslations;
    public int runtime;
    public int seasonNumber;
    public List<TvdbSeason> seasons;
    public long seriesId;

    public String getImage() {
        if (StringUtils.isBlank(this.image)) {
            return ConfigProvider.getConfig().getValue("BotSettings.noPosterImageUrl", String.class);
        } else return this.image;
    }
}