package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbSeries {
    public String abbreviation;
    public String country;
    public int id;
    public String image;
    public int imageType;
    public String name;
    public List<String> nameTranslations;
    public long number;
    public List<String> overviewTranslations;
    public long seriesId;
    public String slug;
    public TvdbSeasonType type;
}