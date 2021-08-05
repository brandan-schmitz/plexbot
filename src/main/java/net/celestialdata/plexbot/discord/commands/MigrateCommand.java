package net.celestialdata.plexbot.discord.commands;

import io.quarkus.arc.log.LoggerName;
import net.celestialdata.plexbot.clients.models.tmdb.TmdbSourceIdType;
import net.celestialdata.plexbot.clients.services.TmdbService;
import net.celestialdata.plexbot.dataobjects.ParsedSubtitleFilename;
import net.celestialdata.plexbot.db.daos.*;
import net.celestialdata.plexbot.db.entities.*;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.enumerators.FileType;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.util.event.ListenerManager;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
@ApplicationScoped
public class MigrateCommand implements Command<Message> {
    private Message replyMessage;
    private ListenerManager<ButtonClickListener> cancelListener;
    private LocalDateTime lastUpdated = LocalDateTime.now().minus(5, ChronoUnit.SECONDS);
    private int total = 0;
    private int progress = 0;
    private boolean canceled = false;

    @LoggerName("net.celestialdata.plexbot.discord.commands.MigrateCommand")
    Logger logger;

    @ConfigProperty(name = "FolderSettings.movieFolder")
    String movieFolder;

    @ConfigProperty(name = "FolderSettings.tvFolder")
    String tvFolder;

    @Inject
    @RestClient
    TmdbService tmdbService;

    @Inject
    DiscordApi discordApi;

    @Inject
    MessageFormatter messageFormatter;

    @Inject
    FileUtilities fileUtilities;

    @Inject
    ShowDao showDao;

    @Inject
    EpisodeDao episodeDao;

    @Inject
    MovieDao movieDao;

    @Inject
    MovieSubtitleDao movieSubtitleDao;

    @Inject
    WaitlistMovieDao waitlistMovieDao;

    @Override
    public void handleFailure(Throwable error) {
        logger.error(error);
    }

    @Transactional
    public List<EpisodeOld> listAllOldEpisodes() {
        return EpisodeOld.listAll();
    }

    @Transactional
    public List<ShowOld> listOldShows() {
        return ShowOld.listAll();
    }

    @Transactional
    public List<MovieOld> listAllOldMovies() {
        return MovieOld.listAll();
    }

    @Transactional
    public List<MovieSubtitleOld> listOldMovieSubtitlesByMovie(String movieId) {
        MovieOld oldMovie = MovieOld.findById(movieId);
        return MovieSubtitleOld.list("movie", oldMovie);
    }

    @Transactional
    public List<WaitlistMovieOld> listOldWaitlistMovies() {
        return WaitlistMovieOld.listAll();
    }

    public void updateProgress() {
        if (lastUpdated.plus(3, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
            replyMessage.edit(new EmbedBuilder()
                    .setTitle("Migration Progress")
                    .setDescription("You requested that a data migration occur. Below is the progress of that migration.\n" +
                            "```Migrating " + (progress == 0 ? 1 : progress) + " of " + total + " items```")
                    .setColor(Color.BLUE)
                    .setFooter("Progress updated: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(ZonedDateTime.now()) + " CST")
            ).join();
            lastUpdated = LocalDateTime.now();
        }
    }

    public void sendErrorMessage(String failedItem, Exception exception) {
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Migration Error")
                        .setDescription("An unknown error occurred while migrating the following item:\n```" + failedItem + "```")
                        .addField("Error Message:", "```" + ExceptionUtils.getMessage(exception) + "```")
                        .setColor(Color.RED))
                .send(replyMessage.getChannel())
                .join();
    }

    public void sendErrorMessage(String failedItem, String message) {
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Migration Error")
                        .setDescription("An unknown error occurred while migrating the following item:\n```" + failedItem + "```")
                        .addField("Error Message:", "```" + message + "```")
                        .setColor(Color.RED))
                .send(replyMessage.getChannel())
                .join();
    }

    public void onCancel() {
        replyMessage.edit(new EmbedBuilder()
                .setTitle("Migration Canceled")
                .setDescription("The migration has been canceled. You can resume the migration by running the migrate command again.")
                .setColor(Color.BLUE)
        );
        cancelListener.remove();
    }

