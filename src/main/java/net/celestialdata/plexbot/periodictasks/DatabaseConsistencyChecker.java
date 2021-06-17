package net.celestialdata.plexbot.periodictasks;

import io.quarkus.arc.log.LoggerName;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.scheduler.Scheduled;
import net.celestialdata.plexbot.entities.Episode;
import net.celestialdata.plexbot.entities.Movie;
import net.celestialdata.plexbot.entities.Season;
import net.celestialdata.plexbot.entities.Show;
import net.celestialdata.plexbot.utilities.BotProcess;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
@ApplicationScoped
public class DatabaseConsistencyChecker extends BotProcess {

    @LoggerName("net.celestialdata.plexbot.periodictasks.DatabaseConsistencyChecker")
    Logger logger;

    @ConfigProperty(name = "FolderSettings.movieFolder")
    String movieFolder;

    @ConfigProperty(name = "FolderSettings.tvFolder")
    String tvFolder;

    @ConfigProperty(name = "BotSettings.ownerID")
    String ownerId;

    @Inject
    DiscordApi discordApi;

    @Inject
    EntityManager entityManager;

    @Inject
    FileUtilities fileUtilities;

    // Helper method to send a warning message to the bot owner
    private void sendWarning(EmbedBuilder message) {
        discordApi.getUserById(ownerId).join().sendMessage(message);
    }

    @Scheduled(every = "168h", delay = 10, delayUnit = TimeUnit.SECONDS)
    public void verifyDatabase() {
        // Configure the process
        configureProcess("Database Consistency Checker: na%");

        // Get the lists of media in the database
        List<Show> shows = Show.listAll();
        List<Season> seasons = Season.listAll();
        List<Episode> episodes = Episode.listAll();
        List<Movie> movies = Movie.listAll();

        // Create items to track progress for logging reasons
        AtomicInteger progress = new AtomicInteger(1);
        AtomicInteger overallProgress = new AtomicInteger(1);
        var totalSize = shows.size() + seasons.size() + episodes.size() + movies.size();
        DecimalFormat decimalFormatter = new DecimalFormat("#0.00");

        // Verify that all the folders for the shows in the DB exist
        shows.forEach(show -> {
            logger.trace("Verifying show (" + progress + "/" + shows.size() + "): " + show.name + " {tvdb-" + show.id + "}");
            updateProcessString("Database Consistency Checker: " +
                    decimalFormatter.format(((double) overallProgress.get() / totalSize) * 100) + "%");

            if (!Files.isDirectory(Path.of(tvFolder + "/" + show.foldername))) {
                logger.warn("Data inconsistency found: Show \"" + show.name + " {tvdb-" + show.id +
                        "}\" could not be found on the filesystem, however it is listed in the database.");

                sendWarning(new EmbedBuilder()
                        .setTitle("Data Inconsistency Found")
                        .setDescription("A inconsistency in the database has been found. The following item is listed in the " +
                                "database but cannot be found on the filesystem.")
                        .addInlineField("Media type:", "Show")
                        .addInlineField("Show ID:", show.id)
                        .addField("Show name:", show.name)
                        .setColor(Color.YELLOW)
                );
            }

            progress.getAndIncrement();
            overallProgress.getAndIncrement();
        });

        // Reset the progress counter and verify that all season folders exist
        progress.set(1);
        seasons.forEach(season -> {
            logger.trace("Verifying season (" + progress + "/" + seasons.size() + "): Season " + season.number + " - " + season.show.name);
            updateProcessString("Database Consistency Checker: " +
                    decimalFormatter.format(((double) overallProgress.get() / totalSize) * 100) + "%");

            if (!Files.isDirectory(Path.of(tvFolder + "/" + season.show.foldername + "/" + season.foldername))) {
                logger.warn("Data inconsistency found: Season " + season.number + " of " + season.show.name +
                        " could not be found on the filesystem, however it is listed in the database.");

                sendWarning(new EmbedBuilder()
                        .setTitle("Data Inconsistency Found")
                        .setDescription("A inconsistency in the database has been found. The following item is listed in the " +
                                "database but cannot be found on the filesystem.")
                        .addInlineField("Media type:", "Season")
                        .addInlineField("Season #:", String.valueOf(season.number))
                        .addInlineField("Season ID:", String.valueOf(season.id))
                        .addField("Associated show:", season.show.name)
                        .setColor(Color.YELLOW)
                );
            }

            progress.getAndIncrement();
            overallProgress.getAndIncrement();
        });

        // Reset the progress counter and verify that all episodes exist
        progress.set(1);
        episodes.forEach(episode -> {
            logger.trace("Verifying episode (" + progress + "/" + episodes.size() + "): s" + episode.season.number +
                    "e" + episode.number + " - " + episode.show.name);
            updateProcessString("Database Consistency Checker: " +
                    decimalFormatter.format(((double) overallProgress.get() / totalSize) * 100) + "%");
            verifyEpisode(episode);
            progress.getAndIncrement();
            overallProgress.getAndIncrement();
        });


        // Reset the progress counter and verify that all movies exist
        progress.set(1);
        movies.forEach(movie -> {
            logger.trace("Verifying movie (" + progress + "/" + movies.size() + "): " + movie.title + " (" + movie.year + ") {imdb-" + movie.id + "}");
            updateProcessString("Database Consistency Checker: " +
                    decimalFormatter.format(((double) overallProgress.get() / totalSize) * 100) + "%");
            verifyMovie(movie);
            progress.getAndIncrement();
            overallProgress.getAndIncrement();
        });

        endProcess();
    }

