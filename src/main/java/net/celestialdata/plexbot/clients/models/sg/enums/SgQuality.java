package net.celestialdata.plexbot.clients.models.sg.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public enum SgQuality {
    @JsonProperty(value = "SD TV")
    SD_TV("sdtv", "SD TV", 1),

    @JsonProperty(value = "SD DVD")
    SD_DVD("sddvd", "SD DVD", 2),

    @JsonProperty(value = "HD TV")
    HD_TV("hdtv", "HD TV", 4),

    @JsonProperty(value = "RawHD TV")
    RAW_HD_TV("rawhdtv", "RawHD TV", 8),

    @JsonProperty(value = "720p WEB-DL")
    HD_720_WEB("hdwebdl", "720p WEB-DL", 32),

    @JsonProperty(value = "720p BluRay")
    HD_720_BLURAY("hdbluray", "720p BluRay", 128),

    @JsonProperty(value = "1080p HD TV")
    HD_1080("fullhdtv", "1080p HD TV", 16),

    @JsonProperty(value = "1080p WEB-DL")
    HD_1080_WEB("fullhdwebdl", "1080p WEB-DL", 64),

    @JsonProperty(value = "1080p BluRay")
    HD_1080_BLURAY("fullhdbluray", "1080p BluRay", 256),

    @JsonProperty(value = "UHD2160p")
    UHD_WEB("uhd4kweb", "UHD2160p", 1024),

    @JsonProperty(value = "Unknown")
    UNKNOWN("unknown", "Unknown", 32768);

    private final String apiString;
    private final String humanString;
    private final Integer id;

    SgQuality(String apiString, String humanString, int id) {
        this.apiString = apiString;
        this.humanString = humanString;
        this.id = id;
    }

    public String getApiString() {
        return apiString;
    }

    public String getHumanString() {
        return humanString;
    }

    public Integer getId() {
        return id;
    }
}