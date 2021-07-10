package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbExtendedMovie {
    public List<TvdbAlias> aliases;
    public List<TvdbArtwork> artworks;
    public List<String> audioLanguages;
    public List<TvdbAward> awards;
    public String boxOffice;
    public String budget;
    public List<TvdbCharacter> characters;
    public List<TvdbList> lists;
    public List<TvdbGenre> genres;
    public long id;
    public String image;
    public String name;
    public List<String> nameTranslations;
    public String originalCountry;
    public String originalLanguage;
    public List<String> overviewTranslations;
    public List<TvdbRelease> releases;
    public List<TvdbRemoteID> remoteIds;
    public double score;
    public String slug;
    public TvdbStatus status;
    public List<TvdbStudio> studios;
    public List<String> subtitleLanguages;
    public List<TvdbTrailer> trailers;

    public String getImage() {
        var noPosterImageUrl = ConfigProvider.getConfig().getValue("BotSettings.noPosterImageUrl", String.class);
        if (this.image.isBlank() || this.image == null) {
            return noPosterImageUrl;
        } else return this.image;
    }
}