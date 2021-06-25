package net.celestialdata.plexbot.clients.models.rdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.celestialdata.plexbot.clients.models.rdb.enums.RdbSelectedEnum;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RdbTorrentFile {
    public int id;
    public String path;
    public long bytes;
    public RdbSelectedEnum selected;
}