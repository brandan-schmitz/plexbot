package net.celestialdata.plexbot.clients.models.rdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RdbAddedTorrent {
    public String id;
    public String uri;
}