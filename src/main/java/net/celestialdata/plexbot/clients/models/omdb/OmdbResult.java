package net.celestialdata.plexbot.clients.models.omdb;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OmdbResult {

    @JsonAlias(value = "Title")
    public String title;

    @JsonAlias(value = "Year")
    public String year;

    @JsonAlias(value = "Released")
    public String released;

    @JsonAlias(value = "Season")
    public String season;

    @JsonAlias(value = "Episode")
    public String episode;

    @JsonAlias(value = "Runtime")
    public String runtime;

    @JsonAlias(value = "Genre")
    public String genre;

    @JsonAlias(value = "Director")
    public String director;

    @JsonAlias(value = "Writer")
    public String writer;

    @JsonAlias(value = "Actors")
    public String actors;

    @JsonAlias(value = "Plot")
    public String plot;

    @JsonAlias(value = "Language")
    public String language;

    @JsonAlias(value = "Country")
    public String country;

    @JsonAlias(value = "Awards")
    public String awards;

    @JsonAlias(value = "Poster")
    public String poster;

    public String imdbID;
    public String seriesID;

    @JsonAlias(value = "Type")
    public OmdbResultType type;

    @JsonAlias(value = "Response")
    public OmdbResponseEnum response;

    @JsonAlias(value = "Error")
    public String error;
}