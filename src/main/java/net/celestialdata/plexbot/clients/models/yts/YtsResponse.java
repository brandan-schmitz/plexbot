package net.celestialdata.plexbot.clients.models.yts;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class YtsResponse {
    public String status;

    @JsonAlias(value = "status_message")
    public String statusMessage;

    @JsonAlias(value = "data")
    public YtsResults results;
}