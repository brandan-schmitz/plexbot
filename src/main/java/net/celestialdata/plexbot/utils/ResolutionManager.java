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
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import uk.co.caprica.vlcjinfo.MediaInfo;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static net.celestialdata.plexbot.Main.scheduledExecutorService;

/**
 * A class for handling all things related to the upgrade manager
 *
 * @author Celestialdeath99
 */
public class ResolutionManager {
    /**
     * Override the default constructor. This is used to schedule the checks used to
     * check if a movie in the waitlist is now available.
     */
    public ResolutionManager() {
        scheduledExecutorService.scheduleAtFixedRate(this::runCheck, 0, 2, TimeUnit.HOURS);
    }

    // A custom movie class used for storing information about movies used in this manager
    private static class Movie {
        String id;
        int oldResolution;
        int newResolution;

        public Movie(String id, int oldResolution) {
            this.id = id;
            this.oldResolution = oldResolution;
        }
    }

    /**
     * Add a movie that can be upgraded to the database and upgradable-movies channel.
     *
     * @param movie the OmdbMovie containing the information about the movie to add
     * @param oldResolution the numerical value of the resolution of the current version of the movie on the server
     * @param newResolution the numerical value of the resolution of the version of the movie that can be downloaded
     * @param newSize the size of the new video file
     */
    public static void addUpgradableMovie(OmdbMovie movie, int oldResolution, int newResolution, String newSize) {
        MediaInfo mediaInfo = MediaInfo.mediaInfo(ConfigProvider.BOT_SETTINGS.movieDownloadFolder() + DatabaseDataManager.getMovieFilename(movie.imdbID));


        // First check to see if the movie is already listed in the upgrade list, if not add it and send the message
        if (!DatabaseDataManager.isMovieInUpgradableList(movie.imdbID)) {
            Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.upgradableMoviesChannelId()).ifPresent(
                    textChannel -> textChannel.sendMessage(new EmbedBuilder()
                            .setTitle(movie.Title)
                            .setDescription(
                                    "**Current Size:** " + mediaInfo.first("General").value("File size") + "\n" +
                                            "**New Size:** " + newSize + "\n\n" +
                                            "**ID:** " + movie.imdbID + "\n" +
                                            "**Year:** " + movie.Year + "\n" +
                                            "**Director(s):** " + movie.Director + "\n" +
                                            "**Plot:** " + movie.Plot)
                            .setImage(movie.Poster)
                            .setColor(BotColors.INFO)
                            .setFooter(
                                    newResolution >= 2160 ?
                                            "Upgradable to 4k" + " from " + oldResolution + "p" :
                                            "Upgradable to " + newResolution + "p" + " from " + oldResolution+ "p"))
                            .exceptionally(ExceptionLogger.get()
                    ).thenAccept(message -> DatabaseDataManager.addMovieToUpgradableList(movie.imdbID, newResolution, message.getId()))
            );
        }
    }

    /**
     * Check to see if movies already on the server can be upgraded to a better resolution
     */
    private void runCheck() {
        Runnable checker = () -> {
            // Create the list of movie objects that will contain a list of all movies on the server
            ArrayList<Movie> movies = new ArrayList<>();

            // Create a list of movies in the database
            for (String id : DatabaseDataManager.getAllMovieIDs()) {
                movies.add(new Movie(id, DatabaseDataManager.getMovieResolution(id)));
            }

            // Cycle through all the movies in the database to find any that can be upgraded
            int progress = 0;
            for (Movie m : movies) {
                TorrentHandler torrentHandler;
                torrentHandler = new TorrentHandler(m.id);

                progress++;
                BotStatusManager.getInstance().setResolutionManagerStatus(progress, movies.size());

                // Search YTS for the movie
                try {
                    torrentHandler.searchYts();
                } catch (NullPointerException e) {
                    continue;
                }

                // Skip to the next movie if the search failed or returned no results
                if (torrentHandler.didSearchFail()) {
                    continue;
                } else if (torrentHandler.didSearchReturnNoResults()) {
                    continue;
                }

                // Get a list of movies matching the movie id
                torrentHandler.buildMovieList();
                if (torrentHandler.didBuildMovieListFail()) {
                    continue;
                }

                // Build the list of torrent files for the movie
                torrentHandler.buildTorrentList();
                if (torrentHandler.areNoTorrentsAvailable()) {
                    continue;
                }

                // Generate the magnet link for the movie torrent file
                // Required to be here in order to determine the resolution of the available movies
                torrentHandler.generateMagnetLink();
                if (torrentHandler.isNotMagnetLink()) {
                    continue;
                }

                // Add the movie to the list of upgradable movies if the torrent has a higher resolution available
                if (m.oldResolution != 0 && torrentHandler.getTorrentQuality() > m.oldResolution) {
                    m.newResolution = torrentHandler.getTorrentQuality();
                    addUpgradableMovie(Omdb.getMovieInfo(m.id), m.oldResolution, m.newResolution, torrentHandler.getTorrentSize());
                }
            }

            // Cycle through all the movies that can be upgraded, and start a task for any that have the
            // thumbsup reaction on the message for the upgrade availability.
            for (String id : DatabaseDataManager.getAllUpgradableMovieIds()) {
                Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.upgradableMoviesChannelId())
                        .flatMap(textChannel -> textChannel.getMessageById(DatabaseDataManager.getUpgradableMovieMessageId(id)).join()
                                .getReactionByEmoji(BotEmojis.THUMBS_UP)).ifPresent(reaction -> upgradeMovie(id));
            }

            // Update the upgradable-movies channel to show when this check last finished running
            Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.upgradableMoviesChannelId())
                    .flatMap(Channel::asServerTextChannel).ifPresent(serverTextChannel -> serverTextChannel.updateTopic(
                    "Last checked: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                            .format(ZonedDateTime.now()) + " CST"));

            BotStatusManager.getInstance().removeProcess("Resolution Manager");
            BotStatusManager.getInstance().clearResolutionManagerStatus();
        };

        // Add this runnable to the bot work pool to be executed
        BotWorkPool.getInstance().submitProcess("Resolution Manager", checker);
    }

    /**
     * Download the new version of the movie.
     *
     * @param id the IMDB ID of the movie to download
     */
    private void upgradeMovie(String id) {
        OmdbMovie movie = Omdb.getMovieInfo(id);

        Runnable downloader = () -> {
            TorrentHandler torrentHandler;
            RealDebridHandler realDebridHandler;
            DownloadHandler downloadHandler;
            String magnetLink;

            // Search YTS for the movie
            torrentHandler = new TorrentHandler(movie.imdbID);
            try {
                torrentHandler.searchYts();
            } catch (NullPointerException e) {
                BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // If the search failed or if the movie was not found then cancel the download
            if (torrentHandler.didSearchFail()) {
                BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
                return;
            } else if (torrentHandler.didSearchReturnNoResults()) {
                BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Get a list of movies matching the movie id
            torrentHandler.buildMovieList();
            if (torrentHandler.didBuildMovieListFail()) {
                BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Build the list of torrent files for the movie
            torrentHandler.buildTorrentList();
            if (torrentHandler.areNoTorrentsAvailable()) {
                BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Generate the magnet link for the movie torrent file
            torrentHandler.generateMagnetLink();
            if (torrentHandler.isNotMagnetLink()) {
                BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Add the torrent to real-debrid through its magnet link
            magnetLink = torrentHandler.getMagnetLink();
            realDebridHandler = new RealDebridHandler(magnetLink);
            realDebridHandler.addMagnet();
            if (realDebridHandler.didMagnetAdditionFail()) {
                BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Get the information about the torrent on real-debrid
            realDebridHandler.getTorrentInformation();
            if (realDebridHandler.didTorrentInfoError()) {
                realDebridHandler.deleteTorrent();
                BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Select the proper files to download on real-debrid
            realDebridHandler.selectTorrentFiles();
            if (realDebridHandler.didSelectOperationFail()) {
                realDebridHandler.deleteTorrent();
                BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Wait for real-debrid to have the movie ready for downloading
            synchronized (realDebridHandler.lock) {
                while (realDebridHandler.isNotReadyForDownload()) {
                    try {
                        realDebridHandler.lock.wait();
                    } catch (InterruptedException e) {
                        BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
                        return;
                    }
                }
            }

            // Unrestrict the download link on real-debrid to allow the bot to download it
            realDebridHandler.unrestrictLinks();
            if (realDebridHandler.didUnrestrictOperationFail()) {
                realDebridHandler.deleteTorrent();
                BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Get the download link and create the DownloadHandler for the movie
            String downloadLink = realDebridHandler.getDownloadLink();
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
                        BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
                        return;
                    }
                }
            }

            // Exit if the download failed, cleaning up the torrent on real-debrid in the process
            if (downloadHandler.didDownloadFail()) {
                // TODO: Delete any portion of the file that was downloaded before the failure occurred
                realDebridHandler.deleteTorrent();
                BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Attempt to delete the old movie files
            File oldVersion = new File(ConfigProvider.BOT_SETTINGS.movieDownloadFolder() +
                    DatabaseDataManager.getMovieFilename(movie.imdbID));
            if (!oldVersion.delete()) {
                realDebridHandler.deleteTorrent();
                BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Rename the movie file using the proper extension so that Plex will find it
            // Exit if the rename process failed, and clean up the torrent on real-debrid in the process
            if (!downloadHandler.renameFile(realDebridHandler.getExtension())) {
                // TODO: Delete the file that was downloaded but failed to be renamed
                realDebridHandler.deleteTorrent();
                BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
                return;
            }

            // Delete the torrent file from real-debrid
            realDebridHandler.deleteTorrent();

            // Update the resolution of the movie in the database
            DatabaseDataManager.updateMovieResolution(movie.imdbID, torrentHandler.getTorrentQuality());

            // Update the filename of the movie in the database
            DatabaseDataManager.updateMovieFilename(movie.imdbID, downloadHandler.getFilename() + realDebridHandler.getExtension());

            // Use the default movie poster if one was not found on IMDB
            if (movie.Poster.equalsIgnoreCase("N/A"))  {
                movie.Poster = ConfigProvider.BOT_SETTINGS.noPosterImageUrl();
            }

            // Send a message to the upgraded-movies notification channel
            Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.upgradedMoviesChannelId()).ifPresent(textChannel ->
                    textChannel.sendMessage(new EmbedBuilder()
                            .setTitle(movie.Title)
                            .setDescription("**Year:** " + movie.Year + "\n" +
                                    "**Director(s):** " + movie.Director + "\n" +
                                    "**Plot:** " + movie.Plot)
                            .setImage(movie.Poster)
                            .setColor(BotColors.SUCCESS)
                            .setFooter(torrentHandler.getTorrentQuality() >= 2160 ?
                                    "Upgraded to 4k" :
                                    "Upgraded to " + torrentHandler.getTorrentQuality() + "p")
                    )
            );

            // Delete the message in the upgradable-movies channel
            Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.upgradableMoviesChannelId()).ifPresent(textChannel ->
                    textChannel.getMessageById(DatabaseDataManager.getUpgradableMovieMessageId(movie.imdbID)).join().delete().exceptionally(ExceptionLogger.get()));

            // Delete the movie from the list of upgradable movies
            DatabaseDataManager.removeMovieFromUpgradableList(movie.imdbID);

            BotStatusManager.getInstance().removeProcess("Upgrade " + movie.Title + " (" + movie.Year + ")");
        };

        // Add the task to download this movie to the bots work pool
        BotWorkPool.getInstance().submitProcess("Upgrade " + movie.Title + " (" + movie.Year + ")", downloader);
    }
}