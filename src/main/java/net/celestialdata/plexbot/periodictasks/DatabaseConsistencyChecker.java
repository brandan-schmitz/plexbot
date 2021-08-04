package net.celestialdata.plexbot.periodictasks;

import io.quarkus.arc.log.LoggerName;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import net.celestialdata.plexbot.dataobjects.MediaInfoData;
import net.celestialdata.plexbot.db.daos.*;
import net.celestialdata.plexbot.db.entities.*;
import net.celestialdata.plexbot.utilities.BotProcess;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static net.celestialdata.plexbot.enumerators.FileType.mediaFileExtensions;
import static net.celestialdata.plexbot.enumerators.FileType.subtitleFileExtensions;

@SuppressWarnings("unused")
@ApplicationScoped
public class DatabaseConsistencyChecker extends BotProcess {
    DecimalFormat decimalFormatter = new DecimalFormat("#0.00");
    private List<Movie> moviesInDatabase = new ArrayList<>();
    private List<Episode> episodesInDatabase = new ArrayList<>();
    private List<Show> showsInDatabase = new ArrayList<>();
    private List<MovieSubtitle> movieSubtitlesInDatabase = new ArrayList<>();
    private List<EpisodeSubtitle> episodeSubtitlesInDatabase = new ArrayList<>();
    private Collection<File> mediaFiles;
    private Collection<File> subtitleFiles;

    @LoggerName("net.celestialdata.plexbot.periodictasks.DatabaseConsistencyChecker")
    Logger logger;

    @ConfigProperty(name = "ChannelSettings.corruptedNotificationChannel")
    String corruptedNotificationChannel;

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
    CorruptedMediaItemDao corruptedMediaItemDao;

    @Inject
    EncodingQueueItemDao encodingQueueItemDao;

    @Inject
    EpisodeDao episodeDao;

    @Inject
    EpisodeSubtitleDao episodeSubtitleDao;

    @Inject
    MovieDao movieDao;

    @Inject
    MovieSubtitleDao movieSubtitleDao;

    @Inject
    ShowDao showDao;

    // Helper method to send a warning message to the bot owner
    private void sendWarning(EmbedBuilder message) {
        discordApi.getUserById(ownerId).join().sendMessage(message);
    }

    // Helper method to update the progress of the checker
    private void updateProgress(int progress) {
        var total = decimalFormatter.format(((double) progress / (showsInDatabase.size() +
                episodesInDatabase.size() + moviesInDatabase.size() + mediaFiles.size() + subtitleFiles.size() +
                movieSubtitlesInDatabase.size() + episodeSubtitlesInDatabase.size())) * 100);
        updateProcessString("Database Consistency Checker - " + total + "%");
    }

    // Create a listener for button clicks on corrupted media notification messages
    public void init(@Observes StartupEvent event) {
        TextChannel upgradeChannel = discordApi.getTextChannelById(corruptedNotificationChannel).orElseThrow();
        upgradeChannel.addButtonClickListener(clickEvent -> {
            if (clickEvent.getButtonInteraction().getCustomId().equals("recheck-corrupted-file")) {
                // Get the ID of the message triggering this event
                var messageId = clickEvent.getButtonInteraction().getMessageId();

                // Fetch the corrupted media item from the database
                CorruptedMediaItem corruptedMediaItem = corruptedMediaItemDao.getByMessageId(messageId);

                // Attempt to load the file at the path given
                File file = new File(corruptedMediaItem.absolutePath);

                // Delete the existing entry for the corrupted media file. If it is still corrupted, a new
                // message will be sent to the channel.
                corruptedMediaItemDao.deleteByMessageId(messageId);

                // Verify the media file
                verifyMediaFile(file);
            }
        });
    }

