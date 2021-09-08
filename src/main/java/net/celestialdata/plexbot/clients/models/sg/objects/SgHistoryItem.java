package net.celestialdata.plexbot.clients.models.sg.objects;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.celestialdata.plexbot.clients.models.sg.enums.SgQuality;
import net.celestialdata.plexbot.clients.models.sg.enums.SgStatus;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SgHistoryItem {
    public String date;
    public Integer episode;
    public Integer hide;
    public Integer indexer;
    public String provider;
    public SgQuality quality;
    public String resource;
    public Integer season;
    public SgStatus status;
    public Integer version;

    @JsonAlias(value = "tvdbid")
    public Long tvdbId;

    @JsonAlias(value = "indexerid")
    public Long indexerId;

    @JsonAlias(value = "resource_path")
    public String resourcePath;

    @JsonAlias(value = "show_name")
    public String showName;
}