package net.celestialdata.plexbot.clients.models.tvdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbLoginRequestBody {
    public String apiKey;
    public String pin;
}