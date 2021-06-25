package net.celestialdata.plexbot.clients.models.rdb;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.celestialdata.plexbot.clients.models.rdb.enums.RdbCrcEnum;
import net.celestialdata.plexbot.clients.models.rdb.enums.RdbStreamableEnum;

import java.net.URI;
import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RdbUnrestrictedLink {
    public String id;
    public String filename;
    public long filesize;
    public String link;
    public String host;
    public int chunks;
    public URI download;
    public RdbStreamableEnum streamable;
    public String mimeType;
    public String type;

    @JsonAlias(value = "crc")
    public RdbCrcEnum checkCRC;

    @JsonAlias(value = "alternative")
    public List<RdbAlternativeUnrestrictedLink> alternativeLinks;
}