package net.celestialdata.plexbot.clients.models.rdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.net.URI;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RdbAlternativeUnrestrictedLink {
    public String id;
    public String filename;
    public URI download;
    public String mimeType;
    public String type;
}
