package net.celestialdata.plexbot.clients.models.rdb;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.net.URI;
import java.util.List;

@SuppressWarnings("unused")
public class RdbTorrent {
    private String id;
    private String filename;
    private String hash;
    private Long bytes;
    private String host;
    private Integer split;
    private Integer progress;
    private RdbTorrentStatus status;
    private String added;
    private List<RdbTorrentFile> files;
    private List<URI> links;
    private String ended;
    private Integer speed;
    private Integer seeders;

    @JsonAlias("original_filename")
    private String originalFilename;

    @JsonAlias(value = "original_bytes")
    private Long originalBytes;
}