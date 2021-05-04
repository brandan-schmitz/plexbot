package net.celestialdata.plexbot.database.builders;

import net.celestialdata.plexbot.database.models.Show;

public class ShowBuilder {
    private String tvdbId;
    private String name;
    private String foldername;

    public ShowBuilder withTvdbId(String imdbCode) {
        this.tvdbId = imdbCode;
        return this;
    }

    public ShowBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ShowBuilder withFoldername(String foldername) {
        this.foldername = foldername;
        return this;
    }

    public Show build() {
        return new Show(tvdbId, name, foldername);
    }
}
