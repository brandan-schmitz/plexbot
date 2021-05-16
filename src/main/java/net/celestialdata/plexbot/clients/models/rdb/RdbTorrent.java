package net.celestialdata.plexbot.clients.models.rdb;

import com.fasterxml.jackson.annotation.JsonAlias;
import net.celestialdata.plexbot.clients.models.rdb.enums.RdbTorrentStatusEnum;

import java.net.URI;
import java.util.List;

@SuppressWarnings("unused")
public class RdbTorrent {
    public String id;
    public String filename;
    public String hash;
    public long bytes;
    public String host;
    public int split;
    public int progress;
    public RdbTorrentStatusEnum status;
    public String added;
    public List<RdbTorrentFile> files;
    public List<URI> links;
    public String ended;
    public int speed;
    public int seeders;

    @JsonAlias("original_filename")
    public String originalFilename;

    @JsonAlias(value = "original_bytes")
    public long originalBytes;
}