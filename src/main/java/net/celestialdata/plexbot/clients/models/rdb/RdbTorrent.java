package net.celestialdata.plexbot.clients.models.rdb;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.net.URI;
import java.util.List;

@SuppressWarnings("unused")
public class RdbTorrent {
    public String id;
    public String filename;
    public String hash;
    public Long bytes;
    public String host;
    public Integer split;
    public Integer progress;
    public RdbTorrentStatusEnum status;
    public String added;
    public List<RdbTorrentFile> files;
    public List<URI> links;
    public String ended;
    public Integer speed;
    public Integer seeders;

    @JsonAlias("original_filename")
    public String originalFilename;

    @JsonAlias(value = "original_bytes")
    public Long originalBytes;
}