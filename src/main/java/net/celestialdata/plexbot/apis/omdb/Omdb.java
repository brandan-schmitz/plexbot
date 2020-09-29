package net.celestialdata.plexbot.apis.omdb;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.celestialdata.plexbot.apis.omdb.objects.movie.OmdbMovie;
import net.celestialdata.plexbot.apis.omdb.objects.search.SearchResultResponse;
import net.celestialdata.plexbot.config.ConfigProvider;

public class Omdb {

    private static String baseUrl = "http://www.omdbapi.com/?apikey=" + ConfigProvider.API_KEYS.omdbApiKey();

    public static SearchResultResponse movieSearch(String title) {
        HttpResponse<SearchResultResponse> response = null;
        try {
            response = Unirest.get(baseUrl)
                    .queryString("s", title)
                    .queryString("type", "movie")
                    .asObject(SearchResultResponse.class);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return response.getBody();
    }

    public static SearchResultResponse movieSearch(String title, int pageNum) {
        HttpResponse<SearchResultResponse> response = null;
        try {
            response = Unirest.get(baseUrl)
                    .queryString("s", title)
                    .queryString("type", "movie")
                    .queryString("page", pageNum)
                    .asObject(SearchResultResponse.class);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return response.getBody();
    }

    public static SearchResultResponse movieSearch(String title, String year) {
        HttpResponse<SearchResultResponse> response = null;
        try {
            response = Unirest.get(baseUrl)
                    .queryString("s", title)
                    .queryString("y", year)
                    .queryString("type", "movie")
                    .asObject(SearchResultResponse.class);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return response.getBody();
    }

    public static OmdbMovie getMovieInfo(String imdbCode) {
        HttpResponse<OmdbMovie> response = null;
        try {
            response = Unirest.get(baseUrl)
                    .queryString("i", imdbCode)
                    .queryString("plot", "short")
                    .queryString("type", "movie")
                    .asObject(OmdbMovie.class);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return response.getBody();
    }
}
