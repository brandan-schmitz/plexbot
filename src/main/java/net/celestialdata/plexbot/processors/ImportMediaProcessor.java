package net.celestialdata.plexbot.processors;

import io.quarkus.arc.log.LoggerName;
import io.smallrye.mutiny.Multi;
import net.celestialdata.plexbot.clients.models.omdb.enums.OmdbResponseEnum;
import net.celestialdata.plexbot.clients.models.omdb.enums.OmdbResultTypeEnum;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbRemoteID;
import net.celestialdata.plexbot.clients.services.OmdbService;
import net.celestialdata.plexbot.clients.services.SyncthingService;
import net.celestialdata.plexbot.clients.services.TvdbService;
import net.celestialdata.plexbot.dataobjects.ParsedMediaFilename;
import net.celestialdata.plexbot.dataobjects.ParsedSubtitleFilename;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.entities.EntityUtilities;
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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    @ConfigProperty(name = "ApiKeys.omdbApiKey")
    String omdbApiKey;

    @Inject
    FileUtilities fileUtilities;

    @Inject
    MessageFormatter messageFormatter;

    @Inject
    EntityUtilities entityUtilities;

    @Inject
    DiscordApi discordApi;

    @Inject
    @RestClient
    SyncthingService syncthingService;

    @Inject
    @RestClient
    OmdbService omdbService;

    @Inject
    @RestClient
    TvdbService tvdbService;

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

    public void processImport(Message replyMessage, Long commandMessageId, boolean skipSync, boolean overwrite) {
        // TODO: Ensure there are no previous instance of the import processor running to avoid interference

        // Configure the button click listener used to stop the import process
        this.cancelListener = discordApi.addButtonClickListener(clickEvent -> {
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

        // Configure the cancel listener to remove the button when the listener is removed
        this.cancelListener.addRemoveHandler(() -> {
            var replacementMessage = new MessageBuilder().copy(replyMessage);
            var channel = replyMessage.getChannel();
            replyMessage.delete();
            this.replyMessage = replacementMessage.replyTo(commandMessageId).send(channel).join();
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
                replyMessage.edit(messageFormatter.errorMessage("Failed while waiting for Syncthing.")).exceptionally(ExceptionLogger.get());
                this.cancelListener.remove();
                endProcess();
                return;
            }

            // Create the array of media file extensions to look for
            String[] mediaFileExtensions = {
                    FileType.AVI.getTypeString(),
                    FileType.DIVX.getTypeString(),
                    FileType.FLV.getTypeString(),
                    FileType.M4V.getTypeString(),
                    FileType.MKV.getTypeString(),
                    FileType.MP4.getTypeString(),
                    FileType.MPEG.getTypeString(),
                    FileType.MPG.getTypeString(),
                    FileType.WMV.getTypeString()
            };

            // Create the array of subtitle file extensions to look for
            String[] subtitleFileExtensions = {
                    FileType.SRT.getTypeString(),
                    FileType.SMI.getTypeString(),
                    FileType.SSA.getTypeString(),
                    FileType.ASS.getTypeString(),
                    FileType.VTT.getTypeString()
            };

            // Collect a list of media files to process
            Collection<File> episodeMediaFiles = FileUtils.listFiles(new File(importFolder + "episodes"), mediaFileExtensions, false);
            Collection<File> movieMediaFiles = FileUtils.listFiles(new File(importFolder + "movies"), mediaFileExtensions, false);

            // Collect a list of subtitle files to process
            Collection<File> episodeSubtitleFiles = FileUtils.listFiles(new File(importFolder + "episodes"), subtitleFileExtensions, false);
            Collection<File> movieSubtitleFiles = FileUtils.listFiles(new File(importFolder + "movies"), subtitleFileExtensions, false);

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
                replyMessage.edit(messageFormatter.warningMessage("There were no files available to import. Please make sure they are " +
                        "in the proper folders before continuing.")).exceptionally(ExceptionLogger.get());
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
            var channel = replyMessage.getChannel();
            replyMessage.delete().join();
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Import Processor")
                            .setDescription("You have requested the bot import media contained within the import folder. This action has been completed.")
                            .setColor(Color.GREEN)
                            .setFooter("Finished: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                                    .format(ZonedDateTime.now()) + " CST"))
                    .replyTo(commandMessageId)
                    .send(channel)
                    .join();
        }

        endProcess();
    }

    private void updateStatus(int progress) {
        var percentage = (((double) (currentPos + progress) / totalNumFiles) * 100);

        // Update the bot status process string
        updateProcessString("Import Processor - " + decimalFormatter.format(percentage) + "%");

        // Update the progress message
        if (lastUpdate.plus(3, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
            this.replyMessage.edit(messageFormatter.importProgressMessage("Processing file " + (currentPos + progress) + " of " + totalNumFiles));
            lastUpdate = LocalDateTime.now();
        }
    }

    private Multi<Double> awaitSyncthing() {
        return Multi.createFrom().emitter(multiEmitter -> {
            try {
                var syncComplete = false;
                var lastEmitted = LocalDateTime.now().minus(3, ChronoUnit.SECONDS);

                do {
                    // Reset the progress
                    var progress = 0.00;

                    // Wait 3 seconds between requests to avoid overloading syncthing
                    if (lastEmitted.plus(3, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
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
                } while (!syncComplete);

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
                    if (filesAreSubtitles && !entityUtilities.episodeExists(parsedId)) {
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

                    // Fetch information from TVDB about this item
                    var episodeResponse = tvdbService.getExtendedEpisode(parsedId);

                    // Ensure that the request was successful
                    if (episodeResponse.status.equals("failure")) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage("Unable to match the following file to a TVDB episode: " +
                                        file.getName(), episodeResponse.message))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        progress.getAndIncrement();
                        multiEmitter.emit(progress.get());
                        continue;
                    }

                    // Fetch information about this items series from TVDB
                    var seriesResponse = tvdbService.getSeries(String.valueOf(episodeResponse.extendedEpisode.seriesId));

                    // Ensure that this request was also successful
                    if (seriesResponse.status.equals("failure")) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage("Unable to match the following file to a TVDB series: " +
                                        file.getName(), episodeResponse.message))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        progress.getAndIncrement();
                        multiEmitter.emit(progress.get());
                        continue;
                    }

                    // Verify that the item's name is populated, otherwise check to see if IMDb has it
                    if (episodeResponse.extendedEpisode.name == null || episodeResponse.extendedEpisode.name.isBlank()) {
                        for (TvdbRemoteID id : episodeResponse.extendedEpisode.remoteIds) {
                            if (id.type == 2) {
                                // Check IMDb for the episode
                                var omdbResponse = omdbService.getById(id.id, omdbApiKey);

                                // If the response was successful, try setting the value of name to the value provided by IMDb
                                if (omdbResponse.response == OmdbResponseEnum.TRUE) {
                                    episodeResponse.extendedEpisode.name = omdbResponse.title;
                                }
                            }
                        }
                    }

                    // Create the strings used in the file/folder names for the episode and season numbers
                    var episodeString = generateEpisodeString(episodeResponse.extendedEpisode.number);
                    var seasonString = generateSeasonString(episodeResponse.extendedEpisode.seasonNumber);

                    // Generate the season and show folder names
                    var showFoldername = fileUtilities.generatePathname(seriesResponse.series);
                    var seasonFoldername = "Season " + episodeResponse.extendedEpisode.seasonNumber;
                    String itemFilename;

                    // Generate the filename based on its type
                    if (filesAreSubtitles) {
                        itemFilename = fileUtilities.generateEpisodeSubtitleFilename(
                                seriesResponse.series,
                                seasonString + episodeString,
                                (ParsedSubtitleFilename) parsedFilename
                        );
                    } else {
                        itemFilename = fileUtilities.generateEpisodeFilename(
                                episodeResponse.extendedEpisode,
                                FileType.determineFiletype(file.getName()),
                                seasonString + episodeString,
                                seriesResponse.series
                        );
                    }

                    // Create the show folder
                    fileUtilities.createFolder(tvFolder + showFoldername);

                    // Add the show to the database
                    entityUtilities.addOrUpdateSeries(seriesResponse.series, showFoldername);

                    // Fetch the show from the database
                    var show = entityUtilities.findSeries(String.valueOf(seriesResponse.series.id));

                    // Create the season folder
                    fileUtilities.createFolder(tvFolder + showFoldername + "/" + seasonFoldername);

                    // Check if the item is in the database at all
                    var existsInDatabase = filesAreSubtitles ? entityUtilities.episodeSubtitleExists(itemFilename) : entityUtilities.episodeExists(parsedId);

                    // Verify that the file does not exist. If it does and overwrite is not specified skip this file
                    if (Files.exists(Paths.get(tvFolder + showFoldername + "/" + seasonFoldername + "/" + itemFilename)) && !overwrite || existsInDatabase && !overwrite) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.errorMessage("The following item already exists. " +
                                        "Please use the --overwrite flag if you wish to overwrite this file: " +
                                        file.getName()))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        progress.getAndIncrement();
                        multiEmitter.emit(progress.get());
                        continue;
                    }

                    // Ensure that old media files get deleted if they are being replaced by a file of a different type
                    if (!filesAreSubtitles && !entityUtilities.getEpisode(parsedId).filename.equalsIgnoreCase(itemFilename)) {
                        fileUtilities.deleteFile(tvFolder + showFoldername + "/" + seasonFoldername + "/" + entityUtilities.getEpisode(parsedId).filename);
                    }

                    // Move the item into place
                    fileUtilities.moveMedia(importFolder + "episodes/" + file.getName(),
                            tvFolder + showFoldername + "/" + seasonFoldername + "/" + itemFilename, overwrite);

                    // Add the item to the database
                    if (filesAreSubtitles) {
                        var linkedEpisode = entityUtilities.getEpisode(parsedId);
                        entityUtilities.addOrUpdateEpisodeSubtitle(itemFilename, (ParsedSubtitleFilename) parsedFilename, linkedEpisode);
                    } else {
                        entityUtilities.addOrUpdateEpisode(episodeResponse.extendedEpisode, itemFilename, show);
                    }

                    // Send a message showing the episode has been added to the server if it is a episode
                    if (!filesAreSubtitles) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.newEpisodeNotification(episodeResponse.extendedEpisode, seriesResponse.series))
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

                    // Verify the ID is a tvdb ID and not a IMDb ID
                    var parsedId = filesAreSubtitles ? ((ParsedSubtitleFilename) parsedFilename).id : ((ParsedMediaFilename) parsedFilename).id;
                    if (!parsedId.matches("^(tt\\d{7,8})$")) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage("The following file in the movies import folder is not using a valid IMDb id. " +
                                        "Please make sure that only files using valid IMDb ids are in the movies import folder.\n\n" + file.getName()))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        progress.getAndIncrement();
                        multiEmitter.emit(progress.get());
                        continue;
                    }

                    // Verify the movie exists if this is a subtitle file
                    if (filesAreSubtitles && !entityUtilities.movieExists(parsedId)) {
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

                    // Fetch information from OMDb about this item
                    var omdbResponse = omdbService.getById(parsedId, omdbApiKey);

                    // Ensure that the request was successful
                    if (omdbResponse.response == OmdbResponseEnum.FALSE) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage("Unable to match the following file to a IMDb Movie: " +
                                        file.getName(), omdbResponse.error))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        progress.getAndIncrement();
                        multiEmitter.emit(progress.get());
                        continue;
                    }

                    // Ensure that the item being requested is actually a movie
                    if (omdbResponse.type != OmdbResultTypeEnum.MOVIE) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage("The following file uses an IMDb ID code but is not a movie. " +
                                        "Please make sure that only movies use an IMDb code. TV Episodes should use the corresponding ID from https://thetvdb.com\n" +
                                        file.getName()))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        progress.getAndIncrement();
                        multiEmitter.emit(progress.get());
                        continue;
                    }

                    // Generate movie foldername
                    var foldername = fileUtilities.generatePathname(omdbResponse);

                    // Generate item filename
                    String itemFilename;
                    if (filesAreSubtitles) {
                        itemFilename = fileUtilities.generateMovieSubtitleFilename(omdbResponse, (ParsedSubtitleFilename) parsedFilename);
                    } else {
                        itemFilename = fileUtilities.generateMovieFilename(omdbResponse, FileType.determineFiletype(file.getName()));
                    }

                    // Create the movie folder
                    fileUtilities.createFolder(omdbResponse);

                    // Check if the item is in the database at all
                    var existsInDatabase = filesAreSubtitles ? entityUtilities.movieSubtitleExists(itemFilename) : entityUtilities.movieExists(parsedId);

                    // Verify that the file does not exist. If it does and overwrite is not specified skip this file
                    if (Files.exists(Paths.get(movieFolder + foldername + "/" + itemFilename)) && !overwrite || existsInDatabase && !overwrite) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.errorMessage("The following item already exists. " +
                                        "Please use the --overwrite flag if you wish to overwrite this file: " +
                                        file.getName()))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        progress.getAndIncrement();
                        multiEmitter.emit(progress.get());
                        continue;
                    }

                    // Ensure that old media files get deleted if they are being replaced by a file of a different type
                    if (!filesAreSubtitles && !entityUtilities.getMovie(parsedId).filename.equalsIgnoreCase(itemFilename)) {
                        fileUtilities.deleteFile(movieFolder + foldername + "/" + entityUtilities.getMovie(parsedId).filename);
                    }

                    // Move the item into place
                    fileUtilities.moveMedia(importFolder + "movies/" + file.getName(),
                            movieFolder + foldername + "/" + itemFilename, overwrite);

                    // Add the item to the database
                    if (filesAreSubtitles) {
                        var linkedMovie = entityUtilities.getMovie(parsedId);
                        entityUtilities.addOrUpdateMovieSubtitle(itemFilename, (ParsedSubtitleFilename) parsedFilename, linkedMovie);
                    } else {
                        entityUtilities.addOrUpdateMovie(omdbResponse, itemFilename);
                    }

                    // Send a message showing the episode has been added to the server if it is a episode
                    if (!filesAreSubtitles) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.newMovieNotification(omdbResponse))
                                .send(discordApi.getTextChannelById(newMovieNotificationChannel).orElseThrow())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                    }

                    // If the item was a movie and it was in the waitlist, remove it from the waitlist and send a notification to the requesting user
                    if (!filesAreSubtitles && entityUtilities.waitlistMovieExists(omdbResponse.imdbID)) {
                        var requestingUser = entityUtilities.getWaitlistMovie(omdbResponse.imdbID).requestedBy;
                        new MessageBuilder()
                                .setEmbed(messageFormatter.newMovieUserNotification(omdbResponse))
                                .send(discordApi.getUserById(requestingUser).join())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        entityUtilities.deleteWaitlistMovie(omdbResponse.imdbID);
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