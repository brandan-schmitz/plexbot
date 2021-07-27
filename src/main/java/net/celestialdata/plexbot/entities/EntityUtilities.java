package net.celestialdata.plexbot.entities;

import net.celestialdata.plexbot.clients.models.omdb.OmdbResult;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbExtendedEpisode;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbSeries;
import net.celestialdata.plexbot.dataobjects.ParsedSubtitleFilename;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.enumerators.FileType;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.awt.*;
import java.io.File;
import java.util.List;

@ApplicationScoped
public class EntityUtilities {

    @ConfigProperty(name = "ChannelSettings.movieWaitlistChannel")
    String movieWaitlistChannel;

    @ConfigProperty(name = "ChannelSettings.corruptedNotificationChannel")
    String corruptedNotificationChannel;

    @ConfigProperty(name = "ChannelSettings.upgradeApprovalChannel")
    String upgradeApprovalChannel;

    @ConfigProperty(name = "FolderSettings.movieFolder")
    String movieFolder;

    @ConfigProperty(name = "FolderSettings.tvFolder")
    String tvFolder;

    @Inject
    MessageFormatter messageFormatter;

    @Inject
    DiscordApi discordApi;

    @Inject
    FileUtilities fileUtilities;

    @Inject
    EntityManager entityManager;

    @Transactional
    public List<Movie> getAllMovies() {
        return Movie.listAll();
    }

    @Transactional
    public List<MovieSubtitle> getAllMovieSubtitles() {
        return MovieSubtitle.listAll();
    }

    @Transactional
    public List<MovieSubtitle> getSubtitlesByMovie(String movieId) {
        Movie movie = Movie.findById(movieId);
        return MovieSubtitle.list("movie", movie);
    }

    @Transactional
    public void deleteMovieSubtitle(int subtitleId) {
        MovieSubtitle entity = MovieSubtitle.findById(subtitleId);

        if (entity != null) {
            entity.delete();
        }
    }

    @Transactional
    public List<Episode> getAllEpisodes() {
        return Episode.listAll();
    }

    @Transactional
    public List<EpisodeSubtitle> getAllEpisodeSubtitles() {
        return EpisodeSubtitle.listAll();
    }

    @Transactional
    public List<EpisodeSubtitle> getSubtitlesByEpisode(String episodeId) {
        Episode episode = Episode.findById(episodeId);
        return EpisodeSubtitle.list("episode", episode);
    }

    @Transactional
    public void deleteEpisodeSubtitle(int subtitleId) {
        EpisodeSubtitle entity = EpisodeSubtitle.findById(subtitleId);

        if (entity != null) {
            entity.delete();
        }
    }

    @Transactional
    public List<Show> getAllShows() {
        return Show.listAll();
    }

    @Transactional
    public boolean episodeExists(String id) {
        return Episode.count("id", id) == 1;
    }

    @Transactional
    public Episode getEpisode(String id) {
        return Episode.findById(id);
    }

    @Transactional
    public void addOrUpdateEpisode(TvdbExtendedEpisode episodeData, String filename, Show show) {
        var episodeFileData = fileUtilities.getMediaInfo(tvFolder + show.foldername + "/Season " + episodeData.seasonNumber + "/" + filename);
        var fileType = FileType.determineFiletype(filename);
        Episode episode = new Episode();

        episode.id = String.valueOf(episodeData.id);
        episode.title = episodeData.name;
        episode.date = episodeData.aired;
        episode.number = episodeData.number;
        episode.season = String.valueOf(episodeData.seasonNumber);
        episode.show = show;
        episode.filename = filename;
        episode.filetype = fileType.getTypeString();
        episode.height = episodeFileData.height;
        episode.width = episodeFileData.width;
        episode.duration = episodeFileData.duration;
        episode.codec = episodeFileData.codec;
        episode.resolution = episodeFileData.resolution();
        episode.isOptimized = episodeFileData.isOptimized();

        entityManager.merge(episode).persist();

        if (!episode.isOptimized) {
            addOrUpdateEncodingQueueItem("episode", episode.id);
        }
    }

    @Transactional
    public boolean movieSubtitleExists(String filename) {
        return MovieSubtitle.count("filename", filename) == 1;
    }

    @Transactional
    public boolean episodeSubtitleExists(String filename) {
        return EpisodeSubtitle.count("filename", filename) == 1;
    }

    @Transactional
    public EpisodeSubtitle findEpisodeSubtitle(String filename) {
        return EpisodeSubtitle.find("filename", filename).firstResult();
    }

