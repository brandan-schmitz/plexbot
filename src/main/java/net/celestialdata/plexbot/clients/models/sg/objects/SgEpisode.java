package net.celestialdata.plexbot.clients.models.sg.objects;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.celestialdata.plexbot.clients.models.sg.enums.SgQuality;
import net.celestialdata.plexbot.clients.models.sg.enums.SgStatus;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SgEpisode {

    @JsonAlias(value = "absolute_number")
    public Integer absoluteNumber;

    @JsonAlias(value = "airdate")
    public String airedDate;

    public String description;

    @JsonAlias(value = "file_size")
    public Long fileSize;

    @JsonAlias(value = "file_size_human")
    public String humanReadableFileSize;

    public String location;
    public String name;
    public SgQuality quality;

    @JsonAlias(value = "release_name")
    public String releaseName;

    @Nullable
    @JsonAlias(value = "scene_absolute_number")
    public Integer sceneAbsoluteNumber;

    @Nullable
    @JsonAlias(value = "scene_episode")
    public Integer sceneEpisode;

    @Nullable
    @JsonAlias(value = "scene_season")
    public Integer sceneSeason;

    public SgStatus status;
    public String subtitles;
    public String timezone;
}