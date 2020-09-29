package net.celestialdata.plexbot.utils;

import net.celestialdata.plexbot.BotWorkPool;
import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.apis.omdb.Omdb;
import net.celestialdata.plexbot.apis.omdb.objects.movie.OmdbMovie;
import net.celestialdata.plexbot.workhandlers.DownloadHandler;
import net.celestialdata.plexbot.workhandlers.RealDebridHandler;
import net.celestialdata.plexbot.workhandlers.TorrentHandler;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.database.DatabaseDataManager;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static net.celestialdata.plexbot.Main.scheduledExecutorService;

/**
 * A class for handling all things related to the waiting list.
 *
 * @author Celestialdeath99
 */
public class WaitlistManager {

    /**
     * Override the default constructor. This is used to schedule the checks used to
     * check if a movie in the waitlist is now available.
     */
    public WaitlistManager() {
        scheduledExecutorService.scheduleAtFixedRate(this::runCheck, 0, 1, TimeUnit.HOURS);
    }

    /**
     * Add a movie to the waitlist
     *
     * @param movie the OmdbMovie to add
     * @param userId the ID of the user who requested the movie
     * @see OmdbMovie
     */
    public static void addWaitlistItem(OmdbMovie movie, long userId) {
        // Check if the movie is already in the waiting list, otherwise send a message about it and add it
        if (!DatabaseDataManager.isMovieInWaitlist(movie.imdbID)) {
            Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.waitlistChannelId()).ifPresent(textChannel ->
                    textChannel.sendMessage(new EmbedBuilder()
                            .setTitle(movie.Title)
                            .setDescription("**Year:** " + movie.Year + "\n" +
                                            "**Director(s):** " + movie.Director + "\n" +
                                            "**Plot:** " + movie.Plot)
                            .setImage(movie.Poster)
                            .setColor(BotColors.INFO)
                            .setFooter("Last Checked: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                                    .format(ZonedDateTime.now()) + " CST")).exceptionally(ExceptionLogger.get()
                    ).thenAccept(message ->
                            DatabaseDataManager.addMovieToWaitlist(movie.imdbID, movie.Title, movie.Year, userId, message.getId())));
        }
    }

