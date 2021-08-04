package net.celestialdata.plexbot.processors;

import io.quarkus.arc.log.LoggerName;
import io.smallrye.mutiny.Multi;
import net.celestialdata.plexbot.clients.models.tmdb.TmdbMovie;
import net.celestialdata.plexbot.clients.models.tmdb.TmdbSourceIdType;
import net.celestialdata.plexbot.clients.services.SyncthingService;
import net.celestialdata.plexbot.clients.services.TmdbService;
import net.celestialdata.plexbot.dataobjects.ParsedMediaFilename;
import net.celestialdata.plexbot.dataobjects.ParsedSubtitleFilename;
import net.celestialdata.plexbot.db.daos.EpisodeDao;
import net.celestialdata.plexbot.db.daos.EpisodeSubtitleDao;
import net.celestialdata.plexbot.db.daos.MovieDao;
import net.celestialdata.plexbot.db.daos.MovieSubtitleDao;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.enumerators.FileType;
import net.celestialdata.plexbot.utilities.BotProcess;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.api.util.logging.ExceptionLogger;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static net.celestialdata.plexbot.enumerators.FileType.mediaFileExtensions;
import static net.celestialdata.plexbot.enumerators.FileType.subtitleFileExtensions;

@ApplicationScoped
public class ImportMediaProcessor extends BotProcess {
    private final DecimalFormat decimalFormatter = new DecimalFormat("#0.00");
    private LocalDateTime lastUpdate = LocalDateTime.now();
    private int totalNumFiles = 0;
    private int currentPos = 1;
    private boolean overwrite = false;
    private Message replyMessage;
    private ListenerManager<ButtonClickListener> cancelListener;
    private boolean stopProcess = false;

    @LoggerName("net.celestialdata.plexbot.processors.ImportMediaProcessor")
    Logger logger;

    @ConfigProperty(name = "ChannelSettings.newMovieNotificationChannel")
    String newMovieNotificationChannel;

    @ConfigProperty(name = "ChannelSettings.newEpisodeNotificationChannel")
    String newEpisodeNotificationChannel;

    @ConfigProperty(name = "FolderSettings.importFolder")
    String importFolder;

    @ConfigProperty(name = "FolderSettings.movieFolder")
    String movieFolder;

    @ConfigProperty(name = "FolderSettings.tvFolder")
    String tvFolder;

    @ConfigProperty(name = "SyncthingSettings.enabled")
    boolean syncthingEnabled;

    @ConfigProperty(name = "SyncthingSettings.importFolderId")
    String syncthingImportFolderId;

    @ConfigProperty(name = "SyncthingSettings.devices")
    List<String> syncthingDevices;

    @Inject
    FileUtilities fileUtilities;

    @Inject
    MessageFormatter messageFormatter;

    @Inject
    DiscordApi discordApi;

    @Inject
    @RestClient
    SyncthingService syncthingService;

    @Inject
    @RestClient
    TmdbService tmdbService;

    @Inject
    MovieDao movieDao;

    @Inject
    MovieSubtitleDao movieSubtitleDao;

    @Inject
    EpisodeDao episodeDao;

    @Inject
    EpisodeSubtitleDao episodeSubtitleDao;

