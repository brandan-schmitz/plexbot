package net.celestialdata.plexbot.entities;

import net.celestialdata.plexbot.clients.models.omdb.OmdbResult;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbExtendedEpisode;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbSeries;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.enumerators.FileType;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@ApplicationScoped
public class EntityUtilities {

    @ConfigProperty(name = "ChannelSettings.movieWaitlistChannel")
    String movieWaitlistChannel;

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
    public void addOrUpdateEpisode(TvdbExtendedEpisode episodeData, String filename, Season season, Show show) {
        var episodeFileData = fileUtilities.getMediaInfo(tvFolder + show.foldername + "/" + season.foldername + "/" + filename);
        var fileType = FileType.determineFiletype(filename);
        Episode episode = new Episode();

        episode.id = String.valueOf(episodeData.id);
        episode.title = episodeData.name;
        episode.date = episodeData.aired;
        episode.number = episodeData.number;
        episode.season = season;
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
    }

    @Transactional
    public Season findSeason(Show show, int seasonNumber) {
        return Season.find("show = ?1 and number = ?2", show, seasonNumber).firstResult();
    }

    @Transactional
    public boolean seasonExists(Show show, int seasonNumber) {
        return Season.count("show = ?1 and number = ?2", show, seasonNumber) == 1;
    }

    @Transactional
    public void addOrUpdateSeason(int seasonNumber, String seasonFoldername, Show show) {
        Season season = new Season();

        season.number = seasonNumber;
        season.foldername = seasonFoldername;
        season.show = show;

        entityManager.merge(season).persist();
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
        if (movieExists(movieToAdd.imdbID)) {
            entityManager.merge(movie).persist();
        } else {
            // Save movie to the database
            movie.persist();
        }
    }

    @Transactional
    public boolean waitlistMovieExists(String id) {
        return WaitlistMovie.count("id", id) == 1;
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
    public boolean upgradableMovieExists(String id) {
        return UpgradableMovie.count("id", id) == 1;
    }

    @Transactional
    public void addUpgradeableMovie(OmdbResult movie, int oldResolution, int newResolution) {
        UpgradableMovie upgradableMovie = new UpgradableMovie();
        var upgradeMessage = new MessageBuilder()
                .setEmbed(messageFormatter.upgradeApprovalMessage(movie, oldResolution, newResolution))
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