    @SuppressWarnings("DuplicatedCode")
    @Transactional
    public void addOrUpdateEpisodeSubtitle(String filename, ParsedSubtitleFilename parsedFilename, Episode linkedEpisode) {
        EpisodeSubtitle episodeSubtitle = new EpisodeSubtitle();

        episodeSubtitle.language = parsedFilename.language;
        episodeSubtitle.episode = linkedEpisode;
        episodeSubtitle.filename = filename;
        episodeSubtitle.filetype = FileType.determineFiletype(filename).getTypeString();
        episodeSubtitle.isForced = parsedFilename.isForced;
        episodeSubtitle.isSDH = parsedFilename.isSDH;
        episodeSubtitle.isCC = parsedFilename.isCC;

        if (episodeSubtitleExists(filename)) {
            episodeSubtitle.id = findEpisodeSubtitle(filename).id;
        }

        entityManager.merge(episodeSubtitle).persist();
    }

    @Transactional
    public Show findSeries(String id) {
        return Show.findById(id);
    }

    @Transactional
    public void addOrUpdateSeries(TvdbSeries seriesToAdd, String foldername) {
        Show show = new Show();

        show.id = String.valueOf(seriesToAdd.id);
        show.name = seriesToAdd.name;
        show.foldername = foldername;

        entityManager.merge(show).persist();
    }

    @Transactional
    public boolean movieExists(String id) {
        return Movie.count("id", id) == 1;
    }

    @Transactional
    public Movie getMovie(String id) {
        return Movie.findById(id);
    }

    @Transactional
    public void addOrUpdateMovie(OmdbResult movieToAdd, String filename) {
        var movieFileData = fileUtilities.getMediaInfo(movieFolder + fileUtilities.generatePathname(movieToAdd) + "/" + filename);
        var fileType = FileType.determineFiletype(filename);
        Movie movie = new Movie();

        movie.id = movieToAdd.imdbID;
        movie.title = movieToAdd.title;
        movie.year = movieToAdd.year;
        movie.resolution = movieFileData.resolution();
        movie.height = movieFileData.resolution();
        movie.width = movieFileData.width;
        movie.duration = movieFileData.duration;
        movie.codec = movieFileData.codec;
        movie.filename = filename;
        movie.filetype = fileType.getTypeString();
        movie.folderName = fileUtilities.generatePathname(movieToAdd);
        movie.isOptimized = movieFileData.isOptimized();

        // If the movie is already listed in the database, update it versus adding a new one
        entityManager.merge(movie).persist();

        if (!movie.isOptimized) {
            addOrUpdateEncodingQueueItem("movie", movie.id);
        }
    }


    @SuppressWarnings("DuplicatedCode")
    @Transactional
    public void addOrUpdateMovieSubtitle(String filename, ParsedSubtitleFilename parsedFilename, Movie linkedMovie) {
        MovieSubtitle movieSubtitle = new MovieSubtitle();

        movieSubtitle.language = parsedFilename.language;
        movieSubtitle.movie = linkedMovie;
        movieSubtitle.filename = filename;
        movieSubtitle.filetype = FileType.determineFiletype(filename).getTypeString();
        movieSubtitle.isForced = parsedFilename.isForced;
        movieSubtitle.isSDH = parsedFilename.isSDH;
        movieSubtitle.isCC = parsedFilename.isCC;

        if (episodeSubtitleExists(filename)) {
            movieSubtitle.id = findEpisodeSubtitle(filename).id;
        }

        entityManager.merge(movieSubtitle).persist();
    }

    @Transactional
    public boolean encodingQueueItemExists(String mediaId) {
        return EncodingQueueItem.count("mediaId", mediaId) == 1;
    }

    @Transactional
    public EncodingQueueItem findEncodingQueueItem(String mediaId) {
        return EncodingQueueItem.find("mediaId", mediaId).firstResult();
    }

    @Transactional
    public void addOrUpdateEncodingQueueItem(String itemType, String mediaId) {
        EncodingQueueItem encodingQueueItem = new EncodingQueueItem();

        encodingQueueItem.type = itemType;
        encodingQueueItem.mediaId = mediaId;

        if (encodingQueueItemExists(mediaId)) {
            encodingQueueItem.id = findEncodingQueueItem(mediaId).id;
        }

        entityManager.merge(encodingQueueItem).persist();
    }

    @Transactional
    public boolean waitlistMovieExists(String id) {
        return WaitlistMovie.count("id", id) == 1;
    }

    @Transactional
    public WaitlistMovie getWaitlistMovie(String id) {
        return WaitlistMovie.findById(id);
    }

    @Transactional
    public void addWaitlistMovie(OmdbResult movie, Long requestedBy) {
        WaitlistMovie waitlistMovie = new WaitlistMovie();
        var waitlistMessage = new MessageBuilder()
                .setEmbed(messageFormatter.waitlistNotification(movie))
                .send(discordApi.getTextChannelById(movieWaitlistChannel).orElseThrow())
                .exceptionally(ExceptionLogger.get()).join();

        waitlistMovie.id = movie.imdbID;
        waitlistMovie.title = movie.title;
        waitlistMovie.year = movie.year;
        waitlistMovie.requestedBy = requestedBy;
        waitlistMovie.messageId = waitlistMessage.getId();

        waitlistMovie.persist();
    }

