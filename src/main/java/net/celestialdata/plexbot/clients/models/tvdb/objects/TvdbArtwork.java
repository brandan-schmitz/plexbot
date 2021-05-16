package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbArtwork {
    public int id;
    public String image;
    public String language;
    public float score;
    public String thumbnail;
    public long type;
}