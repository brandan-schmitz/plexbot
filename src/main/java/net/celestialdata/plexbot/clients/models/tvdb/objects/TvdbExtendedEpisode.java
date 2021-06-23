package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbExtendedEpisode {
    public String aired;
    public int airsAfterSeason;
    public int airsBeforeEpisode;
    public int airsBeforeSeason;
    public List<TvdbAward> awards = null;
    public List<TvdbCharacter> characters = null;
    public List<TvdbContentRating> contentRatings = null;
    public long id;
    public String image;
    public int imageType;
    public int isMovie;
    public String name;
    public List<String> nameTranslations = null;
    public TvdbNetwork network;
    public int number;
    public List<String> overviewTranslations = null;
    public String productionCode;
    public List<TvdbRemoteID> remoteIds = null;
    public int runtime;
    public int seasonNumber;
    public List<TvdbSeason> seasons = null;
    public long seriesId;
    public List<TvdbTag> tagOptions = null;
    public List<TvdbTrailer> trailers = null;

    public String getImage() {
        var noPosterImageUrl = ConfigProvider.getConfig().getValue("BotSettings.noPosterImageUrl", String.class);
        if (this.image.isBlank()) {
            return noPosterImageUrl;
        } else return this.image;
    }
}