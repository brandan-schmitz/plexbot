package net.celestialdata.plexbot.utilities;

import io.quarkus.arc.log.LoggerName;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.scheduler.Scheduled;
import net.celestialdata.plexbot.entities.Episode;
import net.celestialdata.plexbot.entities.Movie;
import net.celestialdata.plexbot.entities.Season;
import net.celestialdata.plexbot.entities.Show;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.jboss.logging.Logger;
import uk.co.caprica.vlcjinfo.MediaInfoFile;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
@ApplicationScoped
public class DatabaseConsistencyChecker {

    @LoggerName("net.celestialdata.plexbot.utilities.DatabaseConsistencyChecker")
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

    // Helper method to send a warning message to the bot owner
    private void sendWarning(EmbedBuilder message) {
        discordApi.getUserById(ownerId).join().sendMessage(message);
    }

    //@Scheduled(every = "168h", delay = 10, delayUnit = TimeUnit.SECONDS)
    public void verifyDatabase() {
        // Get the lists of media in the database
        List<Show> shows = Show.listAll();
        List<Season> seasons = Season.listAll();
        List<Episode> episodes = Episode.listAll();
        List<Movie> movies = Movie.listAll();

        // Create an AtomicInteger to track progress for logging reasons
        AtomicInteger progress = new AtomicInteger(1);

        // Verify that all the folders for the shows in the DB exist
        shows.forEach(show -> {
            logger.trace("Verifying show (" + progress + "/" + shows.size() + "): " + show.name + " {tvdb-" + show.id + "}");

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
        });

        // Reset the progress counter and verify that all season folders exist
        progress.set(1);
        seasons.forEach(season -> {
            logger.trace("Verifying season (" + progress + "/" + seasons.size() + "): Season " + season.number + " - " + season.show.name);

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
        });

        // Reset the progress counter and verify that all episodes exist
        progress.set(1);
        episodes.forEach(episode -> {
            logger.trace("Verifying episode (" + progress + "/" + episodes.size() + "): s" + episode.season.number +
                    "e" + episode.number + " - " + episode.show.name);
            verifyEpisode(episode);
            progress.getAndIncrement();
        });


        // Reset the progress counter and verify that all movies exist
        progress.set(1);
        movies.forEach(movie -> {
            logger.trace("Verifying movie (" + progress + "/" + movies.size() + "): " + movie.title + " (" + movie.year + ") {imdb-" + movie.id + "}");
            verifyMovie(movie);
            progress.getAndIncrement();
        });
    }

    @SuppressWarnings("DuplicatedCode")
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
            MediaInfoFile file = new MediaInfoFile(tvFolder + "/" + episode.show.foldername + "/" + episode.season.foldername + "/" + episode.filename);
            if (file.open()) {
                var codec = file.info("Video;%Encoded_Library_Name%");
                var durationString = file.info("General;%Duration/String3%");

                episode.codec = codec;
                episode.isOptimized = codec.equals("x265");
                episode.width = Integer.parseInt(file.info("Video;%Width%"));
                episode.height = Integer.parseInt(file.info("Video;%Height%"));
                try {
                    episode.duration = (Integer.parseInt(durationString.substring(0, 2)) * 60) +
                            Integer.parseInt(durationString.substring(3, 5)) +
                            ((Integer.parseInt(durationString.substring(6, 8)) >= 30) ? 1 : 0);
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

                file.close();
                entityManager.merge(episode).persist();
            } else {
                logger.warn("Unable to verify information about episode " + episode.number + " of season " + episode.season.number + " of " + episode.show.name);

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
        }
    }

    @SuppressWarnings("DuplicatedCode")
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
            MediaInfoFile file = new MediaInfoFile(movieFolder + "/" + movie.folderName + "/" + movie.filename);
            if (file.open()) {
                var codec = file.info("Video;%Encoded_Library_Name%");
                var durationString = file.info("General;%Duration/String3%");

                movie.codec = codec;
                movie.isOptimized = codec.equals("x265");
                movie.width = Integer.parseInt(file.info("Video;%Width%"));
                movie.height = Integer.parseInt(file.info("Video;%Height%"));
                try {
                    movie.duration = (Integer.parseInt(durationString.substring(0, 2)) * 60) +
                            Integer.parseInt(durationString.substring(3, 5)) +
                            ((Integer.parseInt(durationString.substring(6, 8)) >= 30) ? 1 : 0);
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

                file.close();
                entityManager.merge(movie).persist();
            } else {
                logger.warn("Unable to verify information about " + movie.title + " (" + movie.year + ") {imdb-" + movie.id + "}");

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
        }
    }
}