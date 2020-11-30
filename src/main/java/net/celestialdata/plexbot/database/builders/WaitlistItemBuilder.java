package net.celestialdata.plexbot.database.builders;

import net.celestialdata.plexbot.client.model.OmdbMovieInfo;
import net.celestialdata.plexbot.database.DbOperations;
import net.celestialdata.plexbot.database.models.User;
import net.celestialdata.plexbot.database.models.WaitlistItem;

@SuppressWarnings("unused")
public class WaitlistItemBuilder {
    private String id;
    private String title;
    private String year;
    private User requestedBy;
    private Long messageId;

    public WaitlistItemBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public WaitlistItemBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public WaitlistItemBuilder withYear(String year) {
        this.year = year;
        return this;
    }

    public WaitlistItemBuilder withRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
        return this;
    }

    public WaitlistItemBuilder withRequestedBy(Long userId) {
        this.requestedBy = DbOperations.userOps.getUserById(userId);
        return this;
    }

    public WaitlistItemBuilder withMessageId(Long messageId) {
        this.messageId = messageId;
        return this;
    }

    public WaitlistItemBuilder fromOmdbInfo(OmdbMovieInfo movieInfo) {
        this.id = movieInfo.getImdbID();
        this.title = movieInfo.getTitle();
        this.year = movieInfo.getYear();
        return this;
    }

    public WaitlistItem build() {
        return new WaitlistItem(this.id, this.title, this.year, this.requestedBy, this.messageId);
    }
}