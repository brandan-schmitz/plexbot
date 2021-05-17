package net.celestialdata.plexbot.clients.models.yts;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class YtsMovie {
    public int id;
    public String url;
    public String slug;
    public int year;
    public double rating;
    public int runtime;
    public List<String> genres;
    public String summary;
    public String title;
    public String synopsis;
    public String language;
    public String state;
    public List<YtsMovieTorrent> torrents;

    @JsonAlias(value = "imdb_code")
    public String imdbCode;

    @JsonAlias(value = "title_english")
    public String englishTitle;

    @JsonAlias(value = "title_long")
    public String longTitle;

    @JsonAlias(value = "description_full")
    public String fullDescription;

    @JsonAlias(value = "yt_trailer_code")
    public String trailerCode;

    @JsonAlias(value = "mpa_rating")
    public String mpaRating;

    @JsonAlias(value = "background_image")
    public String backgroundImageUrl;

    @JsonAlias(value = "background_image_original")
    public String originalBackgroundImageUrl;

    @JsonAlias(value = "small_cover_image")
    public String smallCoverImage;

    @JsonAlias(value = "medium_cover_image")
    public String mediumCoverImage;

    @JsonAlias(value = "large_cover_image")
    public String largeCoverImage;

    @JsonAlias(value = "date_uploaded")
    public String dateUploaded;

    @JsonAlias(value = "date_uploaded_unix")
    public long dateUploadedUnix;
}