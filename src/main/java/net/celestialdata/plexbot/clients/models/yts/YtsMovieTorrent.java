package net.celestialdata.plexbot.clients.models.yts;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class YtsMovieTorrent {
    public String url;
    public String hash;
    public String quality;
    public String type;
    public int seeds;
    public int peers;
    public String size;

    @JsonAlias(value = "size_bytes")
    public long sizeInBytes;

    @JsonAlias(value = "date_uploaded")
    public String dateUploaded;

    @JsonAlias(value = "date_uploaded_unix")
    public long dateUploadedUnix;
}