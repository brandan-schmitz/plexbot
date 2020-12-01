/*
 * Plexbot
 * Provides all the API functions necessary for the Plexbot to function.
 *
 * OpenAPI spec version: 1.0.0
 * Contact: brandan.schmitz@celestialdata.net
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package net.celestialdata.plexbot.client.model;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.media.Schema;
import net.celestialdata.plexbot.config.ConfigProvider;

import javax.annotation.processing.Generated;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Information about a movie from OMDb
 */
@SuppressWarnings("unused")
@Schema(description = "Information about a movie from OMDb")
@Generated(value = "net.celestialdata.plexbot.codegen.v3.generators.java.JavaClientCodegen", date = "2020-11-23T04:57:45.670Z[GMT]")
public class OmdbMovieInfo {
    @SerializedName("Title")
    private String title = null;

    @SerializedName("Year")
    private String year = null;

    @SerializedName("Rated")
    private String rated = null;

    @SerializedName("Released")
    private String released = null;

    @SerializedName("Runtime")
    private String runtime = null;

    @SerializedName("Genre")
    private String genre = null;

    @SerializedName("Director")
    private String director = null;

    @SerializedName("Writer")
    private String writer = null;

    @SerializedName("Actors")
    private String actors = null;

    @SerializedName("Plot")
    private String plot = null;

    @SerializedName("Language")
    private String language = null;

    @SerializedName("Country")
    private String country = null;

    @SerializedName("Awards")
    private String awards = null;

    @SerializedName("Poster")
    private String poster = null;

    @SerializedName("Ratings")
    private List<OmdbMovieRating> ratings = null;

    @SerializedName("Metascore")
    private String metascore = null;

    @SerializedName("imdbRating")
    private String imdbRating = null;

    @SerializedName("imdbVotes")
    private String imdbVotes = null;

    @SerializedName("imdbID")
    private String imdbID = null;

    @SerializedName("Type")
    private String type = null;

    @SerializedName("DVD")
    private String DVD = null;

    @SerializedName("BoxOffice")
    private String boxOffice = null;

    @SerializedName("Production")
    private String production = null;

    @SerializedName("Website")
    private String website = null;
    @SerializedName("Response")
    private ResponseEnum response = null;
    @SerializedName("Error")
    private String error = null;

    @SuppressWarnings("unused")
    public OmdbMovieInfo title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Movie Title
     *
     * @return title
     **/
    @Schema(description = "Movie Title")
    public String getTitle() {
        return title;
    }