    @Transactional
    public void deleteWaitlistMovie(String id) {
        // Fetch the instance of this movie
        WaitlistMovie waitlistMovie = WaitlistMovie.findById(id);

        // Delete the message from the channel
        discordApi.getTextChannelById(movieWaitlistChannel).ifPresent(textChannel ->
                textChannel.getMessageById(waitlistMovie.messageId).join()
                        .delete().exceptionally(ExceptionLogger.get()
                )
        );

        // Delete the movie
        waitlistMovie.delete();
    }

    @Transactional
    public List<UpgradableMovie> getAllUpgradableMovies() {
        return UpgradableMovie.listAll();
    }

    @Transactional
    public boolean upgradableMovieExists(String id) {
        return UpgradableMovie.count("id", id) == 1;
    }

    @Transactional
    public void addUpgradeableMovie(OmdbResult movie, int oldResolution, int newResolution) {
        UpgradableMovie upgradableMovie = new UpgradableMovie();
        var upgradeMessage = new MessageBuilder()
                .setEmbed(messageFormatter.upgradeApprovalMessage(movie, oldResolution, newResolution))
                .addComponents(ActionRow.of(
                        Button.success("approve-upgrade-" + movie.imdbID, "Upgrade"),
                        Button.danger("ignore-upgrade-" + movie.imdbID, "Ignore")
                ))
                .send(discordApi.getTextChannelById(upgradeApprovalChannel).orElseThrow())
                .exceptionally(ExceptionLogger.get()).join();

        upgradableMovie.id = movie.imdbID;
        upgradableMovie.title = movie.title;
        upgradableMovie.year = movie.year;
        upgradableMovie.resolution = oldResolution;
        upgradableMovie.newResolution = newResolution;
        upgradableMovie.messageId = upgradeMessage.getId();

        upgradableMovie.persist();
    }

    @Transactional
    public boolean corruptedMediaItemExists(String path) {
        return CorruptedMediaItem.count("path", path) == 1;
    }

    @Transactional
    public CorruptedMediaItem getCorruptedMediaItemByMessage(String messageId) {
        return CorruptedMediaItem.find("messageId", messageId).firstResult();
    }

    @Transactional
    public void addCorruptedMediaItem(String mediaType, File mediaFile) {
        if (!corruptedMediaItemExists(mediaFile.getAbsolutePath())) {
            CorruptedMediaItem corruptedMediaItem = new CorruptedMediaItem();
            var corruptedMessage = new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Corrupted Media File Detected:")
                            .setDescription("While analyzing the following file, it was determined that the file is corrupted.  " +
                                    "This file should be replaced with a non-corrupted version.")
                            .addInlineField("Media Type:", "```" + mediaType + "```")
                            .addField("Media Filename:", "```" + mediaFile.getName() + "```")
                            .setColor(Color.RED))
                    .addComponents(ActionRow.of(
                            Button.create("recheck-corrupted-file", ButtonStyle.DANGER, "Check Again")
                    ))
                    .send(discordApi.getTextChannelById(corruptedNotificationChannel).orElseThrow())
                    .exceptionally(ExceptionLogger.get()).join();

            corruptedMediaItem.messageId = corruptedMessage.getIdAsString();
            corruptedMediaItem.type = mediaType;
            corruptedMediaItem.path = mediaFile.getAbsolutePath();

            entityManager.merge(corruptedMediaItem).persist();
        }
    }

    @Transactional
    public void deleteCorruptedMediaItemByMessage (String messageId) {
        CorruptedMediaItem corruptedMediaItem = CorruptedMediaItem.find("messageId", messageId).firstResult();

        // Delete the message from the channel
        discordApi.getTextChannelById(corruptedNotificationChannel).ifPresent(textChannel ->
                textChannel.getMessageById(messageId).join()
                        .delete().exceptionally(ExceptionLogger.get()
                )
        );

        // Delete the corrupted media item from the database
        corruptedMediaItem.delete();
    }

    @Transactional
    public void deleteUpgradeMovie(String id) {
        // Fetch the instance of this movie
        UpgradableMovie upgradableMovie = UpgradableMovie.findById(id);

        // Delete the message from the channel
        discordApi.getTextChannelById(upgradeApprovalChannel).ifPresent(textChannel ->
                textChannel.getMessageById(upgradableMovie.messageId).join()
                        .delete().exceptionally(ExceptionLogger.get()
                )
        );

        // Delete the movie
        upgradableMovie.delete();
    }
}