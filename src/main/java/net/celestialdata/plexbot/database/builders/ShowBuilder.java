package net.celestialdata.plexbot.database.builders;

import net.celestialdata.plexbot.database.models.Show;

public class ShowBuilder {
    private String imdbCode;
    private String name;
    private String foldername;

    public ShowBuilder withImdbCode(String imdbCode) {
        this.imdbCode = imdbCode;
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
        return new Show(imdbCode, name, foldername);
    }
}