    @SuppressWarnings("unused")
    public void setTitle(String title) {
        this.title = title;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo year(String year) {
        this.year = year;
        return this;
    }

    /**
     * Movie Year
     *
     * @return year
     **/
    @Schema(description = "Movie Year")
    public String getYear() {
        return year;
    }

    @SuppressWarnings("unused")
    public void setYear(String year) {
        this.year = year;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo rated(String rated) {
        this.rated = rated;
        return this;
    }

    /**
     * Movie Rating
     *
     * @return rated
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie Rating")
    public String getRated() {
        return rated;
    }

    @SuppressWarnings("unused")
    public void setRated(String rated) {
        this.rated = rated;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo released(String released) {
        this.released = released;
        return this;
    }

    /**
     * Movie Released
     *
     * @return released
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie Released")
    public String getReleased() {
        return released;
    }

    @SuppressWarnings("unused")
    public void setReleased(String released) {
        this.released = released;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo runtime(String runtime) {
        this.runtime = runtime;
        return this;
    }

    /**
     * Movie Runtime (minutes)
     *
     * @return runtime
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie Runtime (minutes)")
    public String getRuntime() {
        return runtime;
    }

    @SuppressWarnings("unused")
    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo genre(String genre) {
        this.genre = genre;
        return this;
    }

    /**
     * Movie Genre
     *
     * @return genre
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie Genre")
    public String getGenre() {
        return genre;
    }

    @SuppressWarnings("unused")
    public void setGenre(String genre) {
        this.genre = genre;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo director(String director) {
        this.director = director;
        return this;
    }

    /**
     * Movie Director
     *
     * @return director
     **/
    @Schema(description = "Movie Director")
    public String getDirector() {
        return director;
    }

    @SuppressWarnings("unused")
    public void setDirector(String director) {
        this.director = director;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo writer(String writer) {
        this.writer = writer;
        return this;
    }

    /**
     * Movie Writer
     *
     * @return writer
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie Writer")
    public String getWriter() {
        return writer;
    }

    @SuppressWarnings("unused")
    public void setWriter(String writer) {
        this.writer = writer;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo actors(String actors) {
        this.actors = actors;
        return this;
    }

    /**
     * Movie Actors
     *
     * @return actors
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie Actors")
    public String getActors() {
        return actors;
    }

    @SuppressWarnings("unused")
    public void setActors(String actors) {
        this.actors = actors;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo plot(String plot) {
        this.plot = plot;
        return this;
    }

    /**
     * Movie Plot
     *
     * @return plot
     **/
    @Schema(description = "Movie Plot")
    public String getPlot() {
        return plot;
    }

    @SuppressWarnings("unused")
    public void setPlot(String plot) {
        this.plot = plot;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo language(String language) {
        this.language = language;
        return this;
    }

    /**
     * Movie Language
     *
     * @return language
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie Language")
    public String getLanguage() {
        return language;
    }

    @SuppressWarnings("unused")
    public void setLanguage(String language) {
        this.language = language;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo country(String country) {
        this.country = country;
        return this;
    }

    /**
     * Movie Country
     *
     * @return country
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie Country")
    public String getCountry() {
        return country;
    }

    @SuppressWarnings("unused")
    public void setCountry(String country) {
        this.country = country;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo awards(String awards) {
        this.awards = awards;
        return this;
    }

    /**
     * Movie Awards
     *
     * @return awards
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie Awards")
    public String getAwards() {
        return awards;
    }

    @SuppressWarnings("unused")
    public void setAwards(String awards) {
        this.awards = awards;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo poster(String poster) {
        this.poster = poster;
        return this;
    }

    /**
     * Movie Poster URL
     *
     * @return poster
     **/
    @Schema(description = "Movie Poster URL")
    public String getPoster() {
        if (poster.equalsIgnoreCase("N/A")) {
            return ConfigProvider.BOT_SETTINGS.noPosterImageUrl();
        } else return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo ratings(List<OmdbMovieRating> ratings) {
        this.ratings = ratings;
        return this;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo addRatingsItem(OmdbMovieRating ratingsItem) {
        if (this.ratings == null) {
            this.ratings = new ArrayList<OmdbMovieRating>();
        }
        this.ratings.add(ratingsItem);
        return this;
    }

    /**
     * Movie Ratings
     *
     * @return ratings
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie Ratings")
    public List<OmdbMovieRating> getRatings() {
        return ratings;
    }

    @SuppressWarnings("unused")
    public void setRatings(List<OmdbMovieRating> ratings) {
        this.ratings = ratings;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo metascore(String metascore) {
        this.metascore = metascore;
        return this;
    }

    /**
     * Movie Metascore
     *
     * @return metascore
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie Metascore")
    public String getMetascore() {
        return metascore;
    }

    @SuppressWarnings("unused")
    public void setMetascore(String metascore) {
        this.metascore = metascore;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo imdbRating(String imdbRating) {
        this.imdbRating = imdbRating;
        return this;
    }

    /**
     * Movie IMDB Ratings
     *
     * @return imdbRating
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie IMDB Ratings")
    public String getImdbRating() {
        return imdbRating;
    }

    @SuppressWarnings("unused")
    public void setImdbRating(String imdbRating) {
        this.imdbRating = imdbRating;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo imdbVotes(String imdbVotes) {
        this.imdbVotes = imdbVotes;
        return this;
    }

    /**
     * Movie IMDB Votes
     *
     * @return imdbVotes
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie IMDB Votes")
    public String getImdbVotes() {
        return imdbVotes;
    }

    @SuppressWarnings("unused")
    public void setImdbVotes(String imdbVotes) {
        this.imdbVotes = imdbVotes;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo imdbID(String imdbID) {
        this.imdbID = imdbID;
        return this;
    }

    /**
     * Movie IMDB ID
     *
     * @return imdbID
     **/
    @Schema(description = "Movie IMDB ID")
    public String getImdbID() {
        return imdbID;
    }

    @SuppressWarnings("unused")
    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo type(String type) {
        this.type = type;
        return this;
    }

    /**
     * Movie Type
     *
     * @return type
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie Type")
    public String getType() {
        return type;
    }

    @SuppressWarnings("unused")
    public void setType(String type) {
        this.type = type;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo DVD(String DVD) {
        this.DVD = DVD;
        return this;
    }

    /**
     * Movie DVD Sales
     *
     * @return DVD
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie DVD Sales")
    public String getDVD() {
        return DVD;
    }

    @SuppressWarnings("unused")
    public void setDVD(String DVD) {
        this.DVD = DVD;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo boxOffice(String boxOffice) {
        this.boxOffice = boxOffice;
        return this;
    }

    /**
     * Movie Box Office Sales
     *
     * @return boxOffice
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie Box Office Sales")
    public String getBoxOffice() {
        return boxOffice;
    }

    @SuppressWarnings("unused")
    public void setBoxOffice(String boxOffice) {
        this.boxOffice = boxOffice;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo production(String production) {
        this.production = production;
        return this;
    }

    /**
     * Movie Production Cost
     *
     * @return production
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie Production Cost")
    public String getProduction() {
        return production;
    }

    @SuppressWarnings("unused")
    public void setProduction(String production) {
        this.production = production;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo website(String website) {
        this.website = website;
        return this;
    }

    /**
     * Movie Website
     *
     * @return website
     **/
    @SuppressWarnings("unused")
    @Schema(description = "Movie Website")
    public String getWebsite() {
        return website;
    }

    @SuppressWarnings("unused")
    public void setWebsite(String website) {
        this.website = website;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo response(ResponseEnum response) {
        this.response = response;
        return this;
    }

    /**
     * API Response Success State
     *
     * @return response
     **/
    @Schema(description = "API Response Success State")
    public ResponseEnum getResponse() {
        return response;
    }

    @SuppressWarnings("unused")
    public void setResponse(ResponseEnum response) {
        this.response = response;
    }

    @SuppressWarnings("unused")
    public OmdbMovieInfo error(String error) {
        this.error = error;
        return this;
    }

    /**
     * API Error Message (if error occurred)
     *
     * @return error
     **/
    @SuppressWarnings("unused")
    @Schema(description = "API Error Message (if error occurred)")
    public String getError() {
        return error;
    }

    @SuppressWarnings("unused")
    public void setError(String error) {
        this.error = error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OmdbMovieInfo omdbMovieInfo = (OmdbMovieInfo) o;
        return Objects.equals(this.title, omdbMovieInfo.title) &&
                Objects.equals(this.year, omdbMovieInfo.year) &&
                Objects.equals(this.rated, omdbMovieInfo.rated) &&
                Objects.equals(this.released, omdbMovieInfo.released) &&
                Objects.equals(this.runtime, omdbMovieInfo.runtime) &&
                Objects.equals(this.genre, omdbMovieInfo.genre) &&
                Objects.equals(this.director, omdbMovieInfo.director) &&
                Objects.equals(this.writer, omdbMovieInfo.writer) &&
                Objects.equals(this.actors, omdbMovieInfo.actors) &&
                Objects.equals(this.plot, omdbMovieInfo.plot) &&
                Objects.equals(this.language, omdbMovieInfo.language) &&
                Objects.equals(this.country, omdbMovieInfo.country) &&
                Objects.equals(this.awards, omdbMovieInfo.awards) &&
                Objects.equals(this.poster, omdbMovieInfo.poster) &&
                Objects.equals(this.ratings, omdbMovieInfo.ratings) &&
                Objects.equals(this.metascore, omdbMovieInfo.metascore) &&
                Objects.equals(this.imdbRating, omdbMovieInfo.imdbRating) &&
                Objects.equals(this.imdbVotes, omdbMovieInfo.imdbVotes) &&
                Objects.equals(this.imdbID, omdbMovieInfo.imdbID) &&
                Objects.equals(this.type, omdbMovieInfo.type) &&
                Objects.equals(this.DVD, omdbMovieInfo.DVD) &&
                Objects.equals(this.boxOffice, omdbMovieInfo.boxOffice) &&
                Objects.equals(this.production, omdbMovieInfo.production) &&
                Objects.equals(this.website, omdbMovieInfo.website) &&
                Objects.equals(this.response, omdbMovieInfo.response) &&
                Objects.equals(this.error, omdbMovieInfo.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, year, rated, released, runtime, genre, director, writer, actors, plot, language, country, awards, poster, ratings, metascore, imdbRating, imdbVotes, imdbID, type, DVD, boxOffice, production, website, response, error);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OmdbMovieInfo {\n");

        sb.append("    title: ").append(toIndentedString(title)).append("\n");
        sb.append("    year: ").append(toIndentedString(year)).append("\n");
        sb.append("    rated: ").append(toIndentedString(rated)).append("\n");
        sb.append("    released: ").append(toIndentedString(released)).append("\n");
        sb.append("    runtime: ").append(toIndentedString(runtime)).append("\n");
        sb.append("    genre: ").append(toIndentedString(genre)).append("\n");
        sb.append("    director: ").append(toIndentedString(director)).append("\n");
        sb.append("    writer: ").append(toIndentedString(writer)).append("\n");
        sb.append("    actors: ").append(toIndentedString(actors)).append("\n");
        sb.append("    plot: ").append(toIndentedString(plot)).append("\n");
        sb.append("    language: ").append(toIndentedString(language)).append("\n");
        sb.append("    country: ").append(toIndentedString(country)).append("\n");
        sb.append("    awards: ").append(toIndentedString(awards)).append("\n");
        sb.append("    poster: ").append(toIndentedString(poster)).append("\n");
        sb.append("    ratings: ").append(toIndentedString(ratings)).append("\n");
        sb.append("    metascore: ").append(toIndentedString(metascore)).append("\n");
        sb.append("    imdbRating: ").append(toIndentedString(imdbRating)).append("\n");
        sb.append("    imdbVotes: ").append(toIndentedString(imdbVotes)).append("\n");
        sb.append("    imdbID: ").append(toIndentedString(imdbID)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    DVD: ").append(toIndentedString(DVD)).append("\n");
        sb.append("    boxOffice: ").append(toIndentedString(boxOffice)).append("\n");
        sb.append("    production: ").append(toIndentedString(production)).append("\n");
        sb.append("    website: ").append(toIndentedString(website)).append("\n");
        sb.append("    response: ").append(toIndentedString(response)).append("\n");
        sb.append("    error: ").append(toIndentedString(error)).append("\n");
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

    /**
     * API Response Success State
     */
    @JsonAdapter(ResponseEnum.Adapter.class)
    public enum ResponseEnum {
        TRUE("True"),
        FALSE("False");

        private final String value;

        ResponseEnum(String value) {
            this.value = value;
        }

        public static ResponseEnum fromValue(String text) {
            for (ResponseEnum b : ResponseEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static class Adapter extends TypeAdapter<ResponseEnum> {
            @Override
            public void write(final JsonWriter jsonWriter, final ResponseEnum enumeration) throws IOException {
                jsonWriter.value(enumeration.getValue());
            }

            @Override
            public ResponseEnum read(final JsonReader jsonReader) throws IOException {
                String value = jsonReader.nextString();
                return ResponseEnum.fromValue(String.valueOf(value));
            }
        }
    }

}