    private void resetToNewMessage(EmbedBuilder embedBuilder, long messageToReplyTo) {
        var channel = replyMessage.getChannel();
        discordApi.getMessageById(replyMessage.getId(), channel).join().delete().join();
        new MessageBuilder()
                .setEmbed(embedBuilder)
                .replyTo(messageToReplyTo)
                .send(channel)
                .join();
    }

    public void cleanDirectory(File directory) throws IOException {
        // Fetch a list of files and directories remaining
        var filesList = new ArrayList<File>();
        Files.list(directory.toPath())
                .forEach(path -> filesList.add(path.toFile()));

        // Ensure that files the directory is cleaned out
        for (File file : filesList) {
            if (file.isDirectory() && FileUtils.isEmptyDirectory(file)) {
                FileUtils.deleteDirectory(file);
            } else if (file.isHidden()) {
                FileUtils.delete(file);
            }
        }
    }

    @Override
    public void execute(Message incomingMessage, String prefix, String usedAlias, String parameterString) {
        replyMessage = new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Migration Started")
                        .setDescription("You requested that the migration process be started.")
                        .setColor(Color.BLUE))
                .addComponents(ActionRow.of(Button.danger("cancel-" + incomingMessage.getId(), "Cancel")))
                .replyTo(incomingMessage)
                .send(incomingMessage.getChannel())
                .join();

        cancelListener = replyMessage.getChannel().addButtonClickListener(clickEvent -> {
            if (clickEvent.getButtonInteraction().getCustomId().equals("cancel-" + incomingMessage.getId())) {
                clickEvent.getInteraction().asMessageComponentInteraction().orElseThrow()
                        .createOriginalMessageUpdater()
                        .removeAllComponents()
                        .removeAllEmbeds()
                        .addEmbed(new EmbedBuilder()
                                .setTitle("Stopping Migration")
                                .setDescription("The bot will finish migrating the current item then stop the migration " +
                                        "process. This will ensure that no media files are corrupted by an improper stopping " +
                                        "of the file transfer that is occurring during the migration process.")
                                .setColor(Color.YELLOW))
                        .update();
                canceled = true;
            }
        });

        // Gather media lists
        List<EpisodeOld> oldEpisodes = listAllOldEpisodes();
        List<ShowOld> oldShows = listOldShows();
        List<MovieOld> oldMovies = listAllOldMovies();
        List<WaitlistMovieOld> oldWaitlistMovies = listOldWaitlistMovies();

        // Count total number of items that need to be migrated
        progress = 0;
        total = oldEpisodes.size() + oldShows.size() + oldMovies.size() + oldWaitlistMovies.size();

        // Update the progress to show it has started
        updateProgress();

        // Process episodes
        for (EpisodeOld episode : oldEpisodes) {
            try {
                migrateOldEpisode(episode);
            } catch (Exception e) {
                logger.error("Unable to process episode " + episode.filename, e);
                sendErrorMessage("Episode " + episode.filename, e);
                progress++;
            }

            // Stop if the process was canceled
            if (canceled) {
                onCancel();
                return;
            }

            // Update the progress
            updateProgress();
        }

        // Stop if the process was canceled
        if (canceled) {
            onCancel();
            return;
        }

        // Process shows
        for (ShowOld show : oldShows) {
            try {
                migrateOldShow(show);
            } catch (Exception e) {
                logger.error("Unable to process show " + show.foldername, e);
                sendErrorMessage("Show " + show.foldername, e);
                progress++;
            }

            // Stop if the process was canceled
            if (canceled) {
                onCancel();
                return;
            }

            // Update the progress
            updateProgress();
        }

        // Stop if the process was canceled
        if (canceled) {
            onCancel();
            return;
        }

        // Process movies and movie subtitles
        for (MovieOld movie : oldMovies) {
            try {
                migrateOldMovie(movie);
            } catch (Exception e) {
                logger.error("Unable to process movie " + movie.filename, e);
                sendErrorMessage("Movie " + movie.filename, e);
                progress++;
            }

            // Stop if the process was canceled
            if (canceled) {
                onCancel();
                return;
            }

            // Update the progress
            updateProgress();
        }

        // Stop if the process was canceled
        if (canceled) {
            onCancel();
            return;
        }

