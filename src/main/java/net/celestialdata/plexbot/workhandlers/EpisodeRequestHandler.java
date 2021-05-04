package net.celestialdata.plexbot.workhandlers;

import net.celestialdata.plexbot.client.ApiException;
import net.celestialdata.plexbot.client.BotClient;
import net.celestialdata.plexbot.client.model.OmdbItem;
import net.celestialdata.plexbot.database.DbOperations;
import net.celestialdata.plexbot.managers.BotStatusManager;
import net.celestialdata.plexbot.managers.DownloadManager;
import net.celestialdata.plexbot.utils.BotColors;
import net.celestialdata.plexbot.utils.BotEmojis;
import net.celestialdata.plexbot.utils.CustomRunnable;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;


/**
 * Provides a thread with all the worker logic for selecting,
 * searching for and downloading a tv episode.
 *
 * @author Celestialdeath99
 */
@SuppressWarnings("DuplicatedCode")
public class EpisodeRequestHandler implements CustomRunnable {
    private final String processName;
    private final String searchId;
    private final Message sentMessage;
    private final long userId;
    private final Object rdbLock = new Object();

    public EpisodeRequestHandler(String processName, String searchId, Message sentMessage, long userId) {
        this.processName = processName;
        this.searchId = searchId;
        this.sentMessage = sentMessage;
        this.userId = userId;
    }

    @Override
    public String taskName() {
        return processName;
    }

    @Override
    public void endTask(Throwable e) {
        BotStatusManager.getInstance().removeProcess(taskName());
        reportError(e);
        displayError("An unknown error has occurred while processing this request. Brandan has been notified of the error.", e.getMessage());
    }

    @Override
    public void run() {
        // Configure task to run the endTask method if there was an error
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> endTask(e));

        MediaSelectionHandler selectionHandler;
        OmdbItem selectedEpisode;
        TorrentHandler torrentHandler;
        DownloadManager downloadManager;
        String magnetLink;
        String downloadLink = null;
        OmdbItem series;

        // Search for the movie
        List<OmdbItem> resultList = new ArrayList<>();
        if (!searchId.isEmpty()) {
            try {
                var result = BotClient.getInstance().omdbApi.getById(searchId);
                if (result.getResponse() == OmdbItem.ResponseEnum.TRUE && result.getType() == OmdbItem.TypeEnum.EPISODE) {
                    resultList.add(result);
                } else if (result.getResponse() == OmdbItem.ResponseEnum.TRUE && result.getType() == OmdbItem.TypeEnum.SERIES) {
                    displayError("You entered an IMDB for an entire TV show. Please enter one for an individual episode. ");
                    endTask();
                    return;
                } else if (result.getResponse() == OmdbItem.ResponseEnum.TRUE && result.getType() == OmdbItem.TypeEnum.MOVIE) {
                    displayError("You entered an IMDB for an movie. Please enter one for an individual episode. ");
                    endTask();
                    return;
                } else {
                    displayError("Unrecognized IMDB Code, please check your code and try again.");
                    endTask();
                    return;
                }
            } catch (Exception e) {
                displayError(e.getMessage(), "omdb-lookup-id");
                endTask(e);
                return;
            }
        } else {
            displayError("You must supply an IMDB ID for a episode.");
            endTask();
            return;
        }

        if (resultList.isEmpty()) {
            displayError("No episodes found. Please adjust your search parameters and try again.");
            endTask();
            return;
        }

        // Create the selection handler
        selectionHandler = new MediaSelectionHandler(resultList, sentMessage);

        // Wait for a episode to be selected in the selection handler
        synchronized (selectionHandler.lock) {
            while (!selectionHandler.getBeenSet()) {
                try {
                    selectionHandler.lock.wait();
                } catch (InterruptedException e) {
                    displayError(e.getMessage(), "unknown-error");
                    endTask(e);
                    return;
                }
            }
        }

        // If the user canceled the movie selection, cancel this process
        if (selectionHandler.getWasCanceled()) {
            endTask();
            return;
        }

        // Attempt to set the selected movie
        try {
            selectedEpisode = selectionHandler.getSelectedMedia();
        } catch (NullPointerException e) {
            displayError("Unable to get the selected episode.", "episode-select-error");
            endTask(e);
            return;
        }

        // Verify the movie requested does not already exist on the server
        if (DbOperations.episodeOps.exists(selectedEpisode.getImdbID())) {
            displayError("This episode is already on Plex.");
            endTask();
            return;
        }

        // Get information about the series
        try {
            series = BotClient.getInstance().omdbApi.getById(selectedEpisode.getSeriesID());
        } catch (ApiException e) {
            displayError("Unable to fetch information about this episode.", "series-fetch-error");
            endTask(e);
            return;
        }


        // Display the initial status message
        sentMessage.edit(new EmbedBuilder()
                .setTitle("Addition Status")
                .setDescription("The following episode is being added:")
                .addInlineField("Show Title:", series.getTitle())
                .addInlineField("Episode Title:", selectedEpisode.getTitle())
                .addInlineField("Season #: ", selectedEpisode.getEpisode())
                .addInlineField("Episode #:", selectedEpisode.getSeason())
                .addField("Progress:",
                        BotEmojis.FINISHED_STEP + "  User selects episode from <https://www.imdb.com>\n" +
                                BotEmojis.TODO_STEP + "  **Locate episode file**\n" +
                                BotEmojis.TODO_STEP + "  Mask download file\n" +
                                BotEmojis.TODO_STEP + "  Download episode\n" +
                                BotEmojis.TODO_STEP + "  Add episode to database")
                .setFooter("Message updated: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .format(ZonedDateTime.now()) + " CST")
                .setColor(BotColors.INFO))
                .exceptionally(ExceptionLogger.get());

        // TODO: Search on EZTV through the API
        // TODO: Scrape the EZTV website to get additional info
        // TODO: Select the proper file if available
        // TODO: Add episode to the waiting lit if not available

        // Get the magnet link
        // TODO: Get the magnet link

        /*
                sentMessage.edit(new EmbedBuilder()
                .setTitle("Addition Status")
                .setDescription("The movie **" + selectedEpisode.getTitle() + "** is being added.")
                .addField("Progress:",
                        BotEmojis.FINISHED_STEP + "  User selects movie from <https://www.imdb.com>\n" +
                                BotEmojis.FINISHED_STEP + "  Locate movie file\n" +
                                BotEmojis.TODO_STEP + "  **Mask download file**\n" +
                                BotEmojis.TODO_STEP + "  Download movie\n" +
                                BotEmojis.TODO_STEP + "  Add movie to database")
                .setFooter("Message updated: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .format(ZonedDateTime.now()) + " CST")
                .setColor(BotColors.INFO))
                .exceptionally(ExceptionLogger.get());

        // Add the magnet link to real-debrid
        RdbMagnetLink rdbMagnetLink;
        try {
            rdbMagnetLink = BotClient.getInstance().rdbApi.addMagnet(magnetLink);
        } catch (Exception e) {
            displayError("There was an error masking the download. Please try again later.", "rdb-add-fail");
            endTask(e);
            return;
        }

        // Get the torrent file information
        RdbTorrentInfo rdbTorrentInfo;
        try {
            rdbTorrentInfo = BotClient.getInstance().rdbApi.getTorrentInfo(rdbMagnetLink.getId());
        } catch (Exception e) {
            displayError("There was an error masking the download. Please try again later.", "rdb-get-info");
            endTask(e);
            return;
        }

        // Select the proper movie files to download
        String fileToSelect = "";
        String fileExtension = "";
        if (rdbTorrentInfo.getFiles() != null) {
            for (RdbTorrentFile file : rdbTorrentInfo.getFiles()) {
                if (file.getPath() != null) {
                    if (file.getPath().contains(".mp4") || file.getPath().contains(".MP4")) {
                        fileToSelect = String.valueOf(file.getId());
                        fileExtension = ".mp4";
                    } else if (file.getPath().contains(".mkv") || file.getPath().contains(".MKV")) {
                        fileToSelect = String.valueOf(file.getId());
                        fileExtension = ".mkv";
                    }
                } else {
                    displayError("There was an error masking the download. Please try again later.", "rdb-list-files");
                    endTask();
                    return;
                }
            }
        } else {
            displayError("There was an error masking the download. Please try again later.", "rdb-list-files");
            endTask();
            return;
        }

        try {
            BotClient.getInstance().rdbApi.selectTorrentFiles(rdbTorrentInfo.getId(), fileToSelect);
        } catch (Exception e) {
            displayError("There was an error masking the download. Please try again later.", "rdb-select-files");
            endTask(e);
            return;
        }

        // Wait for RealDebrid to download the movie file
        try {
            rdbTorrentInfo = BotClient.getInstance().rdbApi.getTorrentInfo(rdbMagnetLink.getId());

            synchronized (rdbLock) {
                while (rdbTorrentInfo.getStatus() == RdbTorrentInfo.StatusEnum.WAITING_FILES_SELECTION ||
                        rdbTorrentInfo.getStatus() == RdbTorrentInfo.StatusEnum.QUEUED) {
                    rdbLock.wait(2000);
                    rdbTorrentInfo = BotClient.getInstance().rdbApi.getTorrentInfo(rdbMagnetLink.getId());
                }

                while (rdbTorrentInfo.getStatus() == RdbTorrentInfo.StatusEnum.DOWNLOADING) {
                    sentMessage.edit(new EmbedBuilder()
                            .setTitle("Addition Status")
                            .setDescription("The movie **" + selectedEpisode.getTitle() + "** is being added.")
                            .addField("Progress:",
                                    BotEmojis.FINISHED_STEP + "  User selects movie from <https://www.imdb.com>\n" +
                                            BotEmojis.FINISHED_STEP + "  Locate movie file\n" +
                                            BotEmojis.TODO_STEP + "  **Mask download file:** " + rdbTorrentInfo.getProgress() + "%\n" +
                                            BotEmojis.TODO_STEP + "  Download movie\n" +
                                            BotEmojis.TODO_STEP + "  Add movie to database")
                            .setFooter("Message updated: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                                    .format(ZonedDateTime.now()) + " CST")
                            .setColor(BotColors.INFO))
                            .exceptionally(ExceptionLogger.get());

                    rdbLock.wait(10000);
                    rdbTorrentInfo = BotClient.getInstance().rdbApi.getTorrentInfo(rdbMagnetLink.getId());
                }

                while (rdbTorrentInfo.getStatus() != RdbTorrentInfo.StatusEnum.DOWNLOADED) {
                    sentMessage.edit(new EmbedBuilder()
                            .setTitle("Addition Status")
                            .setDescription("The movie **" + selectedEpisode.getTitle() + "** is being added.")
                            .addField("Progress:",
                                    BotEmojis.FINISHED_STEP + "  User selects movie from <https://www.imdb.com>\n" +
                                            BotEmojis.FINISHED_STEP + "  Locate movie file\n" +
                                            BotEmojis.TODO_STEP + "  **Mask download file:** Processing...\n" +
                                            BotEmojis.TODO_STEP + "  Download movie\n" +
                                            BotEmojis.TODO_STEP + "  Add movie to database")
                            .setFooter("Message updated: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                                    .format(ZonedDateTime.now()) + " CST")
                            .setColor(BotColors.INFO))
                            .exceptionally(ExceptionLogger.get());

                    rdbLock.wait(5000);
                    rdbTorrentInfo = BotClient.getInstance().rdbApi.getTorrentInfo(rdbMagnetLink.getId());

                    if (rdbTorrentInfo.getStatus() == RdbTorrentInfo.StatusEnum.VIRUS ||
                            rdbTorrentInfo.getStatus() == RdbTorrentInfo.StatusEnum.ERROR ||
                            rdbTorrentInfo.getStatus() == RdbTorrentInfo.StatusEnum.DEAD) {
                        displayError("There was an error masking the download. Please try again later.", "rdb-download-file");
                        endTask();
                        return;
                    }
                }
            }
        } catch (InterruptedException | ApiException e) {
            displayError("There was an error masking the download. Please try again later.", "rdb-download-file");
            endTask(e);
            return;
        }

        // Make the link unrestricted
        RdbUnrestrictedLink unrestrictedLink;
        try {
            if (rdbTorrentInfo.getLinks() != null) {
                unrestrictedLink = BotClient.getInstance().rdbApi.unrestrictLink(rdbTorrentInfo.getLinks().get(0));
            } else {
                displayError("There was an error masking the download. Please try again later.", "rdb-load-link");
                try {
                    BotClient.getInstance().rdbApi.deleteTorrent(rdbTorrentInfo.getId());
                } catch (ApiException ignored) {}
                endTask();
                return;
            }
        } catch (Exception e) {
            displayError("There was an error masking the download. Please try again later.", "rdb-unrestrict-link");
            try {
                BotClient.getInstance().rdbApi.deleteTorrent(rdbTorrentInfo.getId());
            } catch (ApiException e1) {
                reportError(e1);
            }
            endTask(e);
            return;
        }

        // Get the download link
        if (unrestrictedLink.getDownload() != null) {
            downloadLink = unrestrictedLink.getDownload().toString();
        } else {
            displayError("There was an error masking the download. Please try again later.", "rdb-download-link");
        }
        sentMessage.edit(new EmbedBuilder()
                .setTitle("Addition Status")
                .setDescription("The movie **" + selectedEpisode.getTitle() + "** is being added.")
                .addField("Progress:",
                        BotEmojis.FINISHED_STEP + "  User selects movie from <https://www.imdb.com>\n" +
                                BotEmojis.FINISHED_STEP + "  Locate movie file\n" +
                                BotEmojis.FINISHED_STEP + "  Mask download file\n" +
                                BotEmojis.TODO_STEP + "  **Download movie:** Download Queued\n" +
                                BotEmojis.TODO_STEP + "  Add movie to database")
                .setFooter("Message updated: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .format(ZonedDateTime.now()) + " CST")
                .setColor(BotColors.INFO))
                .exceptionally(ExceptionLogger.get());

        // Download File
        downloadManager = new DownloadManager(downloadLink, selectedEpisode, fileExtension);
        BotWorkPool.getInstance().submitProcess(downloadManager);

        // Wait for the file to be downloaded
        synchronized (downloadManager.lock) {
            LocalDateTime lastUpdated = LocalDateTime.now();
            while (downloadManager.isDownloading() && !downloadManager.didUnknownErrorOccur()) {
                try {
                    downloadManager.lock.wait(10000);

                    // Update the download progress message
                    if (lastUpdated.plus(10, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        sentMessage.edit(new EmbedBuilder()
                                .setTitle("Addition Status")
                                .setDescription("The movie **" + selectedEpisode.getTitle() + "** is being added.")
                                .addField("Progress:",
                                        BotEmojis.FINISHED_STEP + "  User selects movie from <https://www.imdb.com>\n" +
                                                BotEmojis.FINISHED_STEP + "  Locate movie file\n" +
                                                BotEmojis.FINISHED_STEP + "  Mask download file\n" +
                                                BotEmojis.TODO_STEP + "  **Download movie:** " + downloadManager.getProgress() + "\n" +
                                                BotEmojis.TODO_STEP + "  Add movie to database")
                                .setFooter("Message updated: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                                        .format(ZonedDateTime.now()) + " CST")
                                .setColor(BotColors.INFO))
                                .exceptionally(ExceptionLogger.get());
                        lastUpdated = LocalDateTime.now();
                    }
                } catch (InterruptedException e) {
                    displayError("There was an error while downloading this movie. Please try again later.", "file-download-fail");
                    endTask(e);
                    return;
                }
            }
        }

        // Verify that there was not an unknown error
        if (downloadManager.didUnknownErrorOccur()) {
            displayError("An unknown error has occurred while downloading this movie. Brandan has been notified of this issue.", downloadManager.getErrorMessage());
            try {
                BotClient.getInstance().rdbApi.deleteTorrent(rdbTorrentInfo.getId());
            } catch (Exception e) {
                endTask(e);
                return;
            }
            endTask();
            return;
        }

        // Verify that the download did not fail
        if (downloadManager.didDownloadFail()) {
            displayError("There was an error while downloading this movie. Please try again later.", "file-download-fail");
            try {
                BotClient.getInstance().rdbApi.deleteTorrent(rdbTorrentInfo.getId());
            } catch (Exception e) {
                endTask(e);
                return;
            }
            endTask();
            return;
        }

        // Wait for the bot to finish processing the file
        synchronized (downloadManager.lock) {
            LocalDateTime lastUpdated = LocalDateTime.now();
            while (downloadManager.isProcessing() && !downloadManager.didUnknownErrorOccur()) {
                try {
                    downloadManager.lock.wait(10000);

                    // Update the download progress to show it's processing the file
                    if (lastUpdated.plus(5, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        sentMessage.edit(new EmbedBuilder()
                                .setTitle("Addition Status")
                                .setDescription("The movie **" + selectedEpisode.getTitle() + "** is being added.")
                                .addField("Progress:",
                                        BotEmojis.FINISHED_STEP + "  User selects movie from <https://www.imdb.com>\n" +
                                                BotEmojis.FINISHED_STEP + "  Locate movie file\n" +
                                                BotEmojis.FINISHED_STEP + "  Mask download file\n" +
                                                BotEmojis.TODO_STEP + "  **Download movie:** Processing...\n" +
                                                BotEmojis.TODO_STEP + "  Add movie to database")
                                .setFooter("Message updated: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                                        .format(ZonedDateTime.now()) + " CST")
                                .setColor(BotColors.INFO))
                                .exceptionally(ExceptionLogger.get());
                        lastUpdated = LocalDateTime.now();
                    }
                } catch (InterruptedException e) {
                    displayError("There was an error while processing this movie. Please try again later.", "file-process-fail");
                    endTask(e);
                    return;
                }
            }
        }

        // Verify that there was not an unknown error
        if (downloadManager.didUnknownErrorOccur()) {
            displayError("An unknown error has occurred while processing this movie. Brandan has been notified of this issue.", downloadManager.getErrorMessage());
            try {
                BotClient.getInstance().rdbApi.deleteTorrent(rdbTorrentInfo.getId());
            } catch (Exception e) {
                endTask(e);
                return;
            }
            endTask();
            return;
        }

        // Check if the processing of the file failed
        if (downloadManager.didProcessingFail()) {
            if (!downloadManager.isFileServerMounted()) {
                displayError("There was an error while processing this movie. Brandan has been notified " +
                        "of this issue and will manually correct it when he is available.", "nfs-connection-fail");
                Main.getBotApi().getUserById(BotConfig.getInstance().adminUserId()).join().sendMessage(
                        new EmbedBuilder()
                                .setTitle("Bot Error")
                                .setDescription("The bot attempted to download a movie, however the NFS server is not mounted. " +
                                        "Please mount the server and manually finish processing the file and add it to the database.\n")
                                .addField("Process Command:", "```bash\n" +
                                        "mv " + BotConfig.getInstance().tempFolder() + "'" + downloadManager.getFilename() + ".pbdownload' " +
                                        BotConfig.getInstance().movieFolder() + "'" + downloadManager.getFilename() + fileExtension + "'\n```")
                                .addField("SQL Script:", "```sql\n" +
                                        "INSERT INTO `Movies` (`movie_id`, `movie_filename`, `movie_resolution`, `movie_title`, `movie_year`) VALUES (" +
                                        "'" + selectedEpisode.getImdbID() + "', " +
                                        "'" + downloadManager.getFilename() + fileExtension + "', " +
                                        "'" + torrentHandler.getTorrentQuality() + "', " +
                                        "'" + selectedEpisode.getTitle() + "', " +
                                        "'" + selectedEpisode.getYear() + "');\n```")
                                .setColor(BotColors.ERROR)
                                .setFooter("This message was sent by the Plexbot and no reply will be received to messages sent here.")
                );
            } else {
                displayError("There was an error while downloading this movie. Please try again later.", "file-process-fail");
            }
            endTask();
            return;
        }

        // Delete the torrent from RealDebrid
        try {
            BotClient.getInstance().rdbApi.deleteTorrent(rdbTorrentInfo.getId());
        } catch (Exception e) {
            reportError(e);
        }

        sentMessage.edit(new EmbedBuilder()
                .setTitle("Addition Status")
                .setDescription("The movie **" + selectedEpisode.getTitle() + "** is being added.")
                .addField("Progress:",
                        BotEmojis.FINISHED_STEP + "  User selects movie from <https://www.imdb.com>\n" +
                                BotEmojis.FINISHED_STEP + "  Locate movie file\n" +
                                BotEmojis.FINISHED_STEP + "  Mask download file\n" +
                                BotEmojis.FINISHED_STEP + "  Download movie\n" +
                                BotEmojis.TODO_STEP + "  **Add movie to database**")
                .setFooter("Message updated: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .format(ZonedDateTime.now()) + " CST")
                .setColor(BotColors.INFO))
                .exceptionally(ExceptionLogger.get());

        // Add the movie to the database
        DbOperations.saveObject(new MovieBuilder()
                .withId(selectedEpisode.getImdbID())
                .withTitle(selectedEpisode.getTitle())
                .withYear(selectedEpisode.getYear())
                .withResolution(torrentHandler.getTorrentQuality())
                .withFolderName(downloadManager.getFilename())
                .withFilename(downloadManager.getFilename() + fileExtension)
                .withExtension(fileExtension.replace(".", ""))
                .withWidth(MediaInfoHelper.getWidth(BotConfig.getInstance().movieFolder() + downloadManager.getFilename() + "/" +
                        downloadManager.getFilename() + fileExtension))
                .withHeight(MediaInfoHelper.getHeight(BotConfig.getInstance().movieFolder() + downloadManager.getFilename() + "/" +
                        downloadManager.getFilename() + fileExtension))
                .build()
        );

        // Trigger a refresh of the media libraries on the plex server
        try {
            BotClient.getInstance().plexApi.refreshLibraries();
        } catch (Exception e) {
            reportError(e);
        }

        sentMessage.edit(new EmbedBuilder()
                .setTitle("Addition Status")
                .setDescription("The movie **" + selectedEpisode.getTitle() + "** has been added.")
                .addField("Progress:",
                        BotEmojis.FINISHED_STEP + "  User selects movie from <https://www.imdb.com>\n" +
                                BotEmojis.FINISHED_STEP + "  Locate movie file\n" +
                                BotEmojis.FINISHED_STEP + "  Mask download file\n" +
                                BotEmojis.FINISHED_STEP + "  Download movie\n" +
                                BotEmojis.FINISHED_STEP + "  Add movie to database\n\u200b")
                .addField(selectedEpisode.getTitle(),
                        "**Year:** " + selectedEpisode.getYear() + "\n" +
                                "**Director(s):** " + selectedEpisode.getDirector() + "\n" +
                                "**Plot:** " + selectedEpisode.getPlot())
                .setImage(selectedEpisode.getPoster())
                .setFooter("Added on: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .format(ZonedDateTime.now()) + " CST")
                .setColor(BotColors.SUCCESS)
        ).exceptionally(ExceptionLogger.get());

        Main.getBotApi().getTextChannelById(BotConfig.getInstance().newMoviesChannelId()).ifPresent(textChannel ->
                textChannel.sendMessage(new EmbedBuilder()
                        .setTitle(selectedEpisode.getTitle())
                        .setDescription("**Year:** " + selectedEpisode.getYear() + "\n" +
                                "**Director(s):** " + selectedEpisode.getDirector() + "\n" +
                                "**Plot:** " + selectedEpisode.getPlot())
                        .setImage(selectedEpisode.getPoster())
                        .setColor(BotColors.SUCCESS))
                        .exceptionally(ExceptionLogger.get()
                        )
        );

        Main.getBotApi().getUserById(userId).join().sendMessage(new EmbedBuilder()
                .setTitle("Movie Added")
                .setDescription("You requested the following movie be added to Celestial Movies Plex Server. This message is to notify you that the movie is now available on Plex.\n\n" +
                        "**Title:** " + selectedEpisode.getTitle() + "\n" +
                        "**Year:** " + selectedEpisode.getYear() + "\n" +
                        "**Director(s):** " + selectedEpisode.getDirector() + "\n" +
                        "**Plot:** " + selectedEpisode.getPlot())
                .setImage(selectedEpisode.getPoster())
                .setColor(BotColors.SUCCESS)
                .setFooter("This message was sent by the Plexbot and no reply will be received to messages sent here.")
        );
         */

        endTask();
    }

    /**
     * Changes the sentMessage to an error message if there is an error.
     *
     * @param warningMessage The error message to set the body of the message to.
     */
    private void displayWarning(String warningMessage) {
        sentMessage.edit(new EmbedBuilder()
                .addField("An warning has occurred:", "```" + warningMessage + "```")
                .setColor(BotColors.WARNING)
        );
    }

    /**
     * Changes the sentMessage to an error message if there is an error.
     *
     * @param errorMessage The error message to set the body of the message to.
     */
    private void displayError(String errorMessage) {
        sentMessage.edit(new EmbedBuilder()
                .addField("An error has occurred:", "```" + errorMessage + "```")
                .setColor(BotColors.ERROR)
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
}