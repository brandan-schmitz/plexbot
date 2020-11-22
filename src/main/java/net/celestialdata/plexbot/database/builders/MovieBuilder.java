package net.celestialdata.plexbot.database.builders;

import net.celestialdata.plexbot.database.models.Movie;

public class MovieBuilder {
    private String id;
    private String title;
    private String year;
    private int resolution;
    private String filename;

    public MovieBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public MovieBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public MovieBuilder withYear(String year) {
        this.year = year;
        return this;
    }

    public MovieBuilder withResolution(int resolution) {
        this.resolution = resolution;
        return this;
    }

    public MovieBuilder withFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public Movie build() {
        return new Movie(this.id, this.title, this.year, this.resolution, this.filename);
    }
}