    /**
     * Update the message about a movie with an updated timestamp for when it was last checked for.
     *
     * @param movie the OmdbMovie to update the message for
     * @see OmdbMovie
     */
    private void updateMessage(OmdbMovie movie) {
        // Get the channel the waitlist messages are in then fetch the message for the movie and update it with
        // the current date and time.
        Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.waitlistChannelId()).ifPresent(textChannel ->
                textChannel.getMessageById(DatabaseDataManager.getWaitlistMessageId(movie.imdbID)).join().edit(new EmbedBuilder()
                        .setTitle(movie.Title)
                        .setDescription("**Year:** " + movie.Year + "\n" +
                                        "**Director(s):** " + movie.Director + "\n" +
                                        "**Plot:** " + movie.Plot)
                        .setImage(movie.Poster)
                        .setColor(BotColors.INFO)
                        // TODO: Allow the timezone label to be changed in the bot configuration file
                        .setFooter("Last Checked: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                                .format(ZonedDateTime.now()) + " CST")).exceptionally(ExceptionLogger.get()));
    }

    /**
     * Delete a movie from the waiting list.
     *
     * @param movieId the IMDB ID of the movie
     */
    private void deleteWaitlistItem(String movieId) {
        // Remove the message about the movie from the waiting-list channel
        Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.waitlistChannelId()).ifPresent(textChannel ->
                textChannel.getMessageById(DatabaseDataManager.getWaitlistMessageId(movieId)).join().delete().exceptionally(ExceptionLogger.get()));

        // Remove the movie from the waiting-list table in the database
        DatabaseDataManager.removeMovieFromWaitlist(movieId);
    }

    /**
     * Check to see if movies in the waiting list are now available
     */
    private void runCheck() {
        // This runnable runs once per schedules time and checks the entire waiting list
        Runnable checker = () -> {
            int progress = 0;
            ArrayList<String> movieIds = DatabaseDataManager.getMovieIdsInWaitlist();

            // Fetch all the movies in the waiting list and cycle through them
            for (String id : movieIds) {
                progress++;
                BotStatusManager.getInstance().setWaitlistManagerStatus(progress, movieIds.size());

                // Get the info about the movie from IMDB
                OmdbMovie movie = Omdb.getMovieInfo(id);
                TorrentHandler torrentHandler;

                // Set the default movie poster if one is not available
                if (movie.Poster.equalsIgnoreCase("N/A"))  {
                    movie.Poster = ConfigProvider.BOT_SETTINGS.noPosterImageUrl();
                }

                // Move to the next movie if the movie was manually added to the server/db
                // or already exists for some reason.
                if (DatabaseDataManager.doesMovieExistOnServer(movie.imdbID)) {
                    deleteWaitlistItem(movie.imdbID);
                    continue;
                }

                // Set the torrent handler to the ID of the movie
                torrentHandler = new TorrentHandler(movie.imdbID);

                // Search YTS for the movie
                try {
                    torrentHandler.searchYts();
                } catch (NullPointerException e) {
                    continue;
                }

                // If the search failed or if the movie was not found then skip to the next movie
                if (torrentHandler.didSearchFail()) {
                    updateMessage(movie);
                    continue;
                } else if (torrentHandler.didSearchReturnNoResults()) {
                    updateMessage(movie);
                    continue;
                }

                // If the movie was found, add a task to the work queue to download the movie
                downloadMovie(torrentHandler, movie);
            }

            BotStatusManager.getInstance().removeProcess("Waitlist Manager");
            BotStatusManager.getInstance().clearWaitlistManagerStatus();
        };

        // Add this runnable to the bot work pool to be executed
        BotWorkPool.getInstance().submitProcess("Waitlist Manager", checker);
    }

    /**
     * Download the provided movie from the waiting list.
     *
     * @param torrentHandler the torrentHandler containing the information about the movie
     *                       so that the bot does not need to fetch it again
     * @param movie the OmdbMovie object containing the movie to download
     * @see TorrentHandler
     * @see OmdbMovie
     */
    private void downloadMovie(TorrentHandler torrentHandler, OmdbMovie movie) {
        Runnable downloader = () -> {
            RealDebridHandler realDebridHandler;
            DownloadHandler downloadHandler;
            String magnetLink;
            String downloadLink;

            // Get a list of movies matching the movie id
            torrentHandler.buildMovieList();
            if (torrentHandler.didBuildMovieListFail()) {
                updateMessage(movie);
                BotStatusManager.getInstance().removeProcess("Download " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Build the list of torrent files for the movie
            torrentHandler.buildTorrentList();
            if (torrentHandler.areNoTorrentsAvailable()) {
                updateMessage(movie);
                BotStatusManager.getInstance().removeProcess("Download " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Generate the magnet link for the movie torrent file
            torrentHandler.generateMagnetLink();
            if (torrentHandler.isNotMagnetLink()) {
                updateMessage(movie);
                BotStatusManager.getInstance().removeProcess("Download " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Add the torrent to real-debrid through its magnet link
            magnetLink = torrentHandler.getMagnetLink();
            realDebridHandler = new RealDebridHandler(magnetLink);
            realDebridHandler.addMagnet();
            if (realDebridHandler.didMagnetAdditionFail()) {
                updateMessage(movie);
                BotStatusManager.getInstance().removeProcess("Download " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Get the information about the torrent on real-debrid
            realDebridHandler.getTorrentInformation();
            if (realDebridHandler.didTorrentInfoError()) {
                realDebridHandler.deleteTorrent();
                updateMessage(movie);
                BotStatusManager.getInstance().removeProcess("Download " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Select the proper files to download on real-debrid
            realDebridHandler.selectTorrentFiles();
            if (realDebridHandler.didSelectOperationFail()) {
                realDebridHandler.deleteTorrent();
                updateMessage(movie);
                BotStatusManager.getInstance().removeProcess("Download " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Wait for real-debrid to have the movie ready for downloading
            synchronized (realDebridHandler.lock) {
                while (realDebridHandler.isNotReadyForDownload()) {
                    try {
                        realDebridHandler.lock.wait();
                    } catch (InterruptedException e) {
                        BotStatusManager.getInstance().removeProcess("Download " + movie.Title + " (" + movie.Year + ")");
                        return;
                    }
                }
            }

            // Unrestrict the download link on real-debrid to allow the bot to download it
            realDebridHandler.unrestrictLinks();
            if (realDebridHandler.didUnrestrictOperationFail()) {
                realDebridHandler.deleteTorrent();
                updateMessage(movie);
                BotStatusManager.getInstance().removeProcess("Download " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Get the download link and create the DownloadHandler for the movie
            downloadLink = realDebridHandler.getDownloadLink();
            downloadHandler = new DownloadHandler();

            // Specify not to run the download in a separate thread, but to instead this one.
            // This keeps the process all together, separating the download process is only required
            // in the RequestCommandHandler in order to be able to update the download progress in the
            // status message.
            downloadHandler.downloadFile(downloadLink, movie, false);

            // Wait for the download to finish downloading the movie file
            synchronized (downloadHandler.lock) {
                while (downloadHandler.isDownloading()) {
                    try {
                        downloadHandler.lock.wait();
                    } catch (InterruptedException e) {
                        BotStatusManager.getInstance().removeProcess("Download " + movie.Title + " (" + movie.Year + ")");
                        return;
                    }
                }
            }

            // Exit if the download failed, cleaning up the torrent on real-debrid in the process
            if (downloadHandler.didDownloadFail()) {
                // TODO: Delete any portion of the file that was downloaded before the failure occurred
                realDebridHandler.deleteTorrent();
                updateMessage(movie);
                BotStatusManager.getInstance().removeProcess("Download " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Rename the movie file using the proper extension so that Plex will find it
            downloadHandler.renameFile(realDebridHandler.getExtension());

            // Exit if the rename process failed, and clean up the torrent on real-debrid in the process
            if (downloadHandler.didRenameFail()) {
                // TODO: Delete the file that was downloaded but failed to be renamed
                realDebridHandler.deleteTorrent();
                updateMessage(movie);
                BotStatusManager.getInstance().removeProcess("Download " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Delete the torrent file from real-debrid
            realDebridHandler.deleteTorrent();

            // Add the movie to the database
            DatabaseDataManager.addMovie(movie.imdbID, movie.Title, movie.Year, torrentHandler.getTorrentQuality());

            // Send a message to the new-movies notification channel stating the movie is now available on Plex
            Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.newMoviesChannelId()).ifPresent(textChannel ->
                    textChannel.sendMessage(new EmbedBuilder()
                            .setTitle(movie.Title)
                            .setDescription("**Year:** " + movie.Year + "\n" +
                                    "**Director(s):** " + movie.Director + "\n" +
                                    "**Plot:** " + movie.Plot)
                            .setImage(movie.Poster)
                            .setColor(BotColors.SUCCESS)
                            .setFooter("Originally Requested by: " + Main.getBotApi().getUserById(DatabaseDataManager.getWhoRequestedWaitlistItem(movie.imdbID)).join().getDiscriminatedName())).exceptionally(ExceptionLogger.get()
                    )
            );

            // Send a message to the person who requested the movie stating it is now available on Plex
            Main.getBotApi().getUserById(DatabaseDataManager.getWhoRequestedWaitlistItem(movie.imdbID)).join().sendMessage(new EmbedBuilder()
                    .setTitle("Movie Added")
                    .setDescription("You requested the following movie be added to the Celestial Movies Plex Server. This message is to notify you that the movie is now available on Plex.\n\n" +
                            "**Title:** " + movie.Title + "\n" +
                            "**Year:** " + movie.Year + "\n" +
                            "**Director(s):** " + movie.Director + "\n" +
                            "**Plot:** " + movie.Plot)
                    .setImage(movie.Poster)
                    .setColor(BotColors.SUCCESS)
                    .setFooter("This message was sent by the Plexbot and no reply will be received to messages sent here.")
            );

            // Delete the movie from the waiting list
            deleteWaitlistItem(movie.imdbID);

            BotStatusManager.getInstance().removeProcess("Download " + movie.Title + " (" + movie.Year + ")");
        };

        // Add the task to download this movie to the bots work pool
        BotWorkPool.getInstance().submitProcess("Download " + movie.Title + " (" + movie.Year + ")",downloader);
    }
}