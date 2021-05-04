package net.celestialdata.plexbot.database.builders;

import net.celestialdata.plexbot.database.models.Episode;
import net.celestialdata.plexbot.database.models.Season;
import net.celestialdata.plexbot.database.models.Show;

public class EpisodeBuilder {
    private String tvdbId;
    private Show show;
    private Season season;
    private String title;
    private int number;
    private String date;
    private int width;
    private int height;
    private int resolution;
    private String filetype;
    private String filename;

    public EpisodeBuilder withTvdbId(String tvdbId) {
        this.tvdbId = tvdbId;
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

    public EpisodeBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public EpisodeBuilder withNumber(int number) {
        this.number = number;
        return this;
    }

    public EpisodeBuilder withDate(String date) {
        this.date = date;
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

    public EpisodeBuilder withResolution(int resolution) {
        this.resolution = resolution;
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
        return new Episode(tvdbId, show, season, title, number, date, width, height, resolution, filetype, filename);
    }
}
