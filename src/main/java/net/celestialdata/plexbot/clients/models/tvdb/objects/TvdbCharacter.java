package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbCharacter {
    public List<TvdbAlias> aliases;
    public int episodeId;
    public long id;
    public String image;
    public boolean isFeatured;
    public int movieId;
    public String name;
    public List<String> nameTranslations;
    public List<String> overviewTranslations;
    public int peopleId;
    public int seriesId;
    public long sort;
    public long type;
    public String url;
}