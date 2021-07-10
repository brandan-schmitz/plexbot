package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbExtendedSeries {
    public String abbreviation;
    public TvdbSeriesAirsDays airsDays;
    public String airsTime;
    public List<TvdbAlias> aliases;
    public List<TvdbArtwork> artworks;
    public List<TvdbCharacter> characters;
    public String country;
    public long defaultSeasonType;
    public String firstAired;
    public List<TvdbList> lists;
    public List<TvdbGenre> genres;
    public long id;
    public String image;
    public boolean isOrderRandomized;
    public String lastAired;
    public String name;
    public List<String> nameTranslations;
    public List<TvdbNetwork> networks;
    public String nextAired;
    public String originalCountry;
    public String originalLanguage;
    public List<String> overviewTranslations;
    public List<TvdbRemoteID> remoteIds;
    public double score;
    public List<TvdbSeason> seasons;
    public String slug;
    public TvdbStatus status;
    public List<TvdbTrailer> trailers;

    public String getImage() {
        var noPosterImageUrl = ConfigProvider.getConfig().getValue("BotSettings.noPosterImageUrl", String.class);
        if (this.image.isBlank() || this.image == null) {
            return noPosterImageUrl;
        } else return this.image;
    }
}