    @Transactional
    @TransactionConfiguration(timeout = 120)
    public void verifyEpisode(Episode episode) {
        if (Files.notExists(Path.of(tvFolder + "/" + episode.show.foldername + "/" + episode.season.foldername + "/" + episode.filename))) {
            logger.warn("Data inconsistency found: Episode " + episode.number + " of season " + episode.season.number + " of " + episode.show.name +
                    " could not be found on the filesystem, however it is listed in the database.");

            sendWarning(new EmbedBuilder()
                    .setTitle("Data Inconsistency Found")
                    .setDescription("A inconsistency in the database has been found. The following item is listed in the " +
                            "database but cannot be found on the filesystem.")
                    .addInlineField("Media type:", "Episode")
                    .addInlineField("Episode ID:", episode.id)
                    .addInlineField("Episode #:", String.valueOf(episode.number))
                    .addInlineField("Season #:", String.valueOf(episode.season.number))
                    .addField("Episode title:", episode.title)
                    .addField("Associated show:", episode.show.name)
                    .setColor(Color.YELLOW)
            );
        } else {
            // Ensure information about the media file is up to date
            var mediaFileData = fileUtilities.getMediaInfo(tvFolder + "/" + episode.show.foldername + "/" + episode.season.foldername + "/" + episode.filename);

            // Update movie data
            episode.codec = mediaFileData.codec;
            episode.isOptimized = mediaFileData.isOptimized();
            episode.width = mediaFileData.width;
            episode.height = mediaFileData.height;
            episode.resolution = mediaFileData.resolution();
            try {
                episode.duration = mediaFileData.duration;
            } catch (StringIndexOutOfBoundsException e) {
                logger.error("Unable to get information about this media file - " + episode.filename, e);

                sendWarning(new EmbedBuilder()
                        .setTitle("Error fetching media information")
                        .setDescription("An error occurred while fetching information about the following media during the " +
                                "database consistency verifier. Please make sure this media file is not corrupted.")
                        .addInlineField("Media type:", "Episode")
                        .addInlineField("Episode ID:", episode.id)
                        .addInlineField("Episode #:", String.valueOf(episode.number))
                        .addInlineField("Season #:", String.valueOf(episode.season.number))
                        .addField("Episode title:", episode.title)
                        .addField("Associated show:", episode.show.name)
                        .setColor(Color.YELLOW)
                );
            }

            // Update the episode in the database
            entityManager.merge(episode).persist();
        }
    }

    @Transactional
    @TransactionConfiguration(timeout = 120)
    public void verifyMovie(Movie movie) {
        if (Files.notExists(Path.of(movieFolder + "/" + movie.folderName + "/" + movie.filename))) {
            logger.warn("Data inconsistency found: Movie \"" + movie.title + " (" + movie.year + ") {imdb-" + movie.id +
                    "}\" could not be found on the filesystem, however it is listed in the database.");

            sendWarning(new EmbedBuilder()
                    .setTitle("Data Inconsistency Found")
                    .setDescription("A inconsistency in the database has been found. The following item is listed in the " +
                            "database but cannot be found on the filesystem.")
                    .addInlineField("Media type:", "Movie")
                    .addInlineField("Media ID:", movie.id)
                    .addField("Media name:", movie.title)
                    .setColor(Color.YELLOW)
            );
        } else {
            // Ensure information about the media file is up to date
            var mediaFileData = fileUtilities.getMediaInfo(movieFolder + "/" + movie.folderName + "/" + movie.filename);

            // Update movie data
            movie.codec = mediaFileData.codec;
            movie.isOptimized = mediaFileData.isOptimized();
            movie.width = mediaFileData.width;
            movie.height = mediaFileData.height;
            movie.resolution = mediaFileData.resolution();
            try {
                movie.duration = mediaFileData.duration;
            } catch (StringIndexOutOfBoundsException e) {
                logger.error("Unable to get information about this media file - " + movie.filename, e);

                sendWarning(new EmbedBuilder()
                        .setTitle("Error fetching media information")
                        .setDescription("An error occurred while fetching information about the following media during the " +
                                "database consistency verifier. Please make sure this media file is not corrupted.")
                        .addInlineField("Media type:", "Movie")
                        .addInlineField("Media ID:", movie.id)
                        .addField("Media name:", movie.title)
                        .setColor(Color.YELLOW)
                );
            }

            // Update the movie in the database
            entityManager.merge(movie).persist();
        }
    }
}