package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbList {
    public List<TvdbAlias> aliases;
    public long id;
    public boolean isOfficial;
    public String name;
    public List<String> nameTranslations;
    public String overview;
    public List<String> overviewTranslations;
    public String url;
}