package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbContentRating {
    public long id;
    public String name;
    public String country;
    public String contentType;
    public int order;
    public String fullName;
}