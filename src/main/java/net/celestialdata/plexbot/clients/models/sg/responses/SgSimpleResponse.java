package net.celestialdata.plexbot.clients.models.sg.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SgSimpleResponse {
    public String message;
    public String result;
}
