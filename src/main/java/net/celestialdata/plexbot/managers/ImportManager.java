package net.celestialdata.plexbot.managers;

import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.client.ApiException;
import net.celestialdata.plexbot.client.BotClient;
import net.celestialdata.plexbot.client.model.EpisodeBaseRecord;
import net.celestialdata.plexbot.client.model.OmdbItem;
import net.celestialdata.plexbot.client.model.SeriesBaseRecord;
import net.celestialdata.plexbot.configuration.BotConfig;
import net.celestialdata.plexbot.database.DbOperations;
import net.celestialdata.plexbot.database.builders.*;
import net.celestialdata.plexbot.database.models.Episode;
import net.celestialdata.plexbot.database.models.Movie;
import net.celestialdata.plexbot.database.models.Season;
import net.celestialdata.plexbot.database.models.Show;
import net.celestialdata.plexbot.utils.*;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.api.util.logging.ExceptionLogger;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ImportManager implements CustomRunnable {
    private final Message sentMessage;
    private final boolean overwrite;
    private final boolean skipSync;
    private boolean stop = false;
    private final ListenerManager<ReactionAddListener> reactionListener;
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public ImportManager(Message sentMessage, boolean overwrite, boolean skipSync) {
        this.sentMessage = sentMessage;
        this.overwrite = overwrite;
        this.skipSync = skipSync;

        // Configure the message to have the stop reaction and be able to safely
        // stop the import process by allowing the user to click the reaction to stop
        // the import process after whichever media file currently being imported is finished.
        this.sentMessage.addReaction(BotEmojis.STOP);
        reactionListener = this.sentMessage.addReactionAddListener(reactionAddEvent -> {
            if (!reactionAddEvent.getUser().map(User::isBot).orElse(true)) {
                sentMessage.removeAllReactions();
                sentMessage.edit(new EmbedBuilder()
                        .setTitle("Stopping Import")
                        .setDescription("The bot will finish importing the current media item " +
                                "then stop the import process. This will ensure no media files are " +
                                "corrupted by an improper stopping of the file transfer that occurs " +
                                "during the import process.")
                        .setColor(BotColors.WARNING)
                );
                stop = true;
            }
        });

        // Have the reactions removed when the reaction listener is done
        reactionListener.addRemoveHandler(sentMessage::removeAllReactions);
    }

    @Override
    public String taskName() {
        return "Import Manager";
    }

    @Override
    public boolean cancelOnFull() {
        return false;
    }

    @Override
    public boolean cancelOnDuplicate() {
        return true;
    }

    @Override
    public void endTask() {
        reactionListener.remove();
        BotStatusManager.getInstance().removeProcess(taskName());
        BotStatusManager.getInstance().clearImportManagerStatus();
    }

    @Override
    public void endTask(Throwable error) {
        reactionListener.remove();
        reportError(error);
        BotStatusManager.getInstance().removeProcess(taskName());
        BotStatusManager.getInstance().clearImportManagerStatus();
    }

    private void onCancel() {
        sentMessage.edit(new EmbedBuilder()
                .setTitle("Import Canceled")
                .setDescription("The import has been canceled after the " + BotEmojis.STOP + " reaction " +
                        "was pressed. You can resume this import by running the import command again.")
                .setColor(BotColors.KILLED)
        );
        endTask();
    }

    /**
     * Send a message to the channel a import request command was sent with a warning about the import process.
     *
     * @param title     warning title
     * @param message   warning message
     * @param codeBlock warning code block
     */
    private void displayWarning(String title, String message, String codeBlock) {
        sentMessage.getChannel().sendMessage(new EmbedBuilder()
                .setTitle(title)
                .setDescription(message + "\n```" + codeBlock + "\n```")
                .setColor(BotColors.WARNING)
        );
    }

    /**
     * Changes the sentMessage to an error message if there is an error.
     * This one also adds a specified error code to the footer of the message.
     *
     * @param errorMessage The error message to set the body of the message to.
     * @param errorCode    The error code to add to the message footer.
     */
    private void displayError(String errorMessage, String errorCode) {
        sentMessage.edit(new EmbedBuilder()
                .addField("An error has occurred:", "```" + errorMessage + "```")
                .setFooter("Error Code:  " + errorCode)
                .setColor(BotColors.ERROR)
        );
    }

    private void updateProgressMessages(int current, int total) {
        BotStatusManager.getInstance().setImportManagerStatus(current, total);
        if (lastUpdated.plus(2, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
            sentMessage.edit(new EmbedBuilder()
                    .setTitle("Import Started!")
                    .setDescription("The bot has started the process of importing your media. Any warnings " +
                            "or errors will appear in this channel.")
                    .addField("Status:", "```\n" + current + "/" + total + " files processed\n```")
                    .setColor(BotColors.INFO)
            );
            lastUpdated = LocalDateTime.now();
        }
    }

    /**
     * Move a file from one location to another. Destination folder should already exist prior to moving the file.
     *
     * @param file        original file to move
     * @param destination destination folder and filename
     * @return true if moved successfully, false if move failed
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean moveMedia(String file, String destination) {
        boolean success = true;

        try {
            // Copy the file to the destination
            if (overwrite) {
                Files.copy(
                        Paths.get(BotConfig.getInstance().importFolder() + file),
                        Paths.get(destination),
                        StandardCopyOption.REPLACE_EXISTING
                );
            } else {
                Files.copy(
                        Paths.get(BotConfig.getInstance().importFolder() + file),
                        Paths.get(destination)
                );
            }

            // Delete the source file if the copy was successful
            Files.delete(Path.of(BotConfig.getInstance().importFolder() + file));
        } catch (Exception e) {
            displayError("An unknown error occurred while processing the " + file + " file, it will be skipped. " +
                    "Brandan has been notified of this error.", e.getMessage());
            reportError(e);
            success = false;
        }

        return success;
    }

    /**
     * Create a folder on the filesystem. Folder path should be a complete path.
     *
     * @param folderPath path to new folder
     * @param file       file this operation is for (used in reporting errors)
     * @return true if creation was successful, false if creation failed
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean createFolder(String folderPath, String file) {
        boolean success = true;

        try {
            Files.createDirectory(Paths.get(folderPath));
        } catch (IOException e) {
            if (!(e instanceof FileAlreadyExistsException)) {
                sentMessage.edit(new EmbedBuilder()
                        .addField("An error has occurred:", "```An unknown error occurred while processing the " + file +
                                " file. Brandan has been notified of this error.```")
                        .setFooter(e.getMessage())
                        .setColor(BotColors.ERROR)
                );
                reportError(e);
                success = false;
            }
        }

        return success;
    }

    // Run the import process
    @Override
    @SuppressWarnings("DuplicatedCode")
    public void run() {
        // Setup the default exception handler for this process
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            sentMessage.edit(new EmbedBuilder()
                    .setTitle("An error has occurred!")
                    .setDescription("An unknown error has occurred while processing media files and the process was forced to stop. " +
                            "Brandan has been notified of the error. Please try again later.")
                    .setFooter(e.getMessage())
                    .setColor(BotColors.ERROR)
            );
            reactionListener.remove();
            endTask(e);
            e.printStackTrace();
        });

        // If the SyncThing integration is enabled, make sure the import folder is done syncing
        if (BotConfig.getInstance().syncthingEnabled() && !skipSync) {
            boolean isSyncing = false;

            // Check all SyncThing servers listed to make sure it is not syncing media
            for (String device : BotConfig.getInstance().syncthingDevices()) {
                try {
                    isSyncing = BotClient.getInstance()
                            .syncthingApi.getCompletionStatus(BotConfig.getInstance().syncthingImportFolderId(), device)
                            .getCompletion() != 100;
                } catch (ApiException e) {
                    displayError("An unknown error has occurred. Brandan has been notified.", e.getMessage());
                    endTask(e);
                    return;
                }
            }

            // If SyncThing is syncing, wait until it is not
            if (isSyncing) {
                sentMessage.edit(new EmbedBuilder()
                        .setTitle("Waiting for sync")
                        .setDescription("SyncThing is currently syncing media in the import folder. This command will continue " +
                                "once the sync has been completed.")
                        .setColor(BotColors.WARNING)
                );

                while (isSyncing) {
                    try {
                        if (stop) {
                            onCancel();
                            return;
                        }

                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        displayError("An unknown error has occurred. Brandan has been notified.", e.getMessage());
                        endTask(e);
                        return;
                    }

                    for (String device : BotConfig.getInstance().syncthingDevices()) {
                        try {
                            isSyncing = BotClient.getInstance()
                                    .syncthingApi.getCompletionStatus(BotConfig.getInstance().syncthingImportFolderId(), device)
                                    .getCompletion() != 100;
                        } catch (ApiException e) {
                            displayError("An unknown error has occurred. Brandan has been notified.", e.getMessage());
                            endTask(e);
                            return;
                        }
                    }
                }
            }
        }

        // Create the lists used to store information about the process
        List<String> fileList = new ArrayList<>();
        List<String> mediaFileList = new ArrayList<>();
        List<String> subtitleFileList = new ArrayList<>();
        List<String> unknownFileList = new ArrayList<>();

        // Set the status of the task
        BotStatusManager.getInstance().setImportManagerStatus("Fetching list of files");

        // Stop if the NFS server is not mounted
        if (!Files.exists(Path.of(BotConfig.getInstance().importFolder() + "mount.pb")) && BotConfig.getInstance().checkMount()) {
            displayError("Unable to proceed with import because the NFS backend server is not mounted. Please inform Brandan of this error.", "nfs-import-mount");
            endTask();
            return;
        } else if (!Files.exists(Path.of(BotConfig.getInstance().tvFolder() + "mount.pb")) && BotConfig.getInstance().checkMount()) {
            displayError("Unable to proceed with import because the NFS backend server is not mounted. Please inform Brandan of this error.", "nfs-tv-mount");
            endTask();
            return;
        } else if (!Files.exists(Path.of(BotConfig.getInstance().movieFolder() + "mount.pb")) && BotConfig.getInstance().checkMount()) {
            displayError("Unable to proceed with import because the NFS backend server is not mounted. Please inform Brandan of this error.", "nfs-movie-mount");
            endTask();
            return;
        }

        // Get a list of all the files in the import folder
        try {
            List<String> rootFiles;
            List<String> movieFiles;
            List<String> episodeFiles;

            rootFiles = Files.list(Paths.get(BotConfig.getInstance().importFolder()))
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());

            movieFiles = Files.list(Paths.get(BotConfig.getInstance().importFolder() + "/movies"))
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());

            episodeFiles = Files.list(Paths.get(BotConfig.getInstance().importFolder() + "/episodes"))
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());

            fileList.addAll(rootFiles);
            movieFiles.forEach(file -> fileList.add("/movies/" + file));
            episodeFiles.forEach(file -> fileList.add("/episodes/" + file));
        } catch (Exception e) {
            displayError("There was an unexpected error. I was unable to get a list of files to import. Brandan has been notified of this issue.", "fetch-file-list");
            endTask(e);
            return;
        }

        // Remove files listed in the ignore list
        fileList.removeIf(file -> {
            boolean remove = false;

            for (String item : BotConfig.getInstance().ignoredImportFiles()) {
                if (file.endsWith(item)) {
                    remove = true;
                    break;
                }
            }

            return remove;
        });

        // Parse the file list into different file types
        fileList.forEach((file) -> {
            if (file.endsWith(".avi") || file.endsWith(".divx") || file.endsWith(".flv") || file.endsWith(".m4v") ||
                    file.endsWith(".mkv") || file.endsWith(".mp4") || file.endsWith(".mpeg") || file.endsWith(".mpg") || file.endsWith(".wmv")) {
                mediaFileList.add(file);
            } else if (file.endsWith(".smi") || file.endsWith(".srt") || file.endsWith(".sub") || file.endsWith(".ssa") || file.endsWith(".ass") || file.endsWith(".vtt")) {
                subtitleFileList.add(file);
            } else {
                unknownFileList.add(file);
            }
        });

        // Send a warning if any of the files in the file list are of an unsupported file type
        if (!unknownFileList.isEmpty()) {
            StringBuilderPlus unknownFiles = new StringBuilderPlus();
            unknownFileList.forEach(unknownFiles::appendLine);
            displayWarning("Unsupported Files Detected!", "There were files with an unsupported file type " +
                    "located in the import folder. These files will be ignored.", unknownFiles.toString());
        }

        // Process the media files listed in the mediaFileList
        int progress = 1;
        int total = mediaFileList.size() + subtitleFileList.size();
        for (String importFile : mediaFileList) {
            String filetype;
            String code;
            int width;
            int height;

            // Check if the import should be canceled
            if (stop) {
                onCancel();
                return;
            }

            // Update the progress message in the bot status
            updateProgressMessages(progress, total);

            // Determine the file type and parse the IMDB code
            String[] filenameParts = importFile.split("\\.");
            code = filenameParts[0];
            filetype = filenameParts[1];

            // Remove the folder name from the code string
            code = code.replace("/episodes/", "");
            code = code.replace("/movies/", "");

            // Fetch the width of the media item
            try {
                width = MediaInfoHelper.getWidth(BotConfig.getInstance().importFolder() + importFile);
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                displayWarning("Unable to get info!", "I was unable to fetch the width of the following " +
                        "media file. Please check to ensure the file is not corrupted and try again. This item has " +
                        "been skipped for now:", importFile);
                reportError(e);
                progress++;
                updateProgressMessages(progress, total);
                continue;
            }

            // Fetch the height of the media item
            try {
                height = MediaInfoHelper.getHeight(BotConfig.getInstance().importFolder() + importFile);
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                displayWarning("Unable to get info!", "I was unable to fetch the height of the following " +
                        "media file. Please check to ensure the file is not corrupted and try again. This item has " +
                        "been skipped for now:", importFile);
                reportError(e);
                progress++;
                updateProgressMessages(progress, total);
                continue;
            }

            // Determine the resolution of the file
            int resolution = 0;
            if (height > 0 && height <= 240) {
                resolution = 240;
            } else if (height > 240 && height <= 360) {
                resolution = 360;
            } else if (height > 360 && height <= 480) {
                resolution = 480;
            } else if (height > 480 && height <= 720) {
                resolution = 720;
            } else if (height > 720 && height <= 1080) {
                resolution = 1080;
            } else if (height > 1080) {
                resolution = 2160;
            }

            // Check the type of media and move it into place based on the type
            if (importFile.startsWith("/episodes/")) {
                EpisodeBaseRecord episodeBaseRecord;
                SeriesBaseRecord seriesBaseRecord;
                boolean exists = DbOperations.episodeOps.exists(code);
                int seasonNum;
                int episodeNum;
                String seasonString;
                String episodeString;
                String showFoldername;
                String seasonFoldername;
                String episodeFilename;

                // Get information about the media item from TVDB
                try {
                    var episodeResponse = BotClient.getInstance().tvdbApi.getEpisode(Long.valueOf(code));
                    var seriesResponse = BotClient.getInstance().tvdbApi.getSeries(episodeResponse.getData().getSeriesId());

                    if (episodeResponse.getStatus().equalsIgnoreCase("success") && seriesResponse.getStatus().equalsIgnoreCase("success")) {
                        episodeBaseRecord = episodeResponse.getData();
                        seriesBaseRecord = seriesResponse.getData();
                    } else {
                        displayWarning("Unable to get info!", "I was unable to fetch information from " +
                                "TheTVDB about the following media file. Please check the ID code used. This item has " +
                                "been skipped for now:", importFile);
                        progress++;
                        updateProgressMessages(progress, total);
                        continue;
                    }
                } catch (ApiException e) {
                    displayWarning("Unable to get info!", "I was unable to fetch information from " +
                            "TheTVDB about the following media file. Please check the ID code used. This item has " +
                            "been skipped for now:", importFile);
                    reportError(e);
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Ensure the name is populated by trying to fetch it from IMDb if TheTVDB does not have it
                try {
                    if (episodeBaseRecord.getName() == null) {
                        var episodeExtendedResponse = BotClient.getInstance().tvdbApi.getEpisodeExtended(episodeBaseRecord.getId());
                        AtomicReference<String> episodeName = new AtomicReference<>();
                        AtomicBoolean failed = new AtomicBoolean(false);

                        episodeExtendedResponse.getData().getRemoteIds().forEach(id -> {
                            if (id.getType() == 2) {
                                try {
                                    var imdbObject = BotClient.getInstance().omdbApi.getById(id.getId());
                                    episodeName.set(imdbObject.getTitle());
                                } catch (ApiException e) {
                                    reportError(e);
                                    failed.set(true);
                                }
                            }
                        });

                        if (failed.get()) {
                            displayWarning("Unable to get info!", "I was unable to fetch information from " +
                                    "TheTVDB about the following media file. Please check the ID code used. This item has " +
                                    "been skipped for now:", importFile);
                            progress++;
                            continue;
                        } else {
                            episodeBaseRecord.setName(episodeName.get());
                        }
                    }
                } catch (ApiException e) {
                    displayWarning("Unable to get info!", "I was unable to fetch information from " +
                            "TheTVDB about the following media file. Please check the ID code used. This item has " +
                            "been skipped for now:", importFile);
                    reportError(e);
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Parse the season and episode number from the information request
                seasonNum = episodeBaseRecord.getSeasonNumber();
                episodeNum = episodeBaseRecord.getNumber();

                // Parse the season and episode information into a string for use in filenames
                if (seasonNum <= 9) {
                    seasonString = "s0" + seasonNum;
                } else seasonString = "s" + seasonNum;

                if (episodeNum <= 9) {
                    episodeString = "e0" + episodeNum;
                } else episodeString = "e" + episodeNum;

                // Configure the file/folder names for the show and episode
                try {
                    showFoldername = FilenameSanitizer.sanitize(Objects.requireNonNull(seriesBaseRecord).getName() + " {tvdb-" + seriesBaseRecord.getId() + "}");
                    seasonFoldername = FilenameSanitizer.sanitize("Season " + seasonNum);
                    if (episodeBaseRecord.getName() == null) {
                        episodeFilename = FilenameSanitizer.sanitize(seriesBaseRecord.getName() + " - " +
                                seasonString + episodeString) + "." + filetype;
                    } else {
                        episodeFilename = FilenameSanitizer.sanitize(seriesBaseRecord.getName() + " - " +
                                seasonString + episodeString + " - " + episodeBaseRecord.getName()) + "." + filetype;
                    }
                } catch (NullPointerException e) {
                    displayWarning("Unable to import file!", "I was unable to import the following file due to an " +
                            "unknown issue with the data returned to the bot from IMDb. Brandan has been notified of this issue, " +
                            "this file has been skipped for now.", importFile);
                    reportError(e);
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Attempt to create the TV shows folder
                if (!createFolder(BotConfig.getInstance().tvFolder() + showFoldername, importFile)) {
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Build the show's information
                Show dbShow = new ShowBuilder()
                        .withTvdbId(seriesBaseRecord.getId().toString())
                        .withName(seriesBaseRecord.getName())
                        .withFoldername(showFoldername)
                        .build();

                // Ensure the TV show is saved to the database
                DbOperations.saveObject(dbShow);

                // Attempt to create the season folder for the episode
                if (!createFolder(BotConfig.getInstance().tvFolder() + showFoldername + "/" + seasonFoldername, importFile)) {
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Ensure the season is saved to the database
                if (!DbOperations.seasonOps.exists(dbShow, seasonNum)) {
                    DbOperations.saveObject(new SeasonBuilder()
                            .withShow(dbShow)
                            .withNumber(seasonNum)
                            .withFoldername(seasonFoldername)
                            .build());
                }
                Season dbSeason = DbOperations.seasonOps.getSeason(dbShow, seasonNum);

                // Check if the media already is in the system and display a message if it is and the overwrite flag was not given
                if (exists && !overwrite) {
                    sentMessage.getChannel().sendMessage(new EmbedBuilder()
                            .setTitle("Episode already exists!")
                            .setDescription("A file you attempted to import already exists in the system. " +
                                    "If you would like to overwrite the existing media file, please rerun this " +
                                    "command with the `--overwrite` option." + "\n```" + importFile + "\n```")
                            .addInlineField("Episode Title:", episodeBaseRecord.getName())
                            .addInlineField("TV Show:", seriesBaseRecord.getName())
                            .addInlineField("TVDB Code:", episodeBaseRecord.getId().toString())
                            .setColor(BotColors.WARNING)
                    );
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Move the media file into place
                if (!moveMedia(importFile, BotConfig.getInstance().tvFolder() + showFoldername + "/" + seasonFoldername + "/" + episodeFilename)) {
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Save the episode to the database
                DbOperations.saveObject(new EpisodeBuilder()
                        .withShow(dbShow)
                        .withSeason(dbSeason)
                        .withTvdbId(code)
                        .withTitle(episodeBaseRecord.getName())
                        .withDate(episodeBaseRecord.getAired())
                        .withFilename(episodeFilename)
                        .withFiletype(filetype)
                        .withNumber(episodeNum)
                        .withWidth(width)
                        .withHeight(height)
                        .withResolution(resolution)
                        .build());

                // Display a message about the episode being added
                int finalSeasonNum = seasonNum;
                int finalEpisodeNum = episodeNum;
                Main.getBotApi().getTextChannelById(BotConfig.getInstance().newEpisodesChannelId()).ifPresent(textChannel ->
                        textChannel.sendMessage(new EmbedBuilder()
                                .setTitle(episodeBaseRecord.getName())
                                .addField("Show:", seriesBaseRecord.getName())
                                .addInlineField("Season:", String.valueOf(finalSeasonNum))
                                .addInlineField("Episode:", String.valueOf(finalEpisodeNum))
                                .setImage(episodeBaseRecord.getImage())
                                .setColor(BotColors.SUCCESS))
                                .exceptionally(ExceptionLogger.get())
                );
            } else {
                OmdbItem omdbItem;

                // Get information about the media item from IMDb
                try {
                    omdbItem = BotClient.getInstance().omdbApi.getById(code);
                } catch (ApiException e) {
                    displayWarning("Unable to get info!", "I was unable to fetch information from " +
                            "IMDB about the following media file. Please check the IMDB code used. This item has " +
                            "been skipped for now:", importFile);
                    reportError(e);
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Ensure that the query was successful otherwise display the proper warning
                if (omdbItem.getResponse() == OmdbItem.ResponseEnum.FALSE) {
                    if (Objects.equals(omdbItem.getError(), "Incorrect IMDb ID.")) {
                        displayWarning("Incorrect Format!", "The following media file in the import directory " +
                                "does not conforming to the proper naming convention, or is using an invalid IMDb ID. Please check " +
                                "the file and try again.", importFile);
                    } else {
                        displayWarning("Unknown error", "There was an unknown error while processing the media file " +
                                "displayed below. Please notify Brandan of the issue.", importFile);
                    }
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                String foldername = FilenameSanitizer.sanitize(omdbItem.getTitle() + " (" + omdbItem.getYear() + ") {imdb-" + omdbItem.getImdbID() + "}");
                String filename = foldername + "." + filetype;
                boolean exists = DbOperations.movieOps.exists(code);

                // Check if the media already is in the system and display a message if it is and the overwrite flag was not given
                if (exists && !overwrite) {
                    sentMessage.getChannel().sendMessage(new EmbedBuilder()
                            .setTitle("Movie already exists!")
                            .setDescription("A file you attempted to import already exists in the system. " +
                                    "If you would like to overwrite the existing media file, please rerun this " +
                                    "command with the `--overwrite` option." + "\n```" + importFile + "\n```")
                            .addInlineField("Movie Title: ", omdbItem.getTitle())
                            .addInlineField("IMDB Code:", omdbItem.getImdbID())
                            .setColor(BotColors.WARNING)
                    );
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Create the movie folder
                if (!createFolder(BotConfig.getInstance().movieFolder() + foldername, importFile)) {
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Move the media file into place
                if (!moveMedia(importFile, BotConfig.getInstance().movieFolder() + foldername + "/" + filename)) {
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Save the movie to the database
                DbOperations.saveObject(new MovieBuilder()
                        .withId(code)
                        .withTitle(omdbItem.getTitle())
                        .withYear(omdbItem.getYear())
                        .withResolution(resolution)
                        .withHeight(height)
                        .withWidth(width)
                        .withFilename(filename)
                        .withExtension(filetype)
                        .withFolderName(foldername)
                        .build());

                // Display a message about the movie being added
                Main.getBotApi().getTextChannelById(BotConfig.getInstance().newMoviesChannelId()).ifPresent(textChannel ->
                        textChannel.sendMessage(new EmbedBuilder()
                                .setTitle(omdbItem.getTitle())
                                .setDescription("**Year:** " + omdbItem.getYear() + "\n" +
                                        "**Director(s):** " + omdbItem.getDirector() + "\n" +
                                        "**Plot:** " + omdbItem.getPlot())
                                .setImage(omdbItem.getPoster())
                                .setColor(BotColors.SUCCESS))
                                .exceptionally(ExceptionLogger.get())
                );
            }

            // Trigger a refresh of the media libraries on the plex server
            try {
                BotClient.getInstance().refreshPlexServers();
            } catch (Exception e) {
                reportError(e);
            }

            progress++;
            updateProgressMessages(progress, total);
        }

        // Process the subtitle files
        for (String importFile : subtitleFileList) {
            String filetype = null;
            String code = null;
            String languageCode = null;
            boolean forced = false;

            // Check if the import should be canceled
            if (stop) {
                onCancel();
                return;
            }

            // Update the progress message in the bot status
            updateProgressMessages(progress, total);

            // Determine the file type and parse the IMDB code and language code
            String[] filenameParts = importFile.split("\\.");

            // Parse the name of the file into its different parts
            if (filenameParts.length == 3) {
                code = filenameParts[0];
                languageCode = filenameParts[1];
                filetype = filenameParts[2];
            } else if (filenameParts.length == 4 && filenameParts[2].equalsIgnoreCase("forced")) {
                code = filenameParts[0];
                languageCode = filenameParts[1];
                filetype = filenameParts[3];
                forced = true;
            } else {
                displayWarning("Unable to parse file!", "I was unable to parse the required information " +
                        "required from the following file. Please check the filename and try again.", importFile);
            }

            // Move file into place based on the media it is for
            if (importFile.startsWith("/episodes/")) {
                EpisodeBaseRecord episodeBaseRecord;
                SeriesBaseRecord seriesBaseRecord;
                String seasonString;
                String episodeString;
                String showFoldername;
                String seasonFoldername;
                String subtitleFilename;

                // Get information about the media item from TVDB
                try {
                    var episodeResponse = BotClient.getInstance().tvdbApi.getEpisode(Long.valueOf(Objects.requireNonNull(code)));
                    var seriesResponse = BotClient.getInstance().tvdbApi.getSeries(episodeResponse.getData().getSeriesId());

                    if (episodeResponse.getStatus().equalsIgnoreCase("success") && seriesResponse.getStatus().equalsIgnoreCase("success")) {
                        episodeBaseRecord = episodeResponse.getData();
                        seriesBaseRecord = seriesResponse.getData();
                    } else {
                        displayWarning("Unable to get info!", "I was unable to fetch information from " +
                                "TheTVDB about the following media file. Please check the ID code used. This item has " +
                                "been skipped for now:", importFile);
                        progress++;
                        updateProgressMessages(progress, total);
                        continue;
                    }
                } catch (ApiException e) {
                    displayWarning("Unable to get info!", "I was unable to fetch information from " +
                            "TheTVDB about the following media file. Please check the ID code used. This item has " +
                            "been skipped for now:", importFile);
                    reportError(e);
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Ensure the name is populated by trying to fetch it from IMDb if TheTVDB does not have it
                try {
                    if (episodeBaseRecord.getName() == null) {
                        var episodeExtendedResponse = BotClient.getInstance().tvdbApi.getEpisodeExtended(episodeBaseRecord.getId());
                        AtomicReference<String> episodeName = new AtomicReference<>();
                        AtomicBoolean failed = new AtomicBoolean(false);

                        episodeExtendedResponse.getData().getRemoteIds().forEach(id -> {
                            if (id.getType() == 2) {
                                try {
                                    var imdbObject = BotClient.getInstance().omdbApi.getById(id.getId());
                                    episodeName.set(imdbObject.getTitle());
                                } catch (ApiException e) {
                                    reportError(e);
                                    failed.set(true);
                                }
                            }
                        });

                        if (failed.get()) {
                            displayWarning("Unable to get info!", "I was unable to fetch information from " +
                                    "TheTVDB about the following media file. Please check the ID code used. This item has " +
                                    "been skipped for now:", importFile);
                            progress++;
                            updateProgressMessages(progress, total);
                            continue;
                        } else {
                            episodeBaseRecord.setName(episodeName.get());
                        }
                    }
                } catch (ApiException e) {
                    displayWarning("Unable to get info!", "I was unable to fetch information from " +
                            "TheTVDB about the following media file. Please check the ID code used. This item has " +
                            "been skipped for now:", importFile);
                    reportError(e);
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Parse the season and episode number from the information request
                int seasonNum = episodeBaseRecord.getSeasonNumber();
                int episodeNum = episodeBaseRecord.getNumber();

                // Check if there is a movie or episode corresponding to this subtitle file, display a warning if there is not
                if (!DbOperations.episodeOps.exists(code)) {
                    sentMessage.getChannel().sendMessage(new EmbedBuilder()
                            .setTitle("No matching episode found!")
                            .setDescription("You attempted to import a subtitle for a episode that does not " +
                                    "exist in the system. Please import the episode file in order to import this subtitle." +
                                    "\n```" + importFile + "\n```")
                            .addInlineField("Episode Title:", episodeBaseRecord.getName())
                            .addInlineField("TV Show:", Objects.requireNonNull(seriesBaseRecord).getName())
                            .addInlineField("TVDB Code:", episodeBaseRecord.getId().toString())
                            .setColor(BotColors.WARNING)
                    );
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Parse the season and episode information into a string for use in filenames
                if (seasonNum <= 9) {
                    seasonString = "s0" + seasonNum;
                } else seasonString = "s" + seasonNum;

                if (episodeNum <= 9) {
                    episodeString = "e0" + episodeNum;
                } else episodeString = "e" + episodeNum;


                // Configure the file/folder names for the show and episode
                try {
                    showFoldername = FilenameSanitizer.sanitize(Objects.requireNonNull(seriesBaseRecord).getName() + " {tvdb-" + seriesBaseRecord.getId() + "}");
                    seasonFoldername = FilenameSanitizer.sanitize("Season " + seasonNum);
                    if (episodeBaseRecord.getName() == null) {
                        subtitleFilename = FilenameSanitizer.sanitize(seriesBaseRecord.getName() + " - " +
                                seasonString + episodeString) + "." + languageCode + (forced ? ".forced." : ".") + filetype;
                    } else {
                        subtitleFilename = FilenameSanitizer.sanitize(seriesBaseRecord.getName() + " - " +
                                seasonString + episodeString + " - " + episodeBaseRecord.getName())
                                + "." + languageCode + (forced ? ".forced." : ".") + filetype;
                    }
                } catch (NullPointerException e) {
                    displayWarning("Unable to import file!", "I was unable to import the following file due to an " +
                            "unknown issue with the data returned to the bot from TheTVDB. Brandan has been notified of this issue, " +
                            "this file has been skipped for now.", importFile);
                    reportError(e);
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Check if the media already is in the system and display a message if it is and the overwrite flag was not given
                boolean exists = DbOperations.episodeSubtitleOps.exists(subtitleFilename);
                if (exists && !overwrite) {
                    sentMessage.getChannel().sendMessage(new EmbedBuilder()
                            .setTitle("Subtitle already exists!")
                            .setDescription("A file you attempted to import already exists in the system. " +
                                    "If you would like to overwrite the existing media file, please rerun this " +
                                    "command with the `--overwrite` option." + "\n```" + importFile + "\n```")
                            .addInlineField("Language Code:", languageCode)
                            .addInlineField("Episode Title:", episodeBaseRecord.getName())
                            .addInlineField("TV Show:", Objects.requireNonNull(seriesBaseRecord).getName())
                            .addInlineField("TVDB Code:", episodeBaseRecord.getId().toString())
                            .setColor(BotColors.WARNING)
                    );
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Move the media file into place
                if (!moveMedia(importFile, BotConfig.getInstance().tvFolder() + showFoldername + "/" + seasonFoldername + "/" + subtitleFilename)) {
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Save the subtitle to the database
                Episode dbEpisode = DbOperations.episodeOps.getItemById(code);
                DbOperations.saveObject(new EpisodeSubtitleBuilder()
                        .withEpisode(dbEpisode)
                        .withLanguageCode(languageCode)
                        .withFiletype(filetype)
                        .withForced(forced)
                        .withFilename(subtitleFilename)
                        .build()
                );
            } else {
                OmdbItem omdbItem;

                // Fetch information about the media item
                try {
                    omdbItem = BotClient.getInstance().omdbApi.getById(code);
                } catch (ApiException e) {
                    displayWarning("Unable to get info!", "I was unable to fetch information from " +
                            "IMDB about the following media file. Please check the IMDB code used. This item has " +
                            "been skipped for now:", importFile);
                    reportError(e);
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                String foldername = FilenameSanitizer.sanitize(omdbItem.getTitle() + " (" + omdbItem.getYear() + ") {imdb-" + omdbItem.getImdbID() + "}");
                String filename = FilenameSanitizer.sanitize(foldername) + "." + languageCode + (forced ? ".forced." : ".") + filetype;
                boolean exists = DbOperations.movieSubtitleOps.exists(filename);

                // Check if there is a movie or episode corresponding to this subtitle file, display a warning if there is not
                if (!DbOperations.movieOps.exists(code)) {
                    sentMessage.getChannel().sendMessage(new EmbedBuilder()
                            .setTitle("No matching movie found!")
                            .setDescription("You attempted to import a subtitle for a movie that does not " +
                                    "exist in the system. Please import the movie file in order to import this subtitle." +
                                    "\n```" + importFile + "\n```")
                            .addInlineField("Movie Title: ", omdbItem.getTitle())
                            .addInlineField("IMDB Code:", omdbItem.getImdbID())
                            .setColor(BotColors.WARNING)
                    );
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Check if the media already is in the system and display a message if it is and the overwrite flag was not given
                if (exists && !overwrite) {
                    sentMessage.getChannel().sendMessage(new EmbedBuilder()
                            .setTitle("Subtitle already exists!")
                            .setDescription("A file you attempted to import already exists in the system. " +
                                    "If you would like to overwrite the existing media file, please rerun this " +
                                    "command with the `--overwrite` option." + "\n```" + importFile + "\n```")
                            .addInlineField("Language Code:", languageCode)
                            .addInlineField("Movie Title: ", omdbItem.getTitle())
                            .addInlineField("IMDB Code:", omdbItem.getImdbID())
                            .setColor(BotColors.WARNING)
                    );
                    displayWarning("Subtitle already exists", "A subtitle you attempted to import already exists in the system. " +
                            "If you would like to overwrite the existing media file, please rerun this command with the `--overwrite` option.", importFile);
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Create the movie folder
                if (!createFolder(BotConfig.getInstance().movieFolder() + foldername, importFile)) {
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Move the media file into place
                if (!moveMedia(importFile, BotConfig.getInstance().movieFolder() + foldername + "/" + filename)) {
                    progress++;
                    updateProgressMessages(progress, total);
                    continue;
                }

                // Register the subtitle to the database
                Movie dbMovie = DbOperations.movieOps.getMovieById(code);
                DbOperations.saveObject(new MovieSubtitleBuilder()
                        .withMovie(dbMovie)
                        .withFilename(filename)
                        .withFiletype(filetype)
                        .withLanguageCode(languageCode)
                        .withForced(forced)
                        .build()
                );
            }

            // Trigger a refresh of the media libraries on the plex server
            try {
                BotClient.getInstance().refreshPlexServers();
            } catch (Exception e) {
                reportError(e);
            }

            progress++;
            updateProgressMessages(progress, total);
        }

        sentMessage.edit(new EmbedBuilder()
                .setTitle("Import Completed")
                .setDescription("The import process was completed and processed " + total + " files.")
                .setColor(BotColors.SUCCESS)
        );
        endTask();
    }
}