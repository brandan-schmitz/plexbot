package net.celestialdata.plexbot.database.builders;

import net.celestialdata.plexbot.database.models.Episode;
import net.celestialdata.plexbot.database.models.Season;
import net.celestialdata.plexbot.database.models.Show;

public class EpisodeBuilder {
    private String imdbCode;
    private Show show;
    private Season season;
    private int number;
    private String year;
    private int width;
    private int height;
    private String filetype;
    private String filename;

    public EpisodeBuilder withImdbCode(String imdbCode) {
        this.imdbCode = imdbCode;
        return this;
    }

    public EpisodeBuilder withShow(Show show) {
        this.show = show;
        return this;
    }

    public EpisodeBuilder withSeason(Season season) {
        this.season = season;
        return this;
    }

    public EpisodeBuilder withNumber(int number) {
        this.number = number;
        return this;
    }

    public EpisodeBuilder withYear(String year) {
        this.year = year;
        return this;
    }

    public EpisodeBuilder withWidth(int width) {
        this.width = width;
        return this;
    }

    public EpisodeBuilder withHeight(int height) {
        this.height = height;
        return this;
    }

    public EpisodeBuilder withFiletype(String filetype) {
        this.filetype = filetype;
        return this;
    }

    public EpisodeBuilder withFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public Episode build() {
        return new Episode(imdbCode, show, season, number, year, width, height, filetype, filename);
    }
}
