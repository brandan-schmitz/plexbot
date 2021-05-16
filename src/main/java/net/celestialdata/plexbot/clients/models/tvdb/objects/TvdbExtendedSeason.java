package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbExtendedSeason {
    public String abbreviation;
    public List<TvdbArtwork> artwork;
    public String country;
    public List<TvdbEpisode> episodes;
    public int id;
    public String image;
    public int imageType;
    public String name;
    public List<String> nameTranslations;
    public long number;
    public List<String> overviewTranslations;
    public long seriesId;
    public String slug;
    public List<TvdbTrailer> trailers;
    public TvdbSeasonType type;
}