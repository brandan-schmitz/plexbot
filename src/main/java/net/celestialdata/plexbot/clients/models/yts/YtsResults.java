package net.celestialdata.plexbot.clients.models.yts;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class YtsResults {
    @JsonAlias(value = "movie_count")
    public int resultCount;

    public int limit;

    @JsonAlias(value = "page_number")
    public int pageNumber;

    public List<YtsMovie> movies;
}