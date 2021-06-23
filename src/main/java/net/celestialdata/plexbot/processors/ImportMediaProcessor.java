package net.celestialdata.plexbot.processors;

import io.quarkus.arc.log.LoggerName;
import io.smallrye.mutiny.Multi;
import net.celestialdata.plexbot.clients.models.omdb.enums.OmdbResponseEnum;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbRemoteID;
import net.celestialdata.plexbot.clients.services.OmdbService;
import net.celestialdata.plexbot.clients.services.SyncthingService;
import net.celestialdata.plexbot.clients.services.TvdbService;
import net.celestialdata.plexbot.dataobjects.ParsedMediaFilename;
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
    private int currentPos = 0;
    private boolean overwrite = false;
    private Message replyMessage;

    @LoggerName("net.celestialdata.plexbot.processors.ImportMediaProcessor")
    Logger logger;

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

    @ConfigProperty(name = "SyncthingSettings.movieFolderId")
    String syncthingMovieFolderId;

    @ConfigProperty(name = "SyncthingSettings.tvFolderId")
    String syncthingTvFolderId;

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

    public void processImport(Message replyMessage, boolean skipSync, boolean overwrite) {
        // TODO: Ensure there are no previous instance of the import processor running to avoid interference

        // Configure this process
        configureProcess("Import Processor - initializing", replyMessage);
        this.overwrite = overwrite;
        this.replyMessage = replyMessage;

        try {
            // Verify that SyncThing is not currently syncing the import folder
            // Using the --skipSync flag in the command should override this and allow the import to proceed anyways
            AtomicBoolean syncthingSucceeded = new AtomicBoolean(true);
            if (syncthingEnabled && !skipSync) {
                awaitSyncthing().subscribe().with(
                        progress -> {
                            updateProcessString("Import Processor - Awaiting Sync: " + decimalFormatter.format(progress) + "%");
                            replyMessage.edit(messageFormatter.importProgressMessage("Awaiting Syncthing Sync: " + decimalFormatter.format(progress) + "%"))
                                    .exceptionally(ExceptionLogger.get());
                        },
                        failure -> {
                            replyMessage.edit(messageFormatter.errorMessage("Failed while waiting for Syncthing.", failure.getMessage()))
                                    .exceptionally(ExceptionLogger.get());
                            logger.error(failure);
                            syncthingSucceeded.set(false);
                            reportError(failure);
                            endProcess();
                        },
                        () -> {

                        }
                );
            }

            // Fail the process if there was an issue with waiting for Syncthing
            if (!syncthingSucceeded.get()) {
                replyMessage.edit(messageFormatter.errorMessage("Failed while waiting for Syncthing.")).exceptionally(ExceptionLogger.get());
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

            // Calculate the total number of files to process and ensure the current position is at 0
            totalNumFiles = episodeMediaFiles.size() + movieMediaFiles.size() + episodeSubtitleFiles.size() + movieSubtitleFiles.size();
            currentPos = 0;

            // Ensure there are actually files to import, otherwise exit
            if (totalNumFiles == 0) {
                replyMessage.edit(messageFormatter.warningMessage("There were no files available to import. Please make sure they are " +
                        "in the proper folders before continuing.")).exceptionally(ExceptionLogger.get());
                endProcess();
                return;
            }

            // Update the progress message to show it has started processing media
            this.replyMessage.edit(messageFormatter.importProgressMessage("Processing file 1 of " + totalNumFiles));

            // Process all episode media files
            processEpisodes(episodeMediaFiles).subscribe().with(
                    progress -> {
                        var percentage = (((double) (currentPos + progress) / totalNumFiles) * 100);

                        // Update the bot status process string
                        updateProcessString("Import Processor - " + decimalFormatter.format(percentage) + "%");

                        // Update the progress message
                        if (lastUpdate.plus(3, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                            this.replyMessage.edit(messageFormatter.importProgressMessage("Processing file " + (currentPos + progress) + " of " + totalNumFiles));
                            lastUpdate = LocalDateTime.now();
                        }
                    },
                    this::reportError,
                    () -> currentPos = currentPos + episodeMediaFiles.size()
            );

            // Process all episode subtitle files
            processEpisodeSubtitles(episodeSubtitleFiles);

            // Process all movie media files
            processMovies(movieMediaFiles);

            // Process movie subtitle files
            processMovieSubtitles(movieSubtitleFiles);
        } catch (Exception e) {
            reportError(e);
            endProcess();
        }

        replyMessage.edit(new EmbedBuilder()
                .setTitle("Import Processor")
                .setDescription("You have requested the bot import media contained within the import folder. This action has been completed.")
                .setColor(Color.GREEN)
                .setFooter("Finished: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        .format(ZonedDateTime.now()) + " CST")
        );

        endProcess();
    }

    private Multi<Double> awaitSyncthing() {
        return Multi.createFrom().emitter(multiEmitter -> {
            try {
                var syncComplete = true;
                var lastEmitted = LocalDateTime.now();

                do {
                    // Reset the progress
                    var progress = 0.00;
                    syncComplete = true;

                    // Cycle through the devices and check their status and calculate the overall sync status
                    for (String deviceId : syncthingDevices) {
                        var response = syncthingService.getCompletionStatus(syncthingImportFolderId, deviceId);

                        if (response.completion != 100) {
                            syncComplete = false;
                            progress = progress + (response.completion / syncthingDevices.size());
                        }
                    }

                    // Emit the progress if time since last update has been long enough
                    if (lastEmitted.plus(3, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
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

    public Multi<Integer> processEpisodes(Collection<File> episodeFiles) {
        AtomicInteger episodeProgress = new AtomicInteger();

        return Multi.createFrom().emitter(multiEmitter -> {
            for (File file : episodeFiles) {
                try {
                    // Parse the media filename into its component parts for later usage
                    var parsedFilename = new ParsedMediaFilename().parseFilename(file.getName());

                    // Fetch information from TVDB about this episode
                    var episodeResponse = tvdbService.getExtendedEpisode(parsedFilename.id);

                    // Ensure that the request was successful
                    if (episodeResponse.status.equals("failure")) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage("Unable to match the following file to a TVDB episode: " +
                                        file.getName(), episodeResponse.message))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        episodeProgress.getAndIncrement();
                        multiEmitter.emit(episodeProgress.get());
                        continue;
                    }

                    // Fetch information about this episodes series from TVDB
                    var seriesResponse = tvdbService.getSeries(String.valueOf(episodeResponse.extendedEpisode.seriesId));

                    // Ensure that this request was also successful
                    if (seriesResponse.status.equals("failure")) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage("Unable to match the following file to a TVDB series: " +
                                        file.getName(), episodeResponse.message))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        episodeProgress.getAndIncrement();
                        multiEmitter.emit(episodeProgress.get());
                        continue;
                    }

                    // Verify that the episode name is populated, otherwise check to see if IMDb has it
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

                    // If the episode name is still unavailable skip this one and display a warning
                    if (episodeResponse.extendedEpisode.name == null || episodeResponse.extendedEpisode.name.isBlank()) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.warningMessage("Unable to locate a name on TVDB or IMDb for the " +
                                        "following episode file: " + file.getName(), episodeResponse.message))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        episodeProgress.getAndIncrement();
                        multiEmitter.emit(episodeProgress.get());
                        continue;
                    }

                    // Create the strings used in the file/folder names for the episode and season numbers
                    var episodeString = generateEpisodeString(episodeResponse.extendedEpisode.number);
                    var seasonString = generateSeasonString(episodeResponse.extendedEpisode.seasonNumber);

                    // Generate the season, show, and episode folder/filenames
                    var showFoldername = fileUtilities.generatePathname(seriesResponse.series);
                    var seasonFoldername = "Season " + episodeResponse.extendedEpisode.seasonNumber;
                    var episodeFilename = fileUtilities.generateEpisodeFilename(
                            episodeResponse.extendedEpisode,
                            FileType.determineFiletype(file.getName()),
                            seasonString + episodeString,
                            seriesResponse.series
                    );

                    // Create the show folder
                    fileUtilities.createFolder(tvFolder + showFoldername);

                    // Add the show to the database
                    entityUtilities.addOrUpdateSeries(seriesResponse.series, showFoldername);

                    // Create the season folder
                    fileUtilities.createFolder(tvFolder + showFoldername + "/" + seasonFoldername);

                    // Fetch the show from the database
                    var show = entityUtilities.findSeries(String.valueOf(seriesResponse.series.id));

                    // Add the season to the database if it does not exist
                    if (!entityUtilities.seasonExists(show, episodeResponse.extendedEpisode.seasonNumber)) {
                        entityUtilities.addOrUpdateSeason(episodeResponse.extendedEpisode.seasonNumber, seasonFoldername, show);
                    }

                    // Fetch the season from the database
                    var season = entityUtilities.findSeason(show, episodeResponse.extendedEpisode.seasonNumber);

                    // Verify that the file does not exist. If it does and overwrite is not specified skip this file
                    if (Files.exists(Paths.get(tvFolder + showFoldername + "/" + seasonFoldername + "/" + episodeFilename)) && !overwrite) {
                        new MessageBuilder()
                                .setEmbed(messageFormatter.errorMessage("The following episode already exists in the filesystem. " +
                                        "Please use the --overwrite flag if you wish to overwrite this file: " +
                                        file.getName()))
                                .send(replyMessage.getChannel())
                                .exceptionally(ExceptionLogger.get())
                                .join();
                        episodeProgress.getAndIncrement();
                        multiEmitter.emit(episodeProgress.get());
                        continue;
                    }

                    // Move the episode file into place
                    fileUtilities.moveMedia(importFolder + "episodes/" + file.getName(),
                            tvFolder + showFoldername + "/" + seasonFoldername + "/" + episodeFilename, overwrite);

                    // Add the episode to the database
                    entityUtilities.addOrUpdateEpisode(episodeResponse.extendedEpisode, episodeFilename, season, show);

                    // Send a message showing the episode has been added to the server
                    new MessageBuilder()
                            .setEmbed(messageFormatter.newEpisodeNotification(episodeResponse.extendedEpisode, seriesResponse.series))
                            .send(discordApi.getTextChannelById(newEpisodeNotificationChannel).orElseThrow())
                            .exceptionally(ExceptionLogger.get())
                            .join();

                    episodeProgress.getAndIncrement();
                    multiEmitter.emit(episodeProgress.get());
                } catch (Exception e) {
                    new MessageBuilder()
                            .setEmbed(messageFormatter.errorMessage("There was an error while importing the following file: " +
                                    file.getName(), e.getMessage()))
                            .send(replyMessage.getChannel())
                            .exceptionally(ExceptionLogger.get())
                            .join();
                    episodeProgress.getAndIncrement();
                    multiEmitter.emit(episodeProgress.get());
                    reportError(e);
                }
            }
            multiEmitter.complete();
        });
    }

    private void processEpisodeSubtitles(Collection<File> episodeSubtitleFiles) {

    }

    private void processMovies(Collection<File> movieFiles) {

    }

    private void processMovieSubtitles(Collection<File> movieSubtitleFiles) {

    }
}