        // Process waitlist movies
        for (WaitlistMovieOld movie : oldWaitlistMovies) {
            try {
                migrateOldWaitlistMovie(movie);
            } catch (Exception e) {
                logger.error("Unable to process waitlist movie " + movie.id, e);
                sendErrorMessage("Waitlist movie " + movie.id, e);
                progress++;
            }

            // Stop if the process was canceled
            if (canceled) {
                onCancel();
                return;
            }

            // Update the progress
            updateProgress();
        }

        // Update the message to remove the cancel button, listener and display a message showing the migration is finished
        resetToNewMessage(new EmbedBuilder()
                .setTitle("Migration Completed")
                .setDescription("The migration you requested has been completed. Please verify that no error messages were displayed during this process. " +
                        "Any files that caused an error will need to be manually migrated.")
                .setColor(Color.GREEN), incomingMessage.getId());
        cancelListener.remove();
    }

    public void migrateOldEpisode(EpisodeOld oldEpisode) {
        // Find episode on tmdb
        var findResponse = tmdbService.findByExternalId(oldEpisode.id, TmdbSourceIdType.TVDB.getValue());

        // Verify episode was located
        if (!findResponse.isSuccessful() || findResponse.episodes.isEmpty()) {
            logger.warn("Unable to find matching episode for " + oldEpisode.filename);
            sendErrorMessage("Episode " + oldEpisode.filename, "Unable to find matching episode.");
            progress++;
            return;
        }

        // Load the episode information
        var episodeInfo = findResponse.episodes.get(0);

        // Fetch information about the episodes show
        var showResponse = tmdbService.getShow(episodeInfo.showId);

        // Verify information was fetched
        if (!showResponse.isSuccessful()) {
            logger.warn("Unable to load matching show for " + oldEpisode.filename);
            sendErrorMessage("Episode " + oldEpisode.filename, "Unable to load matching show.");
            progress++;
            return;
        }

        // Create the new show folder if not exists
        if (!fileUtilities.createFolder(showResponse)) {
            logger.warn("Failed to create show folder for episode " + oldEpisode.filename);
            sendErrorMessage("Episode " + oldEpisode.filename, "Failed to create show folder.");
            progress++;
            return;
        }

        // Ensure the show is in the database
        var show = showDao.create(showResponse.tmdbId, showResponse.name, fileUtilities.generatePathname(showResponse));

        // Create the season folder if not exists
        if (!fileUtilities.createFolder(tvFolder + show.foldername + "/Season " + episodeInfo.seasonNum)) {
            logger.warn("Failed to create season folder: " + tvFolder + show.foldername + "/Season " + episodeInfo.seasonNum);
            sendErrorMessage("Episode " + oldEpisode.filename, "Failed to create season folder.");
            progress++;
            return;
        }

        // Move the episode into place
        var filename = fileUtilities.generateEpisodeFilename(episodeInfo, show, FileType.determineFiletype(oldEpisode.filename));
        var sourceFile = tvFolder + oldEpisode.show.foldername + "/Season " + oldEpisode.season + "/" + oldEpisode.filename;
        var destinationFile = tvFolder + show.foldername + "/Season " + episodeInfo.seasonNum + "/" + filename;
        if (!moveMedia(sourceFile, destinationFile)) {
            logger.warn("Failed to move episode " + oldEpisode.filename);
            sendErrorMessage("Episode " + oldEpisode.filename, "Failed to move episode file.");
            progress++;
            return;
        }

        // Add the episode to the database
        episodeDao.createOrUpdate(episodeInfo, Long.parseLong(oldEpisode.id), filename, show.id);

        // Delete the old episode from the database
        deleteOldEpisode(oldEpisode.id);

        // Increment the progress counter
        progress++;
    }

    void migrateOldShow(ShowOld oldShow) throws IOException {
        // Attempt to delete the show folder, this should fail if there is anything aside remaining
        var directory = new File(tvFolder + oldShow.foldername);
        cleanDirectory(directory);

        // Attempt to remove the show folder
        if (FileUtils.isEmptyDirectory(directory)) {
            FileUtils.deleteDirectory(directory);
        } else {
            logger.warn("Unable to delete show " + oldShow.foldername + " as it is not empty.");
            sendErrorMessage("Show " + oldShow.foldername, "Unable to delete non-empty folder.");
            progress++;
            return;
        }

        // Delete the database entry
        deleteOldShow(oldShow.id);

        // Increment the progress counter
        progress++;
    }

    void migrateOldMovie(MovieOld oldMovie) {
        // Find movie on tmdb
        var findResponse = tmdbService.findByExternalId(oldMovie.id, TmdbSourceIdType.IMDB.getValue());

        // Verify movie was located
        if (!findResponse.isSuccessful() || findResponse.movies.isEmpty()) {
            logger.warn("Unable to find matching movie for " + oldMovie.filename);
            sendErrorMessage("Movie " + oldMovie.filename, "Unable to find matching movie.");
            progress++;
            return;
        }

        // Load detailed the movie information
        var movieResponse = tmdbService.getMovie(findResponse.movies.get(0).tmdbId);

        // Verify movie details were fetched
        if (!movieResponse.isSuccessful()) {
            logger.warn("Unable to find matching tmdb movie for " + oldMovie.filename);
            sendErrorMessage("Movie " + oldMovie.filename, "Unable to find matching tmdb movie.");
            progress++;
            return;
        }

        // Create the movie folder if not exists
        if (!fileUtilities.createFolder(movieResponse)) {
            logger.warn("Failed to create folder for: " + movieResponse.title + " {tmdb-" + movieResponse.tmdbId + "}");
            sendErrorMessage("Movie " + oldMovie.filename, "Failed to create folder.");
            progress++;
            return;
        }

        // Create the filename and paths
        var filename = fileUtilities.generateMovieFilename(movieResponse, FileType.determineFiletype(oldMovie.filename));
        var sourceFile = movieFolder + oldMovie.folderName + "/" + oldMovie.filename;
        var destinationFile = movieFolder + fileUtilities.generatePathname(movieResponse) + "/" + filename;

        // Delete the file if it exists
        if (Files.exists(Paths.get(sourceFile))) {
            if (!moveMedia(sourceFile, destinationFile)) {
                logger.warn("Failed to move movie " + oldMovie.filename);
                sendErrorMessage("Movie " + oldMovie.filename, "Failed to move file.");
                progress++;
                return;
            }
        } else {
            // Fail if the movie is not already registered in the new table
            if (!movieDao.existsByTmdbId(movieResponse.tmdbId)) {
                logger.warn("Missing movie file: " + oldMovie.filename);
                sendErrorMessage("Movie " + oldMovie.filename, "Unable to locate file.");
                progress++;
                return;
            }
        }

        // Add the movie to the database
        var movie = movieDao.createOrUpdate(movieResponse, filename);

        // Fetch any subtitles that were associated with the movie
        var oldSubtitles = listOldMovieSubtitlesByMovie(oldMovie.id);

        // Process old subtitles if any exist
        AtomicBoolean failed = new AtomicBoolean(false);
        if (!oldSubtitles.isEmpty()) {
            oldSubtitles.forEach(subtitle -> {
                try {
                    if (!migrateOldMovieSubtitle(subtitle, oldMovie.folderName, movie)) {
                        failed.set(true);
                    }
                } catch (Exception e) {
                    failed.set(true);
                    logger.error("Unable to process movie subtitle " + subtitle.filename, e);
                    sendErrorMessage("Movie subtitle " + subtitle.filename, e);
                }
            });
        }

        // Ensure that it did not fail processing subtitle files
        if (failed.get()) {
            logger.warn("Failed to movie due to failure in subtitle processing " + oldMovie.filename);
            sendErrorMessage("Movie " + oldMovie.filename, "Failed to process subtitles for this movie.");
        } else {
            try {
                // Clean and delete the movie folder
                var directory = new File(movieFolder + oldMovie.folderName);
                cleanDirectory(directory);
                if (FileUtils.isEmptyDirectory(directory)) {
                    FileUtils.deleteDirectory(directory);
                }
            } catch (IOException e) {
                logger.warn("Unable to delete movie " + oldMovie.folderName + " folder as it is not empty.");
                sendErrorMessage("Movie " + oldMovie.folderName, "Unable to delete non-empty folder.");
                progress++;
                return;
            }

            // Delete the old movie entry from the database
            deleteOldMovie(oldMovie.id);
        }

        // Increment the progress counter
        progress++;
    }

    boolean migrateOldMovieSubtitle(MovieSubtitleOld oldSubtitle, String oldMoveFolder, Movie linkedMovie) {
        // Create a ParsedSubtitleFilename for simpler updating of the filename
        ParsedSubtitleFilename parsedSubtitleFilename = new ParsedSubtitleFilename();
        parsedSubtitleFilename.fileType = FileType.determineFiletype(oldSubtitle.filename);
        parsedSubtitleFilename.language = oldSubtitle.language;
        parsedSubtitleFilename.isForced = oldSubtitle.isForced;
        parsedSubtitleFilename.isSDH = oldSubtitle.isSDH;
        parsedSubtitleFilename.isCC = oldSubtitle.isCC;

        // Move the subtitle file
        var filename = linkedMovie.folderName + fileUtilities.subtitleSuffixBuilder(parsedSubtitleFilename);
        var sourceFile = movieFolder + oldMoveFolder + "/" + oldSubtitle.filename;
        var destinationFile = movieFolder + linkedMovie.folderName + "/" + filename;
        if (!moveMedia(sourceFile, destinationFile)) {
            logger.warn("Failed to move movie subtitle" + oldSubtitle.filename);
            sendErrorMessage("Movie Subtitle " + oldSubtitle.filename, "Failed to move file.");
            return false;
        }

        // Add the subtitle file to the database
        movieSubtitleDao.createOrUpdate(linkedMovie.id, parsedSubtitleFilename, filename);

        // Delete the old subtitle from the database
        deleteOldMovieSubtitle(oldSubtitle.id);

        return true;
    }

    void migrateOldWaitlistMovie(WaitlistMovieOld oldWaitlistMovie) {
        // Find movie on tmdb
        var findResponse = tmdbService.findByExternalId(oldWaitlistMovie.id, TmdbSourceIdType.IMDB.getValue());

        // Verify movie was located
        if (!findResponse.isSuccessful() || findResponse.movies.isEmpty()) {
            logger.warn("Unable to find matching movie for " + oldWaitlistMovie.id);
            sendErrorMessage("Waitlist movie " + oldWaitlistMovie.id, "Unable to find matching movie.");
            progress++;
            return;
        }

        // Load detailed the movie information
        var movieResponse = tmdbService.getMovie(findResponse.movies.get(0).tmdbId);

        // Verify movie details were fetched
        if (!movieResponse.isSuccessful()) {
            logger.warn("Unable to find matching tmdb movie for " + oldWaitlistMovie.id);
            sendErrorMessage("Waitlist movie " + oldWaitlistMovie.id, "Unable to find matching tmdb movie.");
            progress++;
            return;
        }

        // Create the new waitlist item
        waitlistMovieDao.create(movieResponse, oldWaitlistMovie.requestedBy);

        // Delete the old waitlist item
        deleteOldWaitlistMovie(oldWaitlistMovie.id);

        // Increment the progress counter
        progress++;
    }

    @Transactional
    public void deleteOldEpisode(String id) {
        EpisodeOld entity = EpisodeOld.findById(id);
        entity.delete();
    }

    @Transactional
    public void deleteOldShow(String id) {
        ShowOld entity = ShowOld.findById(id);
        entity.delete();
    }

    @Transactional
    public void deleteOldMovie(String id) {
        MovieOld entity = MovieOld.findById(id);
        entity.delete();
    }

    @Transactional
    public void deleteOldMovieSubtitle(int id) {
        MovieSubtitleOld entity = MovieSubtitleOld.findById(id);
        entity.delete();
    }

    @Transactional
    public void deleteOldWaitlistMovie(String id) {
        WaitlistMovieOld entity = WaitlistMovieOld.findById(id);
        entity.delete();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean moveMedia(String source, String destination) {
        boolean success = true;

        try {
            // Copy the file to the destination
            Files.copy(
                    Paths.get(source),
                    Paths.get(destination),
                    StandardCopyOption.REPLACE_EXISTING
            );

            // Delete the source file if the copy was successful
            Files.delete(Paths.get(source));
        } catch (Exception e) {
            success = false;
        }

        return success;
    }
}