    @Override
    public void configureProcess(String processString, Message replyMessage) {
        // Configure the UncaughtExceptionHandler
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            logger.error(e);
            this.cancelListener.remove();
            replyMessage.edit(messageFormatter.errorMessage("An unknown error occurred. Brandan has been notified of the error.", e.getMessage()));
            endProcess(e);
        });

        // Configure the process string and submit the process to the BotStatusDisplay
        this.processString = processString;
        this.processId = botStatusDisplay.submitProcess(processString);
    }

    private void onCancel() {
        this.replyMessage.edit(new EmbedBuilder()
                .setTitle("Import Canceled")
                .setDescription("The import has been canceled. You can resume this import by running the import command again.")
                .setColor(Color.BLACK)
        );
        endProcess();
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

    public void processImport(Message replyMessage, Long commandMessageId, boolean skipSync, boolean overwrite, boolean optimized, boolean includeOptimized) {
        // TODO: Ensure there are no previous instance of the import processor running to avoid interference

        // Configure the button click listener used to stop the import process
        this.cancelListener = replyMessage.getChannel().addButtonClickListener(clickEvent -> {
            if (clickEvent.getButtonInteraction().getCustomId().equals("cancel-" + commandMessageId)) {
                clickEvent.getInteraction().asMessageComponentInteraction().orElseThrow()
                        .createOriginalMessageUpdater()
                        .removeAllComponents()
                        .removeAllEmbeds()
                        .addEmbed(new EmbedBuilder()
                                .setTitle("Stopping Import")
                                .setDescription("The bot will finish importing the current media item " +
                                        "then stop the import process. This will ensure no media files are " +
                                        "corrupted by an improper stopping of the file transfer that occurs " +
                                        "during the import process.")
                                .setColor(Color.YELLOW)
                        )
                        .update();
                this.stopProcess = true;
            }
        });

        // Configure this process
        configureProcess("Import Processor - initializing", replyMessage);
        this.overwrite = overwrite;
        this.replyMessage = replyMessage;
        this.stopProcess = false;
        this.currentPos = 1;

        try {
            // Verify that SyncThing is not currently syncing the import folder
            // Using the --skipSync flag in the command should override this and allow the import to proceed anyways
            AtomicBoolean syncthingSucceeded = new AtomicBoolean(true);
            if (syncthingEnabled && !skipSync) {
                awaitSyncthing().subscribe().with(
                        progress -> {
                            if (progress != 100) {
                                updateProcessString("Import Processor - Awaiting Sync: " + decimalFormatter.format(progress) + "%");
                                replyMessage.edit(messageFormatter.importProgressMessage("Awaiting Syncthing Sync: " + decimalFormatter.format(progress) + "%"))
                                        .exceptionally(ExceptionLogger.get());
                            }
                        },
                        failure -> {
                            replyMessage.edit(messageFormatter.errorMessage("Failed while waiting for Syncthing.", failure.getMessage()))
                                    .exceptionally(ExceptionLogger.get());
                            logger.error(failure);
                            syncthingSucceeded.set(false);
                            this.cancelListener.remove();
                            reportError(failure);
                            endProcess();
                        },
                        () -> {}
                );
            }

            // Fail the process if there was an issue with waiting for Syncthing
            if (!syncthingSucceeded.get()) {
                resetToNewMessage(new EmbedBuilder()
                        .setTitle("Import Failed")
                        .setDescription("An error occurred while waiting for Syncthing to complete it's sync. Please try again later.")
                        .setColor(Color.RED), commandMessageId);
                this.cancelListener.remove();
                endProcess();
                return;
            }

            // Create the objects to hold the collections of media media files
            Collection<File> episodeMediaFiles;
            Collection<File> movieMediaFiles;
            Collection<File> episodeSubtitleFiles;
            Collection<File> movieSubtitleFiles;

            // Collect a list of media files to process
            // If the optimized flag is given, only collect media files within the optimized directory
            // If the include-optimized flag is given include media in the regular import folders as well as the optimized directory
            if (optimized) {
                episodeMediaFiles = FileUtils.listFiles(new File(importFolder + "optimized/episodes"), mediaFileExtensions, false);
                movieMediaFiles = FileUtils.listFiles(new File(importFolder + "optimized/movies"), mediaFileExtensions, false);
                episodeSubtitleFiles = FileUtils.listFiles(new File(importFolder + "optimized/episodes"), subtitleFileExtensions, false);
                movieSubtitleFiles = FileUtils.listFiles(new File(importFolder + "optimized/movies"), subtitleFileExtensions, false);
            } else {
                // Fetch the regular import files
                episodeMediaFiles = FileUtils.listFiles(new File(importFolder + "episodes"), mediaFileExtensions, false);
                movieMediaFiles = FileUtils.listFiles(new File(importFolder + "movies"), mediaFileExtensions, false);
                episodeSubtitleFiles = FileUtils.listFiles(new File(importFolder + "episodes"), subtitleFileExtensions, false);
                movieSubtitleFiles = FileUtils.listFiles(new File(importFolder + "movies"), subtitleFileExtensions, false);

                // Add the optimized media files if flag is used
                if (includeOptimized) {
                    episodeMediaFiles.addAll(FileUtils.listFiles(new File(importFolder + "optimized/episodes"), mediaFileExtensions, false));
                    movieMediaFiles.addAll(FileUtils.listFiles(new File(importFolder + "optimized/movies"), mediaFileExtensions, false));
                    episodeSubtitleFiles.addAll(FileUtils.listFiles(new File(importFolder + "optimized/episodes"), subtitleFileExtensions, false));
                    movieSubtitleFiles.addAll(FileUtils.listFiles(new File(importFolder + "optimized/movies"), subtitleFileExtensions, false));
                }
            }

            // Ensure there are no directories listed as files
            episodeMediaFiles.removeIf(File::isDirectory);
            movieMediaFiles.removeIf(File::isDirectory);
            episodeSubtitleFiles.removeIf(File::isDirectory);
            movieSubtitleFiles.removeIf(File::isDirectory);

            // Remove any hidden files from the lists
            episodeMediaFiles.removeIf(File::isHidden);
            movieMediaFiles.removeIf(File::isHidden);
            episodeSubtitleFiles.removeIf(File::isHidden);
            movieSubtitleFiles.removeIf(File::isHidden);

            // Calculate the total number of files to process and ensure the current position is at 0
            totalNumFiles = episodeMediaFiles.size() + movieMediaFiles.size() + episodeSubtitleFiles.size() + movieSubtitleFiles.size();

            // Ensure there are actually files to import, otherwise exit
            if (totalNumFiles == 0) {
                resetToNewMessage(new EmbedBuilder()
                        .setDescription("There were no files available to import. Please make sure they are in the proper folders before using the import command again.")
                        .setColor(Color.YELLOW), commandMessageId);
                this.cancelListener.remove();
                endProcess();
                return;
            }

            // Update the progress message to show it has started processing media
            updateStatus(0);

            // Process all episode media files
            processEpisodeItems(episodeMediaFiles, false).subscribe().with(
                    this::updateStatus,
                    failure -> {
                        if (!failure.getMessage().equals("Process canceled by user")) {
                            reportError(failure);
                        }
                    },
                    () -> currentPos = currentPos + episodeMediaFiles.size()
            );

            // Process all episode subtitle files
            if (!stopProcess) {
                processEpisodeItems(episodeSubtitleFiles, true).subscribe().with(
                        this::updateStatus,
                        failure -> {
                            if (!failure.getMessage().equals("Process canceled by user")) {
                                reportError(failure);
                            }
                        },
                        () -> currentPos = currentPos + episodeSubtitleFiles.size()
                );
            } else {
                onCancel();
                return;
            }

            // Process all movie media files
            if (!stopProcess) {
                processMovieItems(movieMediaFiles, false).subscribe().with(
                        this::updateStatus,
                        failure -> {
                            if (!failure.getMessage().equals("Process canceled by user")) {
                                reportError(failure);
                            }
                        },
                        () -> currentPos = currentPos + movieMediaFiles.size()
                );
            } else {
                onCancel();
                return;
            }

            // Process movie subtitle files
            if (!stopProcess) {
                processMovieItems(movieSubtitleFiles, true).subscribe().with(
                        this::updateStatus,
                        failure -> {
                            if (!failure.getMessage().equals("Process canceled by user")) {
                                reportError(failure);
                            }
                        },
                        () -> currentPos = currentPos + movieSubtitleFiles.size()
                );
            } else {
                onCancel();
                return;
            }
        } catch (Exception e) {
            this.cancelListener.remove();
            reportError(e);
            endProcess();
        }

        if (stopProcess) {
            onCancel();
            return;
        } else {
            this.cancelListener.remove();
            resetToNewMessage(new EmbedBuilder()
                    .setTitle("Import Processor")
                    .setDescription("You have requested the bot import media contained within the import folder. This action has been completed.")
                    .setColor(Color.GREEN)
                    .setFooter("Finished: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                            .format(ZonedDateTime.now()) + " CST"), commandMessageId);
        }

        endProcess();
    }

    private void updateStatus(int progress) {
        var percentage = (((double) ((currentPos - 1) + progress) / totalNumFiles) * 100);

        // Update the bot status process string
        updateProcessString("Import Processor - " + decimalFormatter.format(percentage) + "%");

        // Update the progress message
        if (lastUpdate.plus(3, ChronoUnit.SECONDS).isBefore(LocalDateTime.now()) || progress == 0) {
            this.replyMessage.edit(messageFormatter.importProgressMessage("Processing file " + (currentPos + progress) + " of " + totalNumFiles));
            lastUpdate = LocalDateTime.now();
        }
    }

    private Multi<Double> awaitSyncthing() {
        return Multi.createFrom().emitter(multiEmitter -> {
            try {
                var syncComplete = true;
                var lastEmitted = LocalDateTime.now();

                for (String deviceId : syncthingDevices) {
                    var response = syncthingService.getCompletionStatus(syncthingImportFolderId, deviceId);
                    syncComplete = response.completion == 100;
                }

                while (!syncComplete) {
                    // Reset the progress
                    var progress = 0.00;

                    // Wait 2 seconds between requests to avoid overloading syncthing
                    if (lastEmitted.plus(2, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        // Cycle through the devices and check their status and calculate the overall sync status
                        for (String deviceId : syncthingDevices) {
                            var response = syncthingService.getCompletionStatus(syncthingImportFolderId, deviceId);

                            if (response.completion != 100) {
                                syncComplete = false;
                                progress = progress + (response.completion / syncthingDevices.size());
                            } else syncComplete = true;
                        }

                        // Emit the progress
                        multiEmitter.emit(progress);
                        lastEmitted = LocalDateTime.now();
                    }
                }

                multiEmitter.complete();
            } catch (Exception e) {
                logger.error(e);
                multiEmitter.fail(e);
            }
        });
    }

    private String generateSeasonString(int seasonNumber) {
        if (seasonNumber <= 9) {
            return "s0" + seasonNumber;
        } else return "s" + seasonNumber;
    }

    private String generateEpisodeString(int episodeNumber) {
        if (episodeNumber <= 9) {
            return "e0" + episodeNumber;
        } else return "e" + episodeNumber;
    }

    @SuppressWarnings("DuplicatedCode")
    public Multi<Integer> processEpisodeItems(Collection<File> files, boolean filesAreSubtitles) {
        AtomicInteger progress = new AtomicInteger(0);

        return Multi.createFrom().emitter(multiEmitter -> {
            for (File file : files) {
                try {
                    // Cancel if the process was canceled by the user
                    if (this.stopProcess) {
                        multiEmitter.fail(new InterruptedException("Process canceled by user"));
                        return;
                    }

                    // Parse the filename into its core components
                    Object parsedFilename;
                    try {
                        parsedFilename = filesAreSubtitles ?
                                new ParsedSubtitleFilename().parseFilename(file.getName()) : new ParsedMediaFilename().parseFilename(file.getName());
                    } catch (IllegalArgumentException e) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage(e.getMessage(), file.toString()))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        progress.getAndIncrement();
                        multiEmitter.emit(progress.get());
                        continue;
                    }

                    // Verify the ID is a tvdb ID and not a IMDb ID
                    var parsedId = filesAreSubtitles ? ((ParsedSubtitleFilename) parsedFilename).id : ((ParsedMediaFilename) parsedFilename).id;
                    if (parsedId.matches("[^0-9].*")) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage("The following file in the episodes import folder is not using a valid TVDB id. " +
                                        "Please make sure that only files using valid TVDB ids are in the episodes import folder.\n\n" + file.getName()))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        progress.getAndIncrement();
                        multiEmitter.emit(progress.get());
                        continue;
                    }

                    // Verify the episode exists if this is a subtitle file
                    if (filesAreSubtitles && !episodeDao.existsByTvdbId(Long.parseLong(parsedId))) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage("You attempted to import the following subtitle file, however the corresponding episode " +
                                        "does not exist in the system. Please add the episode before adding the subtitle.\n" + file.getName()))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        progress.getAndIncrement();
                        multiEmitter.emit(progress.get());
                        continue;
                    }

                    // Use the find endpoint on tmdb to get a list of matching TMDB episodes to a TVDB ID
                    var findResponse = tmdbService.findByExternalId(parsedId, TmdbSourceIdType.TVDB);

                    // Check to see if the find was not successful
                    if (!findResponse.isSuccessful() || findResponse.episodes.isEmpty()) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage("Unable to match the following file to a TMDB episode: " + file.getName()))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        progress.getAndIncrement();
                        multiEmitter.emit(progress.get());
                        continue;
                    }

                    // Get the first result in the list as that should be the matching episode
                    var episodeResponse = findResponse.episodes.get(0);

                    // Fetch information about this episodes show in TMDB
                    var showResponse = tmdbService.getShow(episodeResponse.showId);

                    // Ensure that this request was also successful
                    if (!showResponse.isSuccessful()) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage("Unable to match the following file to a TVDB series: " + file.getName()))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        progress.getAndIncrement();
                        multiEmitter.emit(progress.get());
                        continue;
                    }

                    // Create the strings used in the file/folder names for the episode and season numbers
                    var episodeString = generateEpisodeString(episodeResponse.number);
                    var seasonString = generateSeasonString(episodeResponse.seasonNum);

                    // Generate the season and show folder names
                    var showFoldername = fileUtilities.generatePathname(showResponse);
                    var seasonFoldername = "Season " + episodeResponse.seasonNum;
                    String itemFilename;

                    // Generate the filename based on its type
                    if (filesAreSubtitles) {
                        itemFilename = fileUtilities.generateEpisodeSubtitleFilename(showResponse, seasonString + episodeString,
                                (ParsedSubtitleFilename) parsedFilename);
                    } else {
                        itemFilename = fileUtilities.generateEpisodeFilename(episodeResponse, FileType.determineFiletype(file.getName()),
                                seasonString + episodeString, showResponse);
                    }

                    // Create the show folder
                    fileUtilities.createFolder(tvFolder + showFoldername);

                    // Add the show to the database
                    entityUtilities.addOrUpdateSeries(showResponse, showFoldername);

                    // Fetch the show from the database
                    var show = entityUtilities.findSeries(showResponse.tmdbId);

                    // Create the season folder
                    fileUtilities.createFolder(tvFolder + showFoldername + "/" + seasonFoldername);

                    // Move the file into place as long as it is not overwriting a file without the proper flag. It only requires
                    // the overwrite flag if the file is not one that has been optimized.
                    if (file.getAbsolutePath().contains(importFolder + "optimized/episodes")) {
                        // Ensure that old media files get deleted if they are being replaced by a file of a different type
                        if (!filesAreSubtitles && !episodeDao.getByTmdbId(episodeResponse.tmdbId).filename.equalsIgnoreCase(itemFilename)) {
                            fileUtilities.deleteFile(tvFolder + showFoldername + "/" + seasonFoldername + "/" + episodeDao.getByTmdbId(episodeResponse.tmdbId).filename);
                        }

                        // Move the item into place
                        fileUtilities.moveMedia(importFolder + "optimized/episodes/" + file.getName(),
                                tvFolder + showFoldername + "/" + seasonFoldername + "/" + itemFilename, true);
                    } else {
                        // Check if the item is in the database at all
                        var existsInDatabase = filesAreSubtitles ? episodeSubtitleDao.existsByFilename(itemFilename) : episodeDao.existsByTmdbId(episodeResponse.tmdbId);

                        // Verify that the file does not exist. If it does and overwrite is not specified skip this file
                        if (Files.exists(Paths.get(tvFolder + showFoldername + "/" + seasonFoldername + "/" + itemFilename)) && !overwrite || existsInDatabase && !overwrite) {
                            new MessageBuilder()
                                    .setEmbed(messageFormatter.errorMessage("The following item already exists. " +
                                            "Please use the --overwrite flag if you wish to overwrite this file: " +
                                            file.getAbsolutePath()))
                                    .send(replyMessage.getChannel())
                                    .exceptionally(ExceptionLogger.get())
                                    .join();
                            progress.getAndIncrement();
                            multiEmitter.emit(progress.get());
                            continue;
                        }

                        // Ensure that old media files get deleted if they are being replaced by a file of a different type
                        if (overwrite && !filesAreSubtitles && !episodeDao.getByTmdbId(episodeResponse.tmdbId).filename.equalsIgnoreCase(itemFilename)) {
                            fileUtilities.deleteFile(tvFolder + showFoldername + "/" + seasonFoldername + "/" + episodeDao.getByTmdbId(episodeResponse.tmdbId).filename);
                        }

                        // Ensure that old subtitles are deleted if the media file is being overwritten
                        if (overwrite && !filesAreSubtitles) {
                            // Fetch a list of subtitles matching this episode
                            var subtitleList = new ArrayList<>(episodeSubtitleDao.getByEpisode(episodeDao.getByTmdbId(episodeResponse.tmdbId)));

                            // Delete the file from the filesystem and database
                            subtitleList.forEach(subtitle -> {
                                fileUtilities.deleteFile(tvFolder + showFoldername + "/" + seasonFoldername + "/" + subtitle.filename);
                                episodeSubtitleDao.delete(subtitle.id);
                            });
                        }

                        // Move the item into place
                        fileUtilities.moveMedia(importFolder + "episodes/" + file.getName(),
                                tvFolder + showFoldername + "/" + seasonFoldername + "/" + itemFilename, overwrite);
                    }

                    // Add the item to the database
                    if (filesAreSubtitles) {
                        var linkedEpisode = episodeDao.getByTmdbId(episodeResponse.tmdbId);
                        entityUtilities.addOrUpdateEpisodeSubtitle(itemFilename, (ParsedSubtitleFilename) parsedFilename, linkedEpisode);
                    } else {
                        entityUtilities.addOrUpdateEpisode(episodeResponse, itemFilename, show);
                    }

                    // Send a message showing the episode has been added to the server if it is a episode
                    if (!filesAreSubtitles) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.newEpisodeNotification(episodeResponse, showResponse))
                                .send(discordApi.getTextChannelById(newEpisodeNotificationChannel).orElseThrow())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                    }

                    // Increment progress
                    progress.getAndIncrement();
                    multiEmitter.emit(progress.get());
                } catch (Exception e) {
                    new MessageBuilder()
                            .setEmbed(messageFormatter.errorMessage("There was an error while importing the following file: " +
                                    file.getName(), e.getMessage()))
                            .send(replyMessage.getChannel())
                            .exceptionally(ExceptionLogger.get())
                            .join();
                    progress.getAndIncrement();
                    multiEmitter.emit(progress.get());
                    reportError(e);
                }
            }
            multiEmitter.complete();
        });
    }

    @SuppressWarnings("DuplicatedCode")
    private Multi<Integer> processMovieItems(Collection<File> files, boolean filesAreSubtitles) {
        AtomicInteger progress = new AtomicInteger(0);

        return Multi.createFrom().emitter(multiEmitter -> {
            for (File file : files) {
                try {
                    // Cancel if the process was canceled by the user
                    if (this.stopProcess) {
                        multiEmitter.fail(new InterruptedException("Process canceled by user"));
                        return;
                    }

                    // Parse the filename into its core components
                    Object parsedFilename;
                    try {
                        parsedFilename = filesAreSubtitles ?
                                new ParsedSubtitleFilename().parseFilename(file.getName()) : new ParsedMediaFilename().parseFilename(file.getName());
                    } catch (IllegalArgumentException e) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage(e.getMessage()))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        progress.getAndIncrement();
                        multiEmitter.emit(progress.get());
                        continue;
                    }

                    // Verify the ID is a IMDB ID or a TMDB ID
                    var parsedId = filesAreSubtitles ? ((ParsedSubtitleFilename) parsedFilename).id : ((ParsedMediaFilename) parsedFilename).id;
                    if (!parsedId.matches("^tt[0-9]{7,8}") || !parsedId.matches("^[0-9]{1,12}")) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage("The following file in the movies import folder is not using a valid IMDB or TMDB id. " +
                                        "Please make sure that only files using valid IMDB or TMDB IDs are in the movies import folder.\n\n" + file.getName()))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        progress.getAndIncrement();
                        multiEmitter.emit(progress.get());
                        continue;
                    }

                    // Fetch information about this movie. Method used depends on if the ID is TMDB or IMDB
                    TmdbMovie movieResponse;
                    if (parsedId.matches("^tt[0-9]{7,8}")) {
                        // Find the movie using the find endpoint since this is a IMDB ID
                        var results = tmdbService.findByExternalId(parsedId, TmdbSourceIdType.IMDB);

                        // Verify that the search was successful, and if so select the first result as that should be the correct movie
                        if (results.isSuccessful() && !results.movies.isEmpty()) {
                            movieResponse = results.movies.get(0);
                        } else {
                            // Display a error for this file since it could mot match to a movie
                            new MessageBuilder()
                                    .setEmbed(messageFormatter.warningMessage("Unable to match the following file to a TMDB Movie: " + file.getName()))
                                    .send(replyMessage.getChannel())
                                    .exceptionally(ExceptionLogger.get())
                                    .join();
                            progress.getAndIncrement();
                            multiEmitter.emit(progress.get());
                            continue;
                        }
                    } else {
                        // Fetch the movie from TMDB using its TMDB ID parsed from the filename
                        var result = tmdbService.getMovie(Long.parseLong(parsedId));

                        // Verify that the data retrieval was successful and set the correct movie if it was
                        if (result.isSuccessful()) {
                            movieResponse = result;
                        } else {
                            // Display a error for this file since it could mot match to a movie
                            new MessageBuilder()
                                    .setEmbed(messageFormatter.warningMessage("Unable to match the following file to a TMDB Movie: " + file.getName()))
                                    .send(replyMessage.getChannel())
                                    .exceptionally(ExceptionLogger.get())
                                    .join();
                            progress.getAndIncrement();
                            multiEmitter.emit(progress.get());
                            continue;
                        }
                    }

                    // Verify the movie exists if this is a subtitle file
                    if (filesAreSubtitles && !movieDao.existsByTmdbId(movieResponse.tmdbId)) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage("You attempted to import the following subtitle file, however the corresponding movie " +
                                        "does not exist in the system. Please add the movie before adding the subtitle.\n" + file.getName()))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        progress.getAndIncrement();
                        multiEmitter.emit(progress.get());
                        continue;
                    }

                    // Generate movie foldername
                    var foldername = fileUtilities.generatePathname(movieResponse);

                    // Generate item filename
                    String itemFilename;
                    if (filesAreSubtitles) {
                        itemFilename = fileUtilities.generateMovieSubtitleFilename(movieResponse, (ParsedSubtitleFilename) parsedFilename);
                    } else {
                        itemFilename = fileUtilities.generateMovieFilename(movieResponse, FileType.determineFiletype(file.getName()));
                    }

                    // Create the movie folder
                    fileUtilities.createFolder(movieResponse);

                    // Move the file into place as long as it is not overwriting a file without the proper flag. It only requires
                    // the overwrite flag if the file is not one that has been optimized.
                    if (file.getAbsolutePath().contains(importFolder + "optimized/movies")) {
                        // Ensure that old media files get deleted if they are being replaced by a file of a different type
                        if (!filesAreSubtitles && !movieDao.getByTmdbId(movieResponse.tmdbId).filename.equalsIgnoreCase(itemFilename)) {
                            fileUtilities.deleteFile(movieFolder + foldername + "/" + movieDao.getByTmdbId(movieResponse.tmdbId).filename);
                        }

                        // Move the item into place
                        fileUtilities.moveMedia(importFolder + "optimized/movies/" + file.getName(),
                                movieFolder + foldername + "/" + itemFilename, true);
                    } else {
                        // Check if the item is in the database at all
                        var existsInDatabase = filesAreSubtitles ? movieSubtitleDao.existsByFilename(itemFilename) : movieDao.existsByTmdbId(movieResponse.tmdbId);

                        // Verify that the file does not exist. If it does and overwrite is not specified skip this file
                        if (Files.exists(Paths.get(movieFolder + foldername + "/" + itemFilename)) && !overwrite || existsInDatabase && !overwrite) {
                            new MessageBuilder()
                                    .setEmbed(messageFormatter.errorMessage("The following item already exists. " +
                                            "Please use the --overwrite flag if you wish to overwrite this file: " +
                                            file.getAbsolutePath()))
                                    .send(replyMessage.getChannel())
                                    .exceptionally(ExceptionLogger.get())
                                    .join();
                            progress.getAndIncrement();
                            multiEmitter.emit(progress.get());
                            continue;
                        }

                        // Ensure that old media files get deleted if they are being replaced by a file of a different type
                        if (overwrite && !filesAreSubtitles && !movieDao.getByTmdbId(movieResponse.tmdbId).filename.equalsIgnoreCase(itemFilename)) {
                            fileUtilities.deleteFile(movieFolder + foldername + "/" + movieDao.getByTmdbId(movieResponse.tmdbId).filename);
                        }

                        // Ensure that old subtitles are deleted if the media file is being overwritten
                        if (overwrite && !filesAreSubtitles) {
                            // Fetch a list of subtitles matching this movie
                            var subtitleList = new ArrayList<>(movieSubtitleDao.getByMovie(movieDao.getByTmdbId(movieResponse.tmdbId)));

                            // Delete the file from the filesystem and database
                            subtitleList.forEach(subtitle -> {
                                fileUtilities.deleteFile(movieFolder + foldername + "/" + subtitle.filename);
                                movieSubtitleDao.delete(subtitle.id);
                            });
                        }

                        // Move the item into place
                        fileUtilities.moveMedia(importFolder + "movies/" + file.getName(),
                                movieFolder + foldername + "/" + itemFilename, overwrite);
                    }

                    // Add the item to the database
                    if (filesAreSubtitles) {
                        var linkedMovie = movieDao.getByTmdbId(movieResponse.tmdbId);
                        entityUtilities.addOrUpdateMovieSubtitle(itemFilename, (ParsedSubtitleFilename) parsedFilename, linkedMovie);
                    } else {
                        entityUtilities.addOrUpdateMovie(movieResponse, itemFilename);
                    }

                    // Send a message showing the episode has been added to the server if it is a episode
                    if (!filesAreSubtitles) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.newMovieNotification(movieResponse))
                                .send(discordApi.getTextChannelById(newMovieNotificationChannel).orElseThrow())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                    }

                    // If the item was a movie and it was in the waitlist, remove it from the waitlist and send a notification to the requesting user
                    if (!filesAreSubtitles && entityUtilities.waitlistMovieExists(movieResponse.tmdbId)) {
                        var requestingUser = entityUtilities.getWaitlistMovie(movieResponse.tmdbId).requestedBy;
                        new MessageBuilder()
                                .setEmbed(messageFormatter.newMovieUserNotification(movieResponse))
                                .send(discordApi.getUserById(requestingUser).join())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        entityUtilities.deleteWaitlistMovie(movieResponse.tmdbId);
                    }

                    // Increment progress
                    progress.getAndIncrement();
                    multiEmitter.emit(progress.get());
                } catch (Exception e) {
                    new MessageBuilder()
                            .setEmbed(messageFormatter.errorMessage("There was an error while importing the following file: " +
                                    file.getName(), e.getMessage()))
                            .send(replyMessage.getChannel())
                            .exceptionally(ExceptionLogger.get())
                            .join();
                    progress.getAndIncrement();
                    multiEmitter.emit(progress.get());
                    reportError(e);
                }
            }
        });
    }
}