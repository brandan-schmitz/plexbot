package net.celestialdata.plexbot.clients.models.rdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RdbTorrentFile {
    public Integer id;
    public String path;
    public Long bytes;
    public RdbSelectedEnum selected;
}