    @Scheduled(every = "168h", delay = 10, delayUnit = TimeUnit.SECONDS)
    public void verifyDatabase() {
        // Configure the process
        configureProcess("Database Consistency Checker - na%");

        // Get the lists of media in the database
        showsInDatabase = showDao.listALl();
        episodesInDatabase = episodeDao.listALl();
        episodeSubtitlesInDatabase = episodeSubtitleDao.listALl();
        moviesInDatabase = movieDao.listALl();
        movieSubtitlesInDatabase = movieSubtitleDao.listALl();

        // Fetch a list of all media files in the filesystem
        mediaFiles = FileUtils.listFiles(new File(movieFolder), mediaFileExtensions, true);
        mediaFiles.addAll(FileUtils.listFiles(new File(tvFolder), mediaFileExtensions, true));

        // Fetch a list of all subtitle files in the filesystem
        subtitleFiles = FileUtils.listFiles(new File(movieFolder), subtitleFileExtensions, true);
        subtitleFiles.addAll(FileUtils.listFiles(new File(tvFolder), subtitleFileExtensions, true));

        // Ensure folders and hidden files are somehow not included in the files list
        mediaFiles.removeIf(File::isDirectory);
        mediaFiles.removeIf(File::isHidden);
        subtitleFiles.removeIf(File::isDirectory);
        subtitleFiles.removeIf(File::isHidden);

        // Create items to track progress for logging reasons
        AtomicInteger overallProgress = new AtomicInteger(1);

        // Verify that all the folders for the shows in the DB exist
        showsInDatabase.forEach(show -> {
            // Log a tracer message
            logger.info("Verifying show: " + show.name + " {tvdb-" + show.id + "}");

            // Verify the folder exists, if not send warnings
            if (!Files.isDirectory(Path.of(tvFolder + "/" + show.foldername))) {
                // Log that the folder does not exist
                logger.warn("Data inconsistency found: Show \"" + show.name + " {tvdb-" + show.id +
                        "}\" could not be found on the filesystem, however it is listed in the database.");

                // Send a warning in discord about the folder not existing.
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

            // Update the progress
            overallProgress.getAndIncrement();
            updateProgress(overallProgress.get());
        });

        // Verify that the media files exist and are updated
        mediaFiles.forEach(file -> {
            // Log a tracer message
            logger.info("Verifying media file: " + file.getAbsolutePath());

            // Verify the file
            verifyMediaFile(file);

            // Update the progress
            overallProgress.getAndIncrement();
            updateProgress(overallProgress.get());
        });

        // Verify that the subtitle files exist
        subtitleFiles.forEach(file -> {
            // Log a tracer message
            logger.info("Verifying subtitle file: " + file.getAbsolutePath());

            // Verify the file
            verifySubtitleFile(file);

            // Update the progress
            overallProgress.getAndIncrement();
            updateProgress(overallProgress.get());
        });

        // Report any movies remaining in the list of movies in the database since they were not located on the filesystem
        moviesInDatabase.forEach(movie -> {
            // Log the warning about a missing movie
            logger.warn("Data inconsistency found: Movie \"" + movie.title + " (" + movie.year + ") {imdb-" + movie.id +
                    "}\" could not be found on the filesystem, however it is listed in the database.");

            // Send the warning over discord
            sendWarning(new EmbedBuilder()
                    .setTitle("Data Inconsistency Found")
                    .setDescription("A inconsistency in the database has been found. The following item is listed in the " +
                            "database but cannot be found on the filesystem.")
                    .addInlineField("Media type:", "```Movie```")
                    .addInlineField("Media ID:", "```" + movie.id + "```")
                    .addInlineField("Media name:", "```" + movie.title + "```")
                    .setColor(Color.YELLOW)
            );

            // Update the progress
            overallProgress.getAndIncrement();
            updateProgress(overallProgress.get());
        });

        // Report any movie subtitles remaining in the list of movie subtitles in the database since they were not located on the filesystem
        movieSubtitlesInDatabase.forEach(subtitle -> {
            // Log the warning about a missing movie
            logger.warn("Data inconsistency found: Movie subtitle " + subtitle.filename + " for \"" + subtitle.movie.title +
                    " (" + subtitle.movie.year + ") {imdb-" + subtitle.movie.id + "}\" could not be found on the filesystem, " +
                    "however it is listed in the database.");

            // Send the warning over discord
            sendWarning(new EmbedBuilder()
                    .setTitle("Data Inconsistency Found")
                    .setDescription("A inconsistency in the database has been found. The following item is listed in the " +
                            "database but cannot be found on the filesystem.")
                    .addInlineField("Media type:", "```Movie Subtitle```")
                    .addInlineField("Subtitle: ", "```" + subtitle.filename + "```")
                    .addInlineField("Associated Movie ID:", "```" + subtitle.movie.id + "```")
                    .addInlineField("Associated Movie name:", "```" + subtitle.movie.title + "```")
                    .setColor(Color.YELLOW)
            );

            // Update the progress
            overallProgress.getAndIncrement();
            updateProgress(overallProgress.get());
        });

        // Report any episodes remaining in the list of movies in the database since they were not located on the filesystem
        episodesInDatabase.forEach(episode -> {
            // Log the warning about a missing movie
            logger.warn("Data inconsistency found: Episode " + episode.number + " of season " + episode.season + " of " + episode.show.name +
                    " could not be found on the filesystem, however it is listed in the database.");

            // Send the warning over discord
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

            // Update the progress
            overallProgress.getAndIncrement();
            updateProgress(overallProgress.get());
        });

        // Report any episode subtitles remaining in the list of episode subtitles in the database since they were not located on the filesystem
        episodeSubtitlesInDatabase.forEach(subtitle -> {
            // Log the warning about a missing movie
            logger.warn("Data inconsistency found: Episode subtitle " + subtitle.filename + " for season " + subtitle.episode.season + ", episode " +
                    subtitle.episode.number + " of " + subtitle.episode.show.name + " {tvdb-" + subtitle.episode.show.id + "} could not be found on the " +
                    "filesystem, however it is located in the database");

            // Send the warning over discord
            sendWarning(new EmbedBuilder()
                    .setTitle("Data Inconsistency Found")
                    .setDescription("A inconsistency in the database has been found. The following item is listed in the " +
                            "database but cannot be found on the filesystem.")
                    .addInlineField("Media type:", "```Episode Subtitle```")
                    .addInlineField("Subtitle: ", "```" + subtitle.filename + "```")
                    .addInlineField("Associated Episode #:", "```" + subtitle.episode.number + "```")
                    .addInlineField("Associated Season #:", "```" + subtitle.episode.season + "```")
                    .addInlineField("Associated show:", "```" + subtitle.episode.show.name + "```")
                    .setColor(Color.YELLOW)
            );

            // Update the progress
            overallProgress.getAndIncrement();
            updateProgress(overallProgress.get());
        });

        endProcess();
    }

    @Transactional
    @TransactionConfiguration(timeout = 120)
    public void verifyMediaFile(File file) {
        try {
            // Determine if the file is a movie or episode
            String mediaType;
            if (file.getAbsolutePath().contains(movieFolder)) {
                mediaType = "movie";
            } else {
                mediaType = "episode";
            }

            // Load media info about this file
            MediaInfoData mediaInfo;
            try {
                mediaInfo = fileUtilities.getMediaInfo(file.getAbsolutePath());
            } catch (StringIndexOutOfBoundsException e) {
                // If the file is corrupted, send the proper warnings
                logger.warn("Corrupted File Detected: " + file.getAbsolutePath());
                corruptedMediaItemDao.create(mediaType, file);

                // Ensure that the file was not in the list of database entries
                moviesInDatabase.removeIf(item -> item.filename.equals(file.getName()));

                // Continue to the next file
                return;
            }

            // Attempt to find the corresponding item in the database
            var exists = true;
            if (mediaType.equals("movie")) {
                Movie movie;
                try {
                    // Fetch the movie from the database
                    movie = movieDao.getByFilename(file.getName());
                } catch (NoResultException e) {
                    // If the file does not exist in the database, send an alert and continue to the next file
                    logger.warn("Data inconsistency found: Movie " + file.getAbsolutePath() + " is located on the filesystem but not in the database.");
                    sendWarning(new EmbedBuilder()
                            .setTitle("Data Inconsistency Found")
                            .setDescription("A inconsistency in the database has been found. The following file exists in the filesystem, " +
                                    "however it does not exist in the database. You should use the importer to re-import this file to add it to the database.")
                            .addInlineField("Media Type:", "```Movie```")
                            .addField("Media Filename:", "```" + file.getName() + "```")
                            .setColor(Color.YELLOW)
                    );

                    // Ensure that the file was not in the list of database entries
                    moviesInDatabase.removeIf(item -> item.filename.equals(file.getName()));

                    // Continue to the next file
                    return;
                }

                // Update movie information in the database
                movie.codec = mediaInfo.codec;
                movie.isOptimized = mediaInfo.isOptimized();
                movie.width = mediaInfo.width;
                movie.height = mediaInfo.height;
                movie.resolution = mediaInfo.resolution();
                movie.duration = mediaInfo.duration;

                // If the episode is not optimized, ensure it is in the queue
                if (!movie.isOptimized) {
                    encodingQueueItemDao.create("movie", movie.id);
                }

                // Remove the movie from the list of movies in the database
                moviesInDatabase.removeIf(item -> item.filename.equals(file.getName()));
            } else {
                Episode episode;

                try {
                    // Fetch the episode from the database
                    episode = episodeDao.getByFilename(file.getName());
                } catch (NoResultException e) {
                    // If the file does not exist in the database, send an alert and continue to the next file
                    logger.warn("Data inconsistency found: Episode " + file.getAbsolutePath() + " is located on the filesystem but not in the database.");
                    sendWarning(new EmbedBuilder()
                            .setTitle("Data Inconsistency Found")
                            .setDescription("A inconsistency in the database has been found. The following file exists in the filesystem, " +
                                    "however it does not exist in the database. You should use the importer to re-import this file to add it to the database.")
                            .addInlineField("Media Type:", "```Episode```")
                            .addField("Media Filename:", "```" + file.getName() + "```")
                            .setColor(Color.YELLOW)
                    );

                    // Ensure that the file was not in the list of database entries
                    episodesInDatabase.removeIf(item -> item.filename.equals(file.getName()));

                    // Continue to the next file
                    return;
                }

                // Update episode data
                episode.codec = mediaInfo.codec;
                episode.isOptimized = mediaInfo.isOptimized();
                episode.width = mediaInfo.width;
                episode.height = mediaInfo.height;
                episode.resolution = mediaInfo.resolution();
                episode.duration = mediaInfo.duration;

                // If the episode is not optimized, ensure it is in the queue
                if (!episode.isOptimized) {
                    encodingQueueItemDao.create("episode", episode.tmdbId);
                }

                // Remove the episode from the list of episodes in the database
                episodesInDatabase.removeIf(item -> item.filename.equals(file.getName()));
            }
        } catch (Exception e) {
            // Remove the entry for this file from the database lists if it exists
            moviesInDatabase.removeIf(movie -> movie.filename.equals(file.getName()));
            episodesInDatabase.removeIf(episode -> episode.filename.equals(file.getName()));

            // Log the error and send a warning in Discord
            logger.error("Unable to get information about this media file: " + file.getAbsolutePath(), e);
            sendWarning(new EmbedBuilder()
                    .setTitle("Error fetching media information")
                    .setDescription("An error occurred while fetching information about the following media during the " +
                            "database consistency checker. Please make sure this media file is not corrupted.\n```" + file.getAbsolutePath() + "```")
                    .setFooter("Error message: " + e.getMessage())
                    .setColor(Color.RED)
            );
        }
    }

    @Transactional
    @TransactionConfiguration(timeout = 120)
    public void verifySubtitleFile(File file) {
        try {
            // Determine if the file is a movie or episode
            String mediaType;
            if (file.getAbsolutePath().contains(movieFolder)) {
                mediaType = "movie";
            } else {
                mediaType = "episode";
            }

            // Attempt to find the corresponding item in the database
            var exists = true;
            if (mediaType.equals("movie")) {
                try {
                    // Fetch the movie subtitle from the database
                    MovieSubtitle subtitle = movieSubtitleDao.getByFilename(file.getName());
                } catch (NoResultException e) {
                    // If the file does not exist in the database, send an alert and continue to the next file
                    logger.warn("Data inconsistency found: Movie subtitle " + file.getAbsolutePath() + " is located on the filesystem but not in the database.");
                    sendWarning(new EmbedBuilder()
                            .setTitle("Data Inconsistency Found")
                            .setDescription("A inconsistency in the database has been found. The following file exists in the filesystem, " +
                                    "however it does not exist in the database. You should use the importer to re-import this file to add it to the database.")
                            .addInlineField("Media Type:", "```Movie Subtitle```")
                            .addField("Media Filename:", "```" + file.getName() + "```")
                            .setColor(Color.YELLOW)
                    );
                }

                // Ensure that the file was not in the list of database entries
                movieSubtitlesInDatabase.removeIf(item -> item.filename.equals(file.getName()));
            } else {
                try {
                    // Fetch the episode subtitle from the database
                    EpisodeSubtitle subtitle = episodeSubtitleDao.getByFilename(file.getName());
                } catch (NoResultException e) {
                    // If the file does not exist in the database, send an alert and continue to the next file
                    logger.warn("Data inconsistency found: Episode subtitle " + file.getAbsolutePath() + " is located on the filesystem but not in the database.");
                    sendWarning(new EmbedBuilder()
                            .setTitle("Data Inconsistency Found")
                            .setDescription("A inconsistency in the database has been found. The following file exists in the filesystem, " +
                                    "however it does not exist in the database. You should use the importer to re-import this file to add it to the database.")
                            .addInlineField("Media Type:", "```Episode Subtitle```")
                            .addField("Media Filename:", "```" + file.getName() + "```")
                            .setColor(Color.YELLOW)
                    );
                }

                // Ensure that the file was not in the list of database entries
                episodeSubtitlesInDatabase.removeIf(item -> item.filename.equals(file.getName()));
            }
        } catch (Exception e) {
            // Remove the entry for this file from the database lists if it exists
            movieSubtitlesInDatabase.removeIf(subtitle -> subtitle.filename.equals(file.getName()));
            episodeSubtitlesInDatabase.removeIf(subtitle -> subtitle.filename.equals(file.getName()));

            // Log the error and send a warning in Discord
            logger.error("Unable to get information about this media subtitle file: " + file.getAbsolutePath(), e);
            sendWarning(new EmbedBuilder()
                    .setTitle("Error fetching media information")
                    .setDescription("An error occurred while fetching information about the following media during the " +
                            "database consistency checker. Please make sure this media file is not corrupted.\n```" + file.getAbsolutePath() + "```")
                    .setFooter("Error message: " + e.getMessage())
                    .setColor(Color.RED)
            );
        }
    }
}