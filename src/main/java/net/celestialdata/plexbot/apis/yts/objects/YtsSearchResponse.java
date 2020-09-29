package net.celestialdata.plexbot.apis.yts.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YtsSearchResponse {
    public String status;
    public String status_message;
    public YtsSearchResponseData data;
}
