/*
 * Plexbot
 * Provides all the API functions necessary for the Plexbot to function.
 *
 * The version of the OpenAPI document: 1.0.0
 * Contact: brandan.schmitz@celestialdata.net
 *
 * NOTE: This class is auto generated by OpenAPI Generator
 * (https://openapi-generator.tech). https://openapi-generator.tech Do not edit
 * the class manually.
 */

package net.celestialdata.plexbot.client.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data resulting from a search for a movie on YTS using the query_term
 * parameter
 */
@SuppressWarnings("unused")
@ApiModel(
        description =
                "Data resulting from a search for a movie on YTS using the query_term parameter")
@javax.annotation.
        Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen",
        date = "2021-02-18T23:30:04.504837-06:00[America/Chicago]")
public class YtsSearchResult {
    public static final String SERIALIZED_NAME_MOVIE_COUNT = "movie_count";
    public static final String SERIALIZED_NAME_LIMIT = "limit";
    public static final String SERIALIZED_NAME_PAGE_NUMBER = "page_number";
    public static final String SERIALIZED_NAME_MOVIES = "movies";
    @SerializedName(SERIALIZED_NAME_MOVIE_COUNT)
    private Integer movieCount;
    @SerializedName(SERIALIZED_NAME_LIMIT)
    private Integer limit;
    @SerializedName(SERIALIZED_NAME_PAGE_NUMBER)
    private Integer pageNumber;
    @SerializedName(SERIALIZED_NAME_MOVIES)
    private List<YtsMovieInfo> movies = null;

    @SuppressWarnings("unused")
    public YtsSearchResult movieCount(Integer movieCount) {

        this.movieCount = movieCount;
        return this;
    }

    /**
     * Number of movies returned in this search
     *
     * @return movieCount
     **/
    @SuppressWarnings("unused")
    @ApiModelProperty(value = "Number of movies returned in this search")

    public Integer getMovieCount() {
        if (movieCount == null) {
            movieCount = 0;
        }
        return movieCount;
    }

    @SuppressWarnings("unused")
    public void setMovieCount(Integer movieCount) {
        this.movieCount = movieCount;
    }

    @SuppressWarnings("unused")
    public YtsSearchResult limit(Integer limit) {

        this.limit = limit;
        return this;
    }

    /**
     * Maximum number of results displayed on a \&quot;page\&quot;
     *
     * @return limit
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Maximum number of results displayed on a \"page\"")

    public Integer getLimit() {
        return limit;
    }

    @SuppressWarnings("unused")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @SuppressWarnings("unused")
    public YtsSearchResult pageNumber(Integer pageNumber) {

        this.pageNumber = pageNumber;
        return this;
    }

    /**
     * Current \&quot;page\&quot; of results
     *
     * @return pageNumber
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Current \"page\" of results")

    public Integer getPageNumber() {
        return pageNumber;
    }

    @SuppressWarnings("unused")
    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    @SuppressWarnings("unused")
    public YtsSearchResult movies(List<YtsMovieInfo> movies) {

        this.movies = movies;
        return this;
    }

    @SuppressWarnings("unused")
    public YtsSearchResult addMoviesItem(YtsMovieInfo moviesItem) {
        if (this.movies == null) {
            this.movies = new ArrayList<>();
        }
        this.movies.add(moviesItem);
        return this;
    }

    /**
     * List of movies
     *
     * @return movies
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(value = "List of movies")

    public List<YtsMovieInfo> getMovies() {
        return movies;
    }

    @SuppressWarnings("unused")
    public void setMovies(List<YtsMovieInfo> movies) {
        this.movies = movies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        YtsSearchResult ytsSearchResult = (YtsSearchResult) o;
        return Objects.equals(this.movieCount, ytsSearchResult.movieCount) &&
                Objects.equals(this.limit, ytsSearchResult.limit) &&
                Objects.equals(this.pageNumber, ytsSearchResult.pageNumber) &&
                Objects.equals(this.movies, ytsSearchResult.movies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieCount, limit, pageNumber, movies);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class YtsSearchResult {\n");
        sb.append("    movieCount: ")
                .append(toIndentedString(movieCount))
                .append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    pageNumber: ")
                .append(toIndentedString(pageNumber))
                .append("\n");
        sb.append("    movies: ").append(toIndentedString(movies)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
