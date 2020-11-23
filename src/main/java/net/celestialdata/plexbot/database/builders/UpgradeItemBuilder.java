package net.celestialdata.plexbot.database.builders;

import net.celestialdata.plexbot.database.DbOperations;
import net.celestialdata.plexbot.database.models.Movie;
import net.celestialdata.plexbot.database.models.UpgradeItem;

@SuppressWarnings("unused")
public class UpgradeItemBuilder {
    private Movie movie;
    private int newResolution;
    private Long messageId;

    public UpgradeItemBuilder withMovie(Movie movie) {
        this.movie = movie;
        return this;
    }

    public UpgradeItemBuilder withMovie(String imdbCode) {
        this.movie = DbOperations.movieOps.getMovieById(imdbCode);
        return this;
    }

    public UpgradeItemBuilder withNewResolution(int newResolution) {
        this.newResolution = newResolution;
        return this;
    }

    public UpgradeItemBuilder withMessageId(Long messageId) {
        this.messageId = messageId;
        return this;
    }

    public UpgradeItem build() {
        return new UpgradeItem(this.movie, this.newResolution, this.messageId);
    }
}