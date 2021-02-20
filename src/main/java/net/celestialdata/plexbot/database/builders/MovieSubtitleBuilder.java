package net.celestialdata.plexbot.database.builders;

import net.celestialdata.plexbot.database.models.Movie;
import net.celestialdata.plexbot.database.models.MovieSubtitle;

public class MovieSubtitleBuilder {
    private Movie movie;
    private String languageCode;
    private String filetype;
    private String filename;

    public MovieSubtitleBuilder withMovie(Movie movie) {
        this.movie = movie;
        return this;
    }

    public MovieSubtitleBuilder withLanguageCode(String languageCode) {
        this.languageCode = languageCode;
        return this;
    }

    public MovieSubtitleBuilder withFiletype(String filetype) {
        this.filetype = filetype;
        return this;
    }

    public MovieSubtitleBuilder withFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public MovieSubtitle build() {
        return new MovieSubtitle(movie, languageCode, filetype, filename);
    }
}