package net.celestialdata.plexbot.database.builders;

import net.celestialdata.plexbot.database.models.Season;
import net.celestialdata.plexbot.database.models.Show;

public class SeasonBuilder {
    private Show show;
    private int number;
    private String foldername;

    public SeasonBuilder withShow(Show show) {
        this.show = show;
        return this;
    }

    public SeasonBuilder withNumber(int number) {
        this.number = number;
        return this;
    }

    public SeasonBuilder withFoldername(String foldername) {
        this.foldername = foldername;
        return this;
    }

    public Season build() {
        return new Season(show, number, foldername);
    }
}
