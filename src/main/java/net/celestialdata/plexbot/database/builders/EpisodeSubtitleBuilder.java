package net.celestialdata.plexbot.database.builders;

import net.celestialdata.plexbot.database.models.Episode;
import net.celestialdata.plexbot.database.models.EpisodeSubtitle;

public class EpisodeSubtitleBuilder {
    private Episode episode;
    private String languageCode;
    private String filetype;
    private String filename;
    private boolean forced;

    public EpisodeSubtitleBuilder withEpisode(Episode episode) {
        this.episode = episode;
        return this;
    }

    public EpisodeSubtitleBuilder withLanguageCode(String languageCode) {
        this.languageCode = languageCode;
        return this;
    }

    public EpisodeSubtitleBuilder withFiletype(String filetype) {
        this.filetype = filetype;
        return this;
    }

    public EpisodeSubtitleBuilder withFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public EpisodeSubtitleBuilder withForced(boolean forced) {
        this.forced = forced;
        return this;
    }

    public EpisodeSubtitle build() {
        return new EpisodeSubtitle(episode, languageCode, filetype, filename, forced);
    }
}
