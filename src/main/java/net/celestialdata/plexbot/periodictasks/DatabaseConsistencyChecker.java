package net.celestialdata.plexbot.periodictasks;

import io.quarkus.arc.log.LoggerName;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.scheduler.Scheduled;
import net.celestialdata.plexbot.entities.*;
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

    @Inject
    EntityUtilities entityUtilities;

    // Helper method to send a warning message to the bot owner
    private void sendWarning(EmbedBuilder message) {
        discordApi.getUserById(ownerId).join().sendMessage(message);
    }

    @Scheduled(every = "168h", delay = 10, delayUnit = TimeUnit.SECONDS)
    public void verifyDatabase() {
        // Configure the process
        configureProcess("Database Consistency Checker - na%");

        // Get the lists of media in the database
        List<Show> shows = Show.listAll();
        List<Episode> episodes = Episode.listAll();
        List<Movie> movies = Movie.listAll();

        // Create items to track progress for logging reasons
        AtomicInteger progress = new AtomicInteger(1);
        AtomicInteger overallProgress = new AtomicInteger(1);
        var totalSize = shows.size() + episodes.size() + movies.size();
        DecimalFormat decimalFormatter = new DecimalFormat("#0.00");

        // Verify that all the folders for the shows in the DB exist
        shows.forEach(show -> {
            logger.trace("Verifying show (" + progress + "/" + shows.size() + "): " + show.name + " {tvdb-" + show.id + "}");
            updateProcessString("Database Consistency Checker - " +
                    decimalFormatter.format(((double) overallProgress.get() / totalSize) * 100) + "%");

            if (!Files.isDirectory(Path.of(tvFolder + "/" + show.foldername))) {
                logger.warn("Data inconsistency found: Show \"" + show.name + " {tvdb-" + show.id +
                        "}\" could not be found on the filesystem, however it is listed in the database.");

                sendWarning(new EmbedBuilder()
                        .setTitle("Data Inconsistency Found")
                        .setDescription("A inconsistency in the database has been found. The following item is listed in the " +
                                "database but cannot be found on the filesystem.")
                        .addInlineField("Media type:", "```Show```")
                        .addInlineField("Show ID:", "```" + show.id + "```")
                        .addInlineField("Show name:", "```" + show.name + "```")
                        .setColor(Color.YELLOW)
                );
            }

            progress.getAndIncrement();
            overallProgress.getAndIncrement();
        });

        // Reset the progress counter and verify that all episodes exist
        progress.set(1);
        episodes.forEach(episode -> {
            logger.trace("Verifying episode (" + progress + "/" + episodes.size() + "): s" + episode.season +
                    "e" + episode.number + " - " + episode.show.name);
            updateProcessString("Database Consistency Checker - " +
                    decimalFormatter.format(((double) overallProgress.get() / totalSize) * 100) + "%");
            verifyEpisode(episode);
            progress.getAndIncrement();
            overallProgress.getAndIncrement();
        });


        // Reset the progress counter and verify that all movies exist
        progress.set(1);
        movies.forEach(movie -> {
            logger.trace("Verifying movie (" + progress + "/" + movies.size() + "): " + movie.title + " (" + movie.year + ") {imdb-" + movie.id + "}");
            updateProcessString("Database Consistency Checker - " +
                    decimalFormatter.format(((double) overallProgress.get() / totalSize) * 100) + "%");
            verifyMovie(movie);
            progress.getAndIncrement();
            overallProgress.getAndIncrement();
        });

        endProcess();
    }

    @SuppressWarnings("DuplicatedCode")
    @Transactional
    @TransactionConfiguration(timeout = 120)
    public void verifyEpisode(Episode episode) {
        try {
            // Ensure we get a updated copy of the episode from the DB in the event it was upgraded while this was processing
            episode = entityUtilities.getEpisode(episode.id);

            if (Files.notExists(Path.of(tvFolder + "/" + episode.show.foldername + "/Season " + episode.season + "/" + episode.filename))) {
                logger.warn("Data inconsistency found: Episode " + episode.number + " of season " + episode.season + " of " + episode.show.name +
                        " could not be found on the filesystem, however it is listed in the database.");

                sendWarning(new EmbedBuilder()
                        .setTitle("Data Inconsistency Found")
                        .setDescription("A inconsistency in the database has been found. The following item is listed in the " +
                                "database but cannot be found on the filesystem.")
                        .addInlineField("Media type:", "```Episode```")
                        .addInlineField("Episode ID:", "```" + episode.id + "```")
                        .addInlineField("Episode #:", "```" + episode.number + "```")
                        .addInlineField("Season #:", "```" + episode.season + "```")
                        .addInlineField("Episode title:", "```" + episode.title + "```")
                        .addInlineField("Associated show:", "```" + episode.show.name + "```")
                        .setColor(Color.YELLOW)
                );
            } else {
                // Ensure information about the media file is up to date
                var mediaFileData = fileUtilities.getMediaInfo(tvFolder + "/" + episode.show.foldername + "/Season " + episode.season + "/" + episode.filename);

                // Update movie data
                episode.codec = mediaFileData.codec;
                episode.isOptimized = mediaFileData.isOptimized();
                episode.width = mediaFileData.width;
                episode.height = mediaFileData.height;
                episode.resolution = mediaFileData.resolution();
                episode.duration = mediaFileData.duration;

                // Update the episode in the database
                entityManager.merge(episode).persist();
            }
        } catch (Exception e) {
            logger.error("Unable to get information about this media file - " + episode.filename, e);
            sendWarning(new EmbedBuilder()
                    .setTitle("Error fetching media information")
                    .setDescription("An error occurred while fetching information about the following media during the " +
                            "database consistency checker. Please make sure this media file is not corrupted.")
                    .addInlineField("Media type:", "```Episode```")
                    .addInlineField("Episode ID:", "```" + episode.id + "```")
                    .addInlineField("Episode #:", "```" + episode.number + "```")
                    .addInlineField("Season #:", "```" + episode.season + "```")
                    .addInlineField("Episode title:", "```" + episode.title + "```")
                    .addInlineField("Associated show:", "```" + episode.show.name + "```")
                    .setColor(Color.YELLOW)
            );
        }
    }

    @SuppressWarnings("DuplicatedCode")
    @Transactional
    @TransactionConfiguration(timeout = 120)
    public void verifyMovie(Movie movie) {
        try {
            // Ensure we get a updated copy of the episode from the DB in the event it was upgraded while this was processing
            movie = entityUtilities.getMovie(movie.id);

            if (Files.notExists(Path.of(movieFolder + "/" + movie.folderName + "/" + movie.filename))) {
                logger.warn("Data inconsistency found: Movie \"" + movie.title + " (" + movie.year + ") {imdb-" + movie.id +
                        "}\" could not be found on the filesystem, however it is listed in the database.");

                sendWarning(new EmbedBuilder()
                        .setTitle("Data Inconsistency Found")
                        .setDescription("A inconsistency in the database has been found. The following item is listed in the " +
                                "database but cannot be found on the filesystem.")
                        .addInlineField("Media type:", "```Movie```")
                        .addInlineField("Media ID:", "```" + movie.id + "```")
                        .addInlineField("Media name:", "```" + movie.title + "```")
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
                movie.duration = mediaFileData.duration;

                // Update the movie in the database
                entityManager.merge(movie).persist();
            }
        } catch (Exception e) {
            logger.error("Unable to get information about this media file - " + movie.filename, e);
            sendWarning(new EmbedBuilder()
                    .setTitle("Error fetching media information")
                    .setDescription("An error occurred while fetching information about the following media during the " +
                            "database consistency checker. Please make sure this media file is not corrupted.")
                    .addInlineField("Media type:", "```Movie```")
                    .addInlineField("Media ID:", "```" + movie.id + "```")
                    .addInlineField("Media name:", "```" + movie.title + "```")
                    .setColor(Color.YELLOW)
            );
        }
    }
}