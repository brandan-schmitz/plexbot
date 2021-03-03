package net.celestialdata.plexbot.managers;

import net.celestialdata.plexbot.BotConfig;
import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.client.ApiException;
import net.celestialdata.plexbot.client.BotClient;
import net.celestialdata.plexbot.client.model.OmdbItem;
import net.celestialdata.plexbot.database.DbOperations;
import net.celestialdata.plexbot.database.builders.*;
import net.celestialdata.plexbot.database.models.Episode;
import net.celestialdata.plexbot.database.models.Movie;
import net.celestialdata.plexbot.database.models.Season;
import net.celestialdata.plexbot.database.models.Show;
import net.celestialdata.plexbot.utils.*;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ImportManager implements CustomRunnable {
    private final Message sentMessage;
    private final boolean overwrite;

    public ImportManager(Message sentMessage, boolean overwrite) {
        this.sentMessage = sentMessage;
        this.overwrite = overwrite;
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
        BotStatusManager.getInstance().removeProcess(taskName());
        BotStatusManager.getInstance().clearImportManagerStatus();
    }

    @Override
    public void endTask(Throwable error) {
        reportError(error);
        BotStatusManager.getInstance().removeProcess(taskName());
        BotStatusManager.getInstance().clearImportManagerStatus();
    }

    /**
     * Send a message to the channel a import request command was sent with a warning about the import process.
     *
     * @param title warning title
     * @param message warning message
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

    /**
     * Move a file from one location to another. Destination folder should already exist prior to moving the file.
     *
     * @param file original file to move
     * @param destination destination folder and filename
     * @return true if moved successfully, false it move failed
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean moveMedia(String file, String destination) {
        boolean success = true;

        try {
            if (overwrite) {
                Files.move(
                        Paths.get(BotConfig.getInstance().importFolder() + file),
                        Paths.get(destination),
                        StandardCopyOption.REPLACE_EXISTING
                );
            } else {
                Files.move(
                        Paths.get(BotConfig.getInstance().importFolder() + file),
                        Paths.get(destination)
                );
            }
        } catch (IOException e) {
            sentMessage.edit(new EmbedBuilder()
                    .addField("An error has occurred:", "```An unknown error occurred while processing the " + file +
                            " file. Brandan has been notified of this error.```")
                    .setColor(BotColors.ERROR)
            );
            reportError(e);
            success = false;
        }

        return success;
    }

    /**
     * Create a folder on the filesystem. Folder path should be a complete path.
     *
     * @param folderPath path to new folder
     * @param file file this operation is for (used in reporting errors)
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
                    .setColor(BotColors.ERROR)
            );
            endTask(e);
        });

        // Create the lists used to store information about the process
        List<String> fileList;
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
            fileList = Files.list(Paths.get(BotConfig.getInstance().importFolder()))
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            displayError("There was an unexpected error. I was unable to get a list of files to import. Brandan has been notified of this issue.", "fetch-file-list");
            endTask(e);
            return;
        }

        // Remove files listed in the ignore list
        fileList.removeIf(file -> BotConfig.getInstance().ignoredImportFiles().contains(file));

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
            OmdbItem omdbItem;
            String filetype;
            String code;

            // Update the progress message in the bot status
            BotStatusManager.getInstance().setImportManagerStatus(progress, total);

            // Determine the file type and parse the IMDB code
            String[] filenameParts = importFile.split("\\.");
            code = filenameParts[0];
            filetype = filenameParts[1];

            // Fetch information about the media item
            int width = MediaInfoHelper.getWidth(BotConfig.getInstance().importFolder() + importFile);
            int height = MediaInfoHelper.getHeight(BotConfig.getInstance().importFolder() + importFile);
            try {
                omdbItem = BotClient.getInstance().omdbApi.getById(code);
            } catch (ApiException e) {
                displayWarning("Unable to get info!", "I was unable to fetch information from " +
                        "IMDB about the following media file. Please check the IMDB code used. This item has " +
                        "been skipped for now:", importFile);
                reportError(e);
                progress++;
                continue;
            }

            // Check the type of media and move it into place based on the type
            if (omdbItem.getType() == OmdbItem.TypeEnum.MOVIE) {
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
                    continue;
                }

                // Create the movie folder
                if (!createFolder(BotConfig.getInstance().movieFolder() + foldername, importFile)) {
                    progress++;
                    continue;
                }

                // Move the media file into place
                if (!moveMedia(importFile, BotConfig.getInstance().movieFolder() + foldername + "/" + filename)) {
                    progress++;
                    continue;
                }

                // TODO: Calculate the resolution in the form used by TYS
                // Save the movie to the database
                DbOperations.saveObject(new MovieBuilder()
                        .withId(code)
                        .withTitle(omdbItem.getTitle())
                        .withYear(omdbItem.getYear())
                        .withResolution(0)
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
            } else if (omdbItem.getType() == OmdbItem.TypeEnum.EPISODE) {
                boolean exists = DbOperations.episodeOps.exists(code);
                int seasonNum = Integer.parseInt(Objects.requireNonNull(omdbItem.getSeason()));
                int episodeNum = Integer.parseInt(Objects.requireNonNull(omdbItem.getEpisode()));
                String seasonString;
                String episodeString;
                String showFoldername;
                String seasonFoldername;
                String episodeFilename;

                // Fetch information about the show the episode is a part of
                OmdbItem show;
                try {
                    show = BotClient.getInstance().omdbApi.getById(omdbItem.getSeriesID());
                } catch (ApiException e) {
                    displayWarning("Unable to get info!", "I was unable to fetch information from " +
                            "IMDB about the following media file. Please check the IMDB code used. This item has " +
                            "been skipped for now:", importFile);
                    reportError(e);
                    progress++;
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
                showFoldername = FilenameSanitizer.sanitize(show.getTitle());
                seasonFoldername = FilenameSanitizer.sanitize("Season " + seasonNum);
                episodeFilename = FilenameSanitizer.sanitize(show.getTitle() + " - " +
                        seasonString + episodeString + " - " + omdbItem.getTitle()) + "." + filetype;

                // Attempt to create the TV shows folder
                if (!createFolder(BotConfig.getInstance().tvFolder() + showFoldername, importFile)) {
                    progress++;
                    continue;
                }

                // Build the show's information
                Show dbShow = new ShowBuilder()
                        .withImdbCode(show.getImdbID())
                        .withName(show.getTitle())
                        .withFoldername(showFoldername)
                        .build();

                // Ensure the TV show is saved to the database
                DbOperations.saveObject(dbShow);

                // Attempt to create the season folder for the episode
                if (!createFolder(BotConfig.getInstance().tvFolder() + showFoldername + "/" + seasonFoldername, importFile)) {
                    progress++;
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
                            .addInlineField("Episode Title:", omdbItem.getTitle())
                            .addInlineField("TV Show:", show.getTitle())
                            .addInlineField("IMDB Code:", omdbItem.getImdbID())
                            .setColor(BotColors.WARNING)
                    );
                    progress++;
                    continue;
                }

                // Move the media file into place
                if (!moveMedia(importFile, BotConfig.getInstance().tvFolder() + showFoldername + "/" + seasonFoldername + "/" + episodeFilename)) {
                    progress++;
                    continue;
                }

                // Save the episode to the database
                DbOperations.saveObject(new EpisodeBuilder()
                        .withShow(dbShow)
                        .withSeason(dbSeason)
                        .withImdbCode(code)
                        .withYear(omdbItem.getYear())
                        .withFilename(episodeFilename)
                        .withFiletype(filetype)
                        .withNumber(episodeNum)
                        .withWidth(width)
                        .withHeight(height)
                        .build());

                // Display a message about the episode being added
                Main.getBotApi().getTextChannelById(BotConfig.getInstance().newEpisodesChannelId()).ifPresent(textChannel ->
                        textChannel.sendMessage(new EmbedBuilder()
                                .setTitle(omdbItem.getTitle())
                                .addField("Show:", show.getTitle())
                                .addInlineField("Season:", String.valueOf(seasonNum))
                                .addInlineField("Episode:", String.valueOf(episodeNum))
                                .addField("Director(s):", omdbItem.getDirector())
                                .addField("Plot:", omdbItem.getPlot())
                                .setImage(omdbItem.getPoster())
                                .setColor(BotColors.SUCCESS))
                                .exceptionally(ExceptionLogger.get())
                );
            }

            progress++;
        }

        // Process the subtitle files
        for (String importFile : subtitleFileList) {
            String filetype = null;
            String code = null;
            String languageCode = null;
            boolean forced = false;
            OmdbItem omdbItem;

            // Update the progress message in the bot status
            BotStatusManager.getInstance().setImportManagerStatus(progress, total);

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

            // Fetch information about the media item
            try {
                omdbItem = BotClient.getInstance().omdbApi.getById(code);
            } catch (ApiException e) {
                displayWarning("Unable to get info!", "I was unable to fetch information from " +
                        "IMDB about the following media file. Please check the IMDB code used. This item has " +
                        "been skipped for now:", importFile);
                reportError(e);
                progress++;
                continue;
            }

            // Move file into place based on the media it is for
            if (omdbItem.getType() == OmdbItem.TypeEnum.MOVIE) {
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
                    continue;
                }

                // Create the movie folder
                if (!createFolder(BotConfig.getInstance().movieFolder() + foldername, importFile)) {
                    progress++;
                    continue;
                }

                // Move the media file into place
                if (!moveMedia(importFile, BotConfig.getInstance().movieFolder() + foldername + "/" + filename)) {
                    progress++;
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
            } else if (omdbItem.getType() == OmdbItem.TypeEnum.EPISODE) {
                int seasonNum = Integer.parseInt(Objects.requireNonNull(omdbItem.getSeason()));
                int episodeNum = Integer.parseInt(Objects.requireNonNull(omdbItem.getEpisode()));
                String seasonString;
                String episodeString;
                String showFoldername;
                String seasonFoldername;
                String subtitleFilename;

                // Fetch information about the show the episode is a part of
                OmdbItem show;
                try {
                    show = BotClient.getInstance().omdbApi.getById(omdbItem.getSeriesID());
                } catch (ApiException e) {
                    displayWarning("Unable to get info!", "I was unable to fetch information from " +
                            "IMDB about the following media file. Please check the IMDB code used. This item has " +
                            "been skipped for now:", importFile);
                    reportError(e);
                    progress++;
                    continue;
                }

                // Check if there is a movie or episode corresponding to this subtitle file, display a warning if there is not
                if (!DbOperations.episodeOps.exists(code)) {
                    sentMessage.getChannel().sendMessage(new EmbedBuilder()
                            .setTitle("No matching episode found!")
                            .setDescription("You attempted to import a subtitle for a episode that does not " +
                                    "exist in the system. Please import the episode file in order to import this subtitle." +
                                    "\n```" + importFile + "\n```")
                            .addInlineField("Episode Title:", omdbItem.getTitle())
                            .addInlineField("TV Show:", show.getTitle())
                            .addInlineField("IMDB Code:", omdbItem.getImdbID())
                            .setColor(BotColors.WARNING)
                    );
                    progress++;
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
                showFoldername = FilenameSanitizer.sanitize(show.getTitle());
                seasonFoldername = FilenameSanitizer.sanitize("Season " + seasonNum);
                subtitleFilename = FilenameSanitizer.sanitize(show.getTitle() + " - " +
                        seasonString + episodeString + " - " + omdbItem.getTitle()) + "." + languageCode + (forced ? ".forced." : ".") + filetype;

                // Check if the media already is in the system and display a message if it is and the overwrite flag was not given
                boolean exists = DbOperations.episodeSubtitleOps.exists(subtitleFilename);
                if (exists && !overwrite) {
                    sentMessage.getChannel().sendMessage(new EmbedBuilder()
                            .setTitle("Subtitle already exists!")
                            .setDescription("A file you attempted to import already exists in the system. " +
                                    "If you would like to overwrite the existing media file, please rerun this " +
                                    "command with the `--overwrite` option." + "\n```" + importFile + "\n```")
                            .addInlineField("Language Code:", languageCode)
                            .addInlineField("Episode Title:", omdbItem.getTitle())
                            .addInlineField("TV Show:", show.getTitle())
                            .addInlineField("IMDB Code:", omdbItem.getImdbID())
                            .setColor(BotColors.WARNING)
                    );
                    progress++;
                    continue;
                }

                // Move the media file into place
                if (!moveMedia(importFile, BotConfig.getInstance().tvFolder() + showFoldername + "/" + seasonFoldername + "/" + subtitleFilename)) {
                    progress++;
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
            }

            progress++;
        }

        sentMessage.edit(new EmbedBuilder()
                .setTitle("Import Completed")
                .setDescription("The import process was completed and processed " + total + " files.")
                .setColor(BotColors.SUCCESS)
        );
        endTask();
    }
}