package net.celestialdata.plexbot.managers.resolution;

import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.apis.omdb.objects.movie.OmdbMovie;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.database.DatabaseDataManager;
import net.celestialdata.plexbot.utils.BotColors;
import net.celestialdata.plexbot.utils.CustomRunnable;
import net.celestialdata.plexbot.managers.DownloadManager;
import net.celestialdata.plexbot.workhandlers.RealDebridHandler;
import net.celestialdata.plexbot.workhandlers.TorrentHandler;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import java.io.File;

public class ResolutionUpgrader implements CustomRunnable {
    private final OmdbMovie movie;

    public ResolutionUpgrader(OmdbMovie movie) {
        this.movie = movie;
    }

    @Override
    public String taskName() {
        return "Upgrade " + movie.Title + " (" + movie.Year + ")";
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void run() {
        // Configure task to run the endTask method if there was an error
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> endTask(e));

        TorrentHandler torrentHandler;
        RealDebridHandler realDebridHandler;
        DownloadManager downloadManager;
        String magnetLink;

        // Search YTS for the movie
        torrentHandler = new TorrentHandler(movie.imdbID);

        try {
            torrentHandler.searchYts();
        } catch (NullPointerException e) {
            endTask(e);
            return;
        }

        // If the search failed or if the movie was not found then cancel the download
        if (torrentHandler.didSearchFail()) {
            endTask();
            return;
        } else if (torrentHandler.didSearchReturnNoResults()) {
            endTask();
            return;
        }

        // Get a list of movies matching the movie id
        torrentHandler.buildMovieList();
        if (torrentHandler.didBuildMovieListFail()) {
            endTask();
            return;
        }

        // Build the list of torrent files for the movie
        torrentHandler.buildTorrentList();
        if (torrentHandler.areNoTorrentsAvailable()) {
            endTask();
            return;
        }

        // Generate the magnet link for the movie torrent file
        torrentHandler.generateMagnetLink();
        if (torrentHandler.isNotMagnetLink()) {
            endTask();
            return;
        }

        // Add the torrent to real-debrid through its magnet link
        magnetLink = torrentHandler.getMagnetLink();
        realDebridHandler = new RealDebridHandler(magnetLink);
        realDebridHandler.addMagnet();
        if (realDebridHandler.didMagnetAdditionFail()) {
            endTask();
            return;
        }

        // Get the information about the torrent on real-debrid
        realDebridHandler.getTorrentInformation();
        if (realDebridHandler.didTorrentInfoError()) {
            realDebridHandler.deleteTorrent();
            endTask();
            return;
        }

        // Select the proper files to download on real-debrid
        realDebridHandler.selectTorrentFiles();
        if (realDebridHandler.didSelectOperationFail()) {
            realDebridHandler.deleteTorrent();
            endTask();
            return;
        }

        // Wait for real-debrid to have the movie ready for downloading
        synchronized (realDebridHandler.lock) {
            while (realDebridHandler.isNotReadyForDownload()) {
                try {
                    realDebridHandler.lock.wait();
                } catch (InterruptedException e) {
                    endTask(e);
                    return;
                }
            }
        }

        // Unrestrict the download link on real-debrid to allow the bot to download it
        realDebridHandler.unrestrictLinks();
        if (realDebridHandler.didUnrestrictOperationFail()) {
            realDebridHandler.deleteTorrent();
            endTask();
            return;
        }

        // Get the download link and create the DownloadHandler for the movie
        String downloadLink = realDebridHandler.getDownloadLink();
        downloadManager = new DownloadManager(downloadLink, movie);

        // Start the download process
        downloadManager.run();

        // Wait for the download to finish downloading the movie file
        synchronized (downloadManager.lock) {
            while (downloadManager.isDownloading()) {
                try {
                    downloadManager.lock.wait();
                } catch (InterruptedException e) {
                    endTask(e);
                    return;
                }
            }
        }

        // Exit if the download failed, cleaning up the torrent on real-debrid in the process
        if (downloadManager.didDownloadFail()) {
            // TODO: Delete any portion of the file that was downloaded before the failure occurred
            realDebridHandler.deleteTorrent();
            endTask();
            return;
        }

        // Attempt to delete the old movie files
        File oldVersion = new File(ConfigProvider.BOT_SETTINGS.movieDownloadFolder() +
                DatabaseDataManager.getMovieFilename(movie.imdbID));
        if (!oldVersion.delete()) {
            realDebridHandler.deleteTorrent();
            endTask();
            return;
        }

        // Rename the movie file using the proper extension so that Plex will find it
        // Exit if the rename process failed, and clean up the torrent on real-debrid in the process
        if (!downloadManager.renameFile(realDebridHandler.getExtension())) {
            // TODO: Delete the file that was downloaded but failed to be renamed
            realDebridHandler.deleteTorrent();
            endTask();
            return;
        }

        // Delete the torrent file from real-debrid
        realDebridHandler.deleteTorrent();

        // Update the resolution of the movie in the database
        DatabaseDataManager.updateMovieResolution(movie.imdbID, torrentHandler.getTorrentQuality());

        // Update the filename of the movie in the database
        DatabaseDataManager.updateMovieFilename(movie.imdbID, downloadManager.getFilename() + realDebridHandler.getExtension());

        // Use the default movie poster if one was not found on IMDB
        if (movie.Poster.equalsIgnoreCase("N/A")) {
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

        // Remove the task info from the bot status manager
        endTask();
    }
}