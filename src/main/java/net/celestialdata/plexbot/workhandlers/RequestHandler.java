package net.celestialdata.plexbot.workhandlers;

import net.celestialdata.plexbot.BotWorkPool;
import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.client.ApiException;
import net.celestialdata.plexbot.client.BotClient;
import net.celestialdata.plexbot.client.model.*;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.database.DbOperations;
import net.celestialdata.plexbot.database.builders.MovieBuilder;
import net.celestialdata.plexbot.database.builders.WaitlistItemBuilder;
import net.celestialdata.plexbot.managers.DownloadManager;
import net.celestialdata.plexbot.utils.BotColors;
import net.celestialdata.plexbot.utils.BotEmojis;
import net.celestialdata.plexbot.utils.CustomRunnable;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


/**
 * Provides a thread with all the worker logic for selecting,
 * searching for and downloading a movie.
 *
 * @author Celestialdeath99
 */
@SuppressWarnings("DuplicatedCode")
public class RequestHandler implements CustomRunnable {
    private final String processName;
    private final String searchTitle;
    private final String searchYear;
    private final String searchId;
    private final Message sentMessage;
    private final long userId;
    private final Object rdbLock = new Object();

    /**
     * Constructor for the worker thread.
     *
     * @param processName the name of the process for the work queue
     * @param searchTitle The title of the movie to search for.
     * @param sentMessage The javacord message entity of the message the bot responds with.
     * @param userId      The ID of the user that requested the movie.
     */
    public RequestHandler(String processName, String searchTitle, String searchYear, String searchId, Message sentMessage, long userId) {
        this.processName = processName;
        this.searchTitle = searchTitle;
        this.searchYear = searchYear;
        this.searchId = searchId;
        this.sentMessage = sentMessage;
        this.userId = userId;
    }

    @Override
    public String taskName() {
        return processName;
    }

    /**
     * The method that runs the thread.
     */
    @Override
    public void run() {
        // Configure task to run the endTask method if there was an error
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> endTask(e));

        MovieSelectionHandler selectionHandler;
        OmdbMovieInfo selectedMovie;
        TorrentHandler torrentHandler;
        DownloadManager downloadManager;
        String magnetLink;
        String downloadLink;

        // Search for the movie
        List<OmdbMovieInfo> resultList = new ArrayList<>();
        if (!searchId.isEmpty()) {
            try {
                var result = BotClient.getInstance().omdbApi.getById(searchId);
                if (result.getResponse() == OmdbMovieInfo.ResponseEnum.TRUE) {
                    resultList.add(result);
                } else {
                    displayError("Unrecognized IMDB Code, please check your code and try again.");
                    endTask();
                    return;
                }
            } catch (Exception e) {
                displayError(e.getMessage(), "omdb-lookup-id");
                endTask();
                return;
            }
        } else if (!searchYear.isEmpty()) {
            if (searchTitle.isEmpty()) {
                displayError("You must provide a movie title to search for in addition to a year.");
                endTask();
                return;
            } else {
                try {
                    var result = BotClient.getInstance().omdbApi.search(searchTitle, searchYear);
                    if (result.getResponse() == OmdbSearchResult.ResponseEnum.TRUE) {
                        resultList = result.getSearch();
                    } else {
                        displayError("No movies found. Please adjust your search parameters and try again.");
                        endTask();
                        return;
                    }
                } catch (Exception e) {
                    displayError(e.getMessage(), "omdb-search-year");
                    endTask();
                    return;
                }
            }
        } else {
            try {
                var result = BotClient.getInstance().omdbApi.search(searchTitle);
                if (result.getResponse() == OmdbSearchResult.ResponseEnum.TRUE) {
                    resultList = result.getSearch();
                } else {
                    displayError("No movies found. Please adjust your search parameters and try again.");
                    endTask();
                    return;
                }
            } catch (Exception e) {
                displayError(e.getMessage(), "omdb-search");
                endTask();
                return;
            }
        }

        // Fetch more detailed information about the search results
        for (int i = 0; i < resultList.size(); i++) {
            try {
                resultList.set(i, BotClient.getInstance().omdbApi.getById(resultList.get(i).getImdbID()));
            } catch (Exception e) {
                displayError("Unable to parse results. Please try again later.", "omdb-search-details");
                endTask(e);
                return;
            }
        }

        // Create the selection handler
        selectionHandler = new MovieSelectionHandler(resultList, sentMessage);

        // Wait for a movie to be selected in the selection handler
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
            selectedMovie = selectionHandler.getSelectedMovie();
        } catch (NullPointerException e) {
            displayError("Unable to get the selected movie.", "movie-select-error");
            endTask(e);
            return;
        }

        // Verify the movie requested does not already exist on the server
        if (DbOperations.movieOps.exists(selectedMovie.getImdbID())) {
            displayError("This movie is already on Plex.");
            endTask();
            return;
        }

        sentMessage.edit(new EmbedBuilder()
                .setTitle("Addition Status")
                .setDescription("The movie **" + selectedMovie.getTitle() + "** is being added.")
                .addField("Progress:",
                        BotEmojis.FINISHED_STEP + "  User selects movie from <https://www.imdb.com>\n" +
                                BotEmojis.TODO_STEP + "  **Locate movie file**\n" +
                                BotEmojis.TODO_STEP + "  Mask download file\n" +
                                BotEmojis.TODO_STEP + "  Download movie\n" +
                                BotEmojis.TODO_STEP + "  Add movie to database")
                .setFooter("Message updated: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .format(ZonedDateTime.now()) + " CST")
                .setColor(BotColors.INFO))
                .exceptionally(ExceptionLogger.get());

        // Setup the torrent handler
        torrentHandler = new TorrentHandler(selectedMovie.getImdbID());

        // Search YTS for the movie
        try {
            torrentHandler.searchYts();
        } catch (Exception e) {
            displayError("An error has occurred while searching for this movie's file. Please try again later.", "yts-search-fail");
            endTask();
            return;
        }

        // Add movie to waitinglist if it was not located
        if (torrentHandler.didSearchReturnNoResults()) {
            if (DbOperations.waitlistItemOps.exists(selectedMovie.getImdbID())) {
                displayError("The movie " + selectedMovie.getTitle() + " is not currently available and already exists on the waiting list.");
            } else {
                displayError("Unable to locate the file for " + selectedMovie.getTitle() + " at this time. It has been added to the waiting list " +
                        "and will be automatically when it becomes available.");
                DbOperations.saveObject(new WaitlistItemBuilder()
                        .fromOmdbInfo(selectedMovie)
                        .withRequestedBy(userId)
                        .build()
                );
            }
            endTask();
            return;
        }

        // Build the YTS Movie List
        torrentHandler.buildMovieList();
        if (torrentHandler.didBuildMovieListFail()) {
            displayError("An error has occurred while attempting to locate the movie file. Please try again later.", "yts-movie-list");
            endTask();
            return;
        }

        // Build the list of torrents available
        torrentHandler.buildTorrentList();
        if (torrentHandler.areNoTorrentsAvailable()) {
            displayError("An error has occurred while attempting to locate the movie file. Please try again later.", "yts-torrent-list");
            endTask();
            return;
        }

        // Generate the magnet link
        torrentHandler.generateMagnetLink();
        if (torrentHandler.isNotMagnetLink()) {
            displayError("An error has occurred while attempting to locate the movie file. Please try again later.", "yts-magnet-link");
            endTask();
            return;
        }

        // Get the magnet link
        magnetLink = torrentHandler.getMagnetLink();
        sentMessage.edit(new EmbedBuilder()
                .setTitle("Addition Status")
                .setDescription("The movie **" + selectedMovie.getTitle() + "** is being added.")
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
            e.printStackTrace();
            displayError("There was an error masking the download. Please try again later.", "rdb-get-info");
            endTask(e);
            return;
        }

        // Select the proper movie files to download
        String fileToSelect = "";
        String fileExtension = "";
        for (RdbTorrentFile file : rdbTorrentInfo.getFiles()) {
            if (file.getPath().contains(".mp4") || file.getPath().contains(".MP4")) {
                fileToSelect = String.valueOf(file.getId());
                fileExtension = ".mp4";
            } else if (file.getPath().contains(".mkv") || file.getPath().contains(".MKV")) {
                fileToSelect = String.valueOf(file.getId());
                fileExtension = ".mkv";
            }
        }

        try {
            BotClient.getInstance().rdbApi.selectTorrentFiles(rdbTorrentInfo.getId(), fileToSelect);
        } catch (Exception e) {
            e.printStackTrace();
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
                            .setDescription("The movie **" + selectedMovie.getTitle() + "** is being added.")
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

                    rdbLock.wait(5000);
                    rdbTorrentInfo = BotClient.getInstance().rdbApi.getTorrentInfo(rdbMagnetLink.getId());
                }
            }

            while (rdbTorrentInfo.getStatus() == RdbTorrentInfo.StatusEnum.UPLOADING ||
                    rdbTorrentInfo.getStatus() == RdbTorrentInfo.StatusEnum.COMPRESSING) {
                rdbLock.wait(2000);
                rdbTorrentInfo = BotClient.getInstance().rdbApi.getTorrentInfo(rdbMagnetLink.getId());
            }

            if (BotClient.getInstance().rdbApi.getTorrentInfo(rdbTorrentInfo.getId()).getStatus() != RdbTorrentInfo.StatusEnum.DOWNLOADED) {
                displayError("There was an error masking the download. Please try again later.", "rdb-download-file");
                endTask();
                return;
            }
        } catch (InterruptedException | ApiException e) {
            e.printStackTrace();
            displayError("There was an error masking the download. Please try again later.", "rdb-download-file");
            endTask(e);
            return;
        }

        // Make the link unrestricted
        RdbUnrestrictedLink unrestrictedLink;
        try {
            unrestrictedLink = BotClient.getInstance().rdbApi.unrestrictLink(rdbTorrentInfo.getLinks().get(0));
        } catch (Exception e) {
            e.printStackTrace();
            displayError("There was an error masking the download. Please try again later.", "rdb-unrestrict-link");
            try {
                e.printStackTrace();
                BotClient.getInstance().rdbApi.deleteTorrent(rdbTorrentInfo.getId());
            } catch (ApiException ignored) {}
            endTask(e);
            return;
        }

        // Get the download link
        downloadLink = unrestrictedLink.getDownload();
        sentMessage.edit(new EmbedBuilder()
                .setTitle("Addition Status")
                .setDescription("The movie **" + selectedMovie.getTitle() + "** is being added.")
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
        downloadManager = new DownloadManager(downloadLink, selectedMovie);
        BotWorkPool.getInstance().submitProcess(downloadManager);

        // Wait for the file to be downloaded
        synchronized (downloadManager.lock) {
            LocalDateTime lastUpdated = LocalDateTime.now();
            while (downloadManager.isDownloading()) {
                try {
                    downloadManager.lock.wait();

                    // Update the download progress message
                    if (lastUpdated.plus(5, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        sentMessage.edit(new EmbedBuilder()
                                .setTitle("Addition Status")
                                .setDescription("The movie **" + selectedMovie.getTitle() + "** is being added.")
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
                    endTask(e);
                    return;
                }
            }
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

        sentMessage.edit(new EmbedBuilder()
                .setTitle("Addition Status")
                .setDescription("The movie **" + selectedMovie.getTitle() + "** is being added.")
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

        // Rename the downloaded file to add the extension
        // Verify the rename operation succeeded
        if (!downloadManager.renameFile(fileExtension)) {
            displayError("There was an error while downloading this movie. Please try again later.", "file-rename-fail");
            //realDebridHandler.deleteTorrent();
            try {
                BotClient.getInstance().rdbApi.deleteTorrent(rdbTorrentInfo.getId());
            } catch (Exception e) {
                endTask(e);
                return;
            }
            endTask();
            return;
        }

        // Delete the torrent from RealDebrid
        try {
            BotClient.getInstance().rdbApi.deleteTorrent(rdbTorrentInfo.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add the movie to the database
        DbOperations.saveObject(new MovieBuilder()
                .withId(selectedMovie.getImdbID())
                .withTitle(selectedMovie.getTitle())
                .withYear(selectedMovie.getYear())
                .withResolution(torrentHandler.getTorrentQuality())
                .withFilename(downloadManager.getFilename() + fileExtension)
                .build()
        );

        // Trigger a refresh of the media libraries on the plex server
        try {
            BotClient.getInstance().plexApi.refreshLibraries();
        } catch (Exception e) {
            e.printStackTrace();
        }

        sentMessage.edit(new EmbedBuilder()
                .setTitle("Addition Status")
                .setDescription("The movie **" + selectedMovie.getTitle() + "** has been added.")
                .addField("Progress:",
                        BotEmojis.FINISHED_STEP + "  User selects movie from <https://www.imdb.com>\n" +
                                BotEmojis.FINISHED_STEP + "  Locate movie file\n" +
                                BotEmojis.FINISHED_STEP + "  Mask download file\n" +
                                BotEmojis.FINISHED_STEP + "  Download movie\n" +
                                BotEmojis.FINISHED_STEP + "  Add movie to database\n\u200b")
                .addField(selectedMovie.getTitle(),
                        "**Year:** " + selectedMovie.getYear() + "\n" +
                                "**Director(s):** " + selectedMovie.getDirector() + "\n" +
                                "**Plot:** " + selectedMovie.getPlot())
                .setImage(selectedMovie.getPoster())
                .setFooter("Added on: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .format(ZonedDateTime.now()) + " CST")
                .setColor(BotColors.SUCCESS)
        ).exceptionally(ExceptionLogger.get());

        Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.newMoviesChannelId()).ifPresent(textChannel ->
                textChannel.sendMessage(new EmbedBuilder()
                        .setTitle(selectedMovie.getTitle())
                        .setDescription("**Year:** " + selectedMovie.getYear() + "\n" +
                                "**Director(s):** " + selectedMovie.getDirector() + "\n" +
                                "**Plot:** " + selectedMovie.getPlot())
                        .setImage(selectedMovie.getPoster())
                        .setColor(BotColors.SUCCESS))
                        .exceptionally(ExceptionLogger.get()
                        )
        );

        Main.getBotApi().getUserById(userId).join().sendMessage(new EmbedBuilder()
                .setTitle("Movie Added")
                .setDescription("You requested the following movie be added to Celestial Movies Plex Server. This message is to notify you that the movie is now available on Plex.\n\n" +
                        "**Title:** " + selectedMovie.getTitle() + "\n" +
                        "**Year:** " + selectedMovie.getYear() + "\n" +
                        "**Director(s):** " + selectedMovie.getDirector() + "\n" +
                        "**Plot:** " + selectedMovie.getPlot())
                .setImage(selectedMovie.getPoster())
                .setColor(BotColors.SUCCESS)
                .setFooter("This message was sent by the Plexbot and no reply will be received to messages sent here.")
        );

        endTask();
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