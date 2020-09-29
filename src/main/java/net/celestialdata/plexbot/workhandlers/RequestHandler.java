package net.celestialdata.plexbot.workhandlers;

import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.apis.omdb.Omdb;
import net.celestialdata.plexbot.apis.omdb.objects.movie.OmdbMovie;
import net.celestialdata.plexbot.apis.omdb.objects.search.SearchResult;
import net.celestialdata.plexbot.apis.omdb.objects.search.SearchResultResponse;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.database.DatabaseDataManager;
import net.celestialdata.plexbot.utils.BotColors;
import net.celestialdata.plexbot.utils.BotEmojis;
import net.celestialdata.plexbot.utils.BotStatusManager;
import net.celestialdata.plexbot.utils.WaitlistManager;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;


/**
 * Provides a thread with all the worker logic for selecting,
 * searching for and downloading a movie.
 *
 * @author Celestialdeath99
 */
public class RequestHandler implements Runnable {
    private final String processName;
    private final String searchTitle;
    private final String searchYear;
    private final String searchId;
    private final Message sentMessage;
    private final long userId;

    /**
     * Constructor for the worker thread.
     *
     * @param searchTitle The title of the movie to search for.
     * @param sentMessage The javacord message entity of the message the bot responds with.
     * @param userId The ID of the user that requested the movie.
     */
    public RequestHandler(String processName, String searchTitle, String searchYear, String searchId, Message sentMessage, long userId) {
        this.processName = processName;
        this.searchTitle = searchTitle;
        this.searchYear = searchYear;
        this.searchId = searchId;
        this.sentMessage = sentMessage;
        this.userId = userId;
    }

    /**
     * The method that runs the thread.
     */
    @Override
    public void run() {
        SearchResultResponse searchResponse;
        MovieSelectionHandler selectionHandler;
        OmdbMovie selectedMovie;
        TorrentHandler torrentHandler;
        RealDebridHandler realDebridHandler;
        DownloadHandler downloadHandler;
        String magnetLink;
        String downloadLink;

        // Search for the movie
        if (!searchId.isEmpty()) {
            searchResponse = new SearchResultResponse();
            OmdbMovie searchedMovie = Omdb.getMovieInfo(searchId);

            if (searchedMovie.Response.equalsIgnoreCase("False")) {
                searchResponse.Response = "False";
                searchResponse.Error = searchedMovie.Error;
            } else {
                SearchResult result = new SearchResult();

                result.Title = searchedMovie.Title;
                result.Year = Integer.valueOf(searchedMovie.Year);
                result.imdbID = searchedMovie.imdbID;
                result.Type = searchedMovie.Type;
                result.Poster = searchedMovie.Poster;

                searchResponse.addSearchResult(result);
                searchResponse.Response = "True";
            }
        } else if (!searchYear.isEmpty()) {
            searchResponse = Omdb.movieSearch(searchTitle, searchYear);
        } else searchResponse = Omdb.movieSearch(searchTitle);

        // Send an error message if the search did not succeed
        if (searchResponse.Response.equalsIgnoreCase("false")) {
            displayError(searchResponse.Error, "omdb-search-fail");
            BotStatusManager.getInstance().removeProcess(processName);
            return;
        }

        // Create the selection handler
        selectionHandler = new MovieSelectionHandler(searchResponse, sentMessage);

        // Wait for a movie to be selected in the selection handler
        synchronized (selectionHandler.lock) {
            while (!selectionHandler.getBeenSet()) {
                try {
                    selectionHandler.lock.wait();
                } catch (InterruptedException e) {
                    BotStatusManager.getInstance().removeProcess(processName);
                    return;
                }
            }
        }

        // If the user canceled the movie selection, cancel this process
        if (selectionHandler.getWasCanceled()) {
            BotStatusManager.getInstance().removeProcess(processName);
            return;
        }

        // Attempt to set the selected movie
        try {
            selectedMovie = selectionHandler.getSelectedMovie();
        } catch (NullPointerException e) {
            displayError("Unable to get the selected movie.", "movie-select-error");
            BotStatusManager.getInstance().removeProcess(processName);
            return;
        }

        // Verify the movie requested does not already exist on the server
        if (DatabaseDataManager.doesMovieExistOnServer(selectedMovie.imdbID)) {
            displayError("This movie is already on Plex.");
            BotStatusManager.getInstance().removeProcess(processName);
            return;
        }

        sentMessage.edit(new EmbedBuilder()
                .setTitle("Addition Status")
                .setDescription("The movie **" + selectedMovie.Title + "** is being added.")
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
        torrentHandler = new TorrentHandler(selectedMovie.imdbID);

        // Search YTS for the movie
        torrentHandler.searchYts();
        if (torrentHandler.didSearchFail()) {
            displayError("An error has occurred while searching for this movie's file. Please try again later.", "yts-search-fail");
            BotStatusManager.getInstance().removeProcess(processName);
            return;
        } else if (torrentHandler.didSearchReturnNoResults()) {
            if (DatabaseDataManager.isMovieInWaitlist(selectedMovie.imdbID)) {
                displayError("The movie " + selectedMovie.Title + " is not currently available and already exists on the waiting list.\n\n" +
                        "Please remember to check the waiting list next time before making a request.");
            } else {
                displayError("Unable to locate the file for " + selectedMovie.Title + " at this time. It has been added to the waiting list " +
                        "and will be automatically when it becomes available.");
                WaitlistManager.addWaitlistItem(selectedMovie, userId);
            }
            BotStatusManager.getInstance().removeProcess(processName);
            return;
        }

        // Build the YTS Movie List
        torrentHandler.buildMovieList();
        if (torrentHandler.didBuildMovieListFail()) {
            displayError("An error has occurred while attempting to locate the movie file. Please try again later.", "yts-movie-list");
            BotStatusManager.getInstance().removeProcess(processName);
            return;
        }

        // Build the list of torrents available
        torrentHandler.buildTorrentList();
        if (torrentHandler.areNoTorrentsAvailable()) {
            displayError("An error has occurred while attempting to locate the movie file. Please try again later.", "yts-torrent-list");
            BotStatusManager.getInstance().removeProcess(processName);
            return;
        }

        // Generate the magnet link
        torrentHandler.generateMagnetLink();
        if (torrentHandler.isNotMagnetLink()) {
            displayError("An error has occurred while attempting to locate the movie file. Please try again later.", "yts-magnet-link");
            BotStatusManager.getInstance().removeProcess(processName);
            return;
        }

        // Get the magnet link
        magnetLink = torrentHandler.getMagnetLink();
        sentMessage.edit(new EmbedBuilder()
                .setTitle("Addition Status")
                .setDescription("The movie **" + selectedMovie.Title + "** is being added.")
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

        // Create the RealDebridHandler and add the torrent
        realDebridHandler = new RealDebridHandler(magnetLink);
        realDebridHandler.addMagnet();
        if (realDebridHandler.didMagnetAdditionFail()) {
            displayError("There was an error masking the download. Please try again later.", "rdb-add-fail");
            BotStatusManager.getInstance().removeProcess(processName);
            return;
        }

        // Get the torrent file information
        realDebridHandler.getTorrentInformation();
        if (realDebridHandler.didTorrentInfoError()) {
            displayError("There was an error masking the download. Please try again later.", "rdb-get-info");
            realDebridHandler.deleteTorrent();
            BotStatusManager.getInstance().removeProcess(processName);
            return;
        }

        // Select the proper movie files to download
        realDebridHandler.selectTorrentFiles();
        if (realDebridHandler.didSelectOperationFail()) {
            displayError("There was an error masking the download. Please try again later.", "rdb-select-files");
            realDebridHandler.deleteTorrent();
            BotStatusManager.getInstance().removeProcess(processName);
            return;
        }

        // Wait for RealDebrid to download the movie file
        synchronized (realDebridHandler.lock) {
            LocalDateTime lastUpdated = LocalDateTime.now();

            while (realDebridHandler.isNotReadyForDownload()) {
                try {
                    realDebridHandler.lock.wait();

                    // Update the message timestamp and masking progress
                    if (lastUpdated.plus(5, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        sentMessage.edit(new EmbedBuilder()
                                .setTitle("Addition Status")
                                .setDescription("The movie **" + selectedMovie.Title + "** is being added.")
                                .addField("Progress:",
                                        BotEmojis.FINISHED_STEP + "  User selects movie from <https://www.imdb.com>\n" +
                                                BotEmojis.FINISHED_STEP + "  Locate movie file\n" +
                                                BotEmojis.TODO_STEP + "  **Mask download file** - " + realDebridHandler.getProgress() + "%\n" +
                                                BotEmojis.TODO_STEP + "  Download movie\n" +
                                                BotEmojis.TODO_STEP + "  Add movie to database")
                                .setFooter("Message updated: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                                        .format(ZonedDateTime.now()) + " CST")
                                .setColor(BotColors.INFO))
                                .exceptionally(ExceptionLogger.get());
                    }
                } catch (InterruptedException e) {
                    BotStatusManager.getInstance().removeProcess(processName);
                    return;
                }
            }
        }

        // Make the link Unrestricted
        realDebridHandler.unrestrictLinks();
        if (realDebridHandler.didUnrestrictOperationFail()) {
            displayError("There was an error masking the download. Please try again later.", "rdb-unrestrict-link");
            realDebridHandler.deleteTorrent();
            BotStatusManager.getInstance().removeProcess(processName);
            return;
        }

        // Get the download link
        downloadLink = realDebridHandler.getDownloadLink();
        sentMessage.edit(new EmbedBuilder()
                .setTitle("Addition Status")
                .setDescription("The movie **" + selectedMovie.Title + "** is being added.")
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
        downloadHandler = new DownloadHandler();
        downloadHandler.downloadFile(downloadLink, selectedMovie, true);

        // Wait for the file to be downloaded
        synchronized (downloadHandler.lock) {
            LocalDateTime lastUpdated = LocalDateTime.now();
            while (downloadHandler.isDownloading()) {
                try {
                    downloadHandler.lock.wait();

                    // Update the download progress message
                    if (lastUpdated.plus(5, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        sentMessage.edit(new EmbedBuilder()
                                .setTitle("Addition Status")
                                .setDescription("The movie **" + selectedMovie.Title + "** is being added.")
                                .addField("Progress:",
                                        BotEmojis.FINISHED_STEP + "  User selects movie from <https://www.imdb.com>\n" +
                                                BotEmojis.FINISHED_STEP + "  Locate movie file\n" +
                                                BotEmojis.FINISHED_STEP + "  Mask download file\n" +
                                                BotEmojis.TODO_STEP + "  **Download movie:** " + downloadHandler.getProgress() + "\n" +
                                                BotEmojis.TODO_STEP + "  Add movie to database")
                                .setFooter("Message updated: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                                        .format(ZonedDateTime.now()) + " CST")
                                .setColor(BotColors.INFO))
                                .exceptionally(ExceptionLogger.get());
                        lastUpdated = LocalDateTime.now();
                    }
                } catch (InterruptedException e) {
                    BotStatusManager.getInstance().removeProcess(processName);
                    return;
                }
            }
        }

        // Verify that the download did not fail
        if (downloadHandler.didDownloadFail()) {
            displayError("There was an error while downloading this movie. Please try again later.", "file-download-fail");
            realDebridHandler.deleteTorrent();
            BotStatusManager.getInstance().removeProcess(processName);
            return;
        }

        sentMessage.edit(new EmbedBuilder()
                .setTitle("Addition Status")
                .setDescription("The movie **" + selectedMovie.Title + "** is being added.")
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
        downloadHandler.renameFile(realDebridHandler.getExtension());

        // Verify the rename operation succeeded
        if (downloadHandler.didRenameFail()) {
            displayError("There was an error while downloading this movie. Please try again later.", "file-rename-fail");
            realDebridHandler.deleteTorrent();
            BotStatusManager.getInstance().removeProcess(processName);
            return;
        }

        // Delete the torrent from RealDebrid
        realDebridHandler.deleteTorrent();

        // Add the movie to the database
        DatabaseDataManager.addMovie(selectedMovie.imdbID, selectedMovie.Title, selectedMovie.Year, torrentHandler.getTorrentQuality());

        sentMessage.edit(new EmbedBuilder()
                .setTitle("Addition Status")
                .setDescription("The movie **" + selectedMovie.Title + "** has been added.")
                .addField("Progress:",
                        BotEmojis.FINISHED_STEP + "  User selects movie from <https://www.imdb.com>\n" +
                                BotEmojis.FINISHED_STEP + "  Locate movie file\n" +
                                BotEmojis.FINISHED_STEP + "  Mask download file\n" +
                                BotEmojis.FINISHED_STEP + "  Download movie\n" +
                                BotEmojis.FINISHED_STEP + "  Add movie to database\n\u200b")
                .addField(selectedMovie.Title,
                        "**Year:** " + selectedMovie.Year + "\n" +
                                "**Director(s):** " + selectedMovie.Director + "\n" +
                                "**Plot:** " + selectedMovie.Plot)
                .setImage(selectedMovie.Poster)
                .setFooter("Added on: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .format(ZonedDateTime.now()) + " CST")
                .setColor(BotColors.SUCCESS)
        ).exceptionally(ExceptionLogger.get());

        Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.newMoviesChannelId()).ifPresent(textChannel ->
                textChannel.sendMessage(new EmbedBuilder()
                        .setTitle(selectedMovie.Title)
                        .setDescription("**Year:** " + selectedMovie.Year + "\n" +
                                "**Director(s):** " + selectedMovie.Director + "\n" +
                                "**Plot:** " + selectedMovie.Plot)
                        .setImage(selectedMovie.Poster)
                        .setColor(BotColors.SUCCESS)
                        .setFooter("Originally Requested by: " + Main.getBotApi().getUserById(userId).join().getDiscriminatedName())).exceptionally(ExceptionLogger.get()
                )
        );

        Main.getBotApi().getUserById(userId).join().sendMessage(new EmbedBuilder()
                .setTitle("Movie Added")
                .setDescription("You requested the following movie be added to Celestial Movies Plex Server. This message is to notify you that the movie is now available on Plex.\n\n" +
                        "**Title:** " + selectedMovie.Title + "\n" +
                        "**Year:** " + selectedMovie.Year + "\n" +
                        "**Director(s):** " + selectedMovie.Director + "\n" +
                        "**Plot:** " + selectedMovie.Plot)
                .setImage(selectedMovie.Poster)
                .setColor(BotColors.SUCCESS)
                .setFooter("This message was sent by the Plexbot and no reply will be received to messages sent here.")
        );

        BotStatusManager.getInstance().removeProcess(processName);
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
     * @param errorCode The error code to add to the message footer.
     */
    private void displayError(String errorMessage, String errorCode) {
        sentMessage.edit(new EmbedBuilder()
                .addField("An error has occurred:", "```" + errorMessage + "```")
                .setFooter("Error Code:  " + errorCode)
                .setColor(BotColors.ERROR)
        );
    }
}