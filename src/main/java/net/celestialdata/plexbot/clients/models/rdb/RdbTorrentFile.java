package net.celestialdata.plexbot.clients.models.rdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RdbTorrentFile {
    private Integer id;
    private String path;
    private Long bytes;
    private RdbSelectedEnum selected;
}