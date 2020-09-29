package net.celestialdata.plexbot.apis.yts.objects;

import java.util.ArrayList;
import java.util.List;

public class YtsMovie {
    public int id;
    public String url;
    public String imdb_code;
    public String title;
    public String title_english;
    public String title_long;
    public String slug;
    public int year;
    public double rating;
    public int runtime;
    public List<String> genres;
    public String summary;
    public String description_full;
    public String synopsis;
    public String yt_trailer_code;
    public String language;
    public String mpa_rating;
    public String background_image;
    public String background_image_original;
    public String small_cover_image;
    public String medium_cover_image;
    public String large_cover_image;
    public String state;
    public ArrayList<YtsTorrent> torrents;
    public String date_uploaded;
    public long date_uploaded_unix;
}
