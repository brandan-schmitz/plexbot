package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbExtendedSeason {
    public String abbreviation;
    public List<TvdbArtwork> artwork;
    public String country;
    public List<TvdbEpisode> episodes;
    public Long id;
    public String image;
    public int imageType;
    public String name;
    public List<String> nameTranslations;
    public int number;
    public List<String> overviewTranslations;
    public long seriesId;
    public String slug;
    public List<TvdbTrailer> trailers;
    public TvdbSeasonType type;

    public String getImage() {
        var noPosterImageUrl = ConfigProvider.getConfig().getValue("BotSettings.noPosterImageUrl", String.class);
        if (this.image.isBlank() || this.image == null) {
            return noPosterImageUrl;
        } else return this.image;
    }
}