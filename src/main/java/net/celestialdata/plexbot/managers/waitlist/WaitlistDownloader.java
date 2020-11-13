package net.celestialdata.plexbot.managers.waitlist;

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

public class WaitlistDownloader implements CustomRunnable {
    private final TorrentHandler torrentHandler;
    private final OmdbMovie movie;

    public WaitlistDownloader(TorrentHandler torrentHandler, OmdbMovie movie) {
        this.torrentHandler = torrentHandler;
        this.movie = movie;
    }

    @Override
    public String taskName() {
        return "Download " + movie.Title + " (" + movie.Year + ")";
    }

    @Override
    public void run() {
        RealDebridHandler realDebridHandler;
        DownloadManager downloadManager;
        String magnetLink;
        String downloadLink;

        // Get a list of movies matching the movie id
        torrentHandler.buildMovieList();
        if (torrentHandler.didBuildMovieListFail()) {
            WaitlistManager.updateMessage(movie);
            endTask();
            return;
        }

        // Build the list of torrent files for the movie
        torrentHandler.buildTorrentList();
        if (torrentHandler.areNoTorrentsAvailable()) {
            WaitlistManager.updateMessage(movie);
            endTask();
            return;
        }

        // Generate the magnet link for the movie torrent file
        torrentHandler.generateMagnetLink();
        if (torrentHandler.isNotMagnetLink()) {
            WaitlistManager.updateMessage(movie);
            endTask();
            return;
        }

        // Add the torrent to real-debrid through its magnet link
        magnetLink = torrentHandler.getMagnetLink();
        realDebridHandler = new RealDebridHandler(magnetLink);
        realDebridHandler.addMagnet();
        if (realDebridHandler.didMagnetAdditionFail()) {
            WaitlistManager.updateMessage(movie);
            endTask();
            return;
        }

        // Get the information about the torrent on real-debrid
        realDebridHandler.getTorrentInformation();
        if (realDebridHandler.didTorrentInfoError()) {
            realDebridHandler.deleteTorrent();
            WaitlistManager.updateMessage(movie);
            endTask();
            return;
        }

        // Select the proper files to download on real-debrid
        realDebridHandler.selectTorrentFiles();
        if (realDebridHandler.didSelectOperationFail()) {
            realDebridHandler.deleteTorrent();
            WaitlistManager.updateMessage(movie);
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
            WaitlistManager.updateMessage(movie);
            endTask();
            return;
        }

        // Get the download link and create the DownloadHandler for the movie
        downloadLink = realDebridHandler.getDownloadLink();
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
            WaitlistManager.updateMessage(movie);
            endTask();
            return;
        }

        // Rename the movie file using the proper extension so that Plex will find it
        // Exit if the rename process failed, and clean up the torrent on real-debrid in the process
        if (!downloadManager.renameFile(realDebridHandler.getExtension())) {
            // TODO: Delete the file that was downloaded but failed to be renamed
            realDebridHandler.deleteTorrent();
            WaitlistManager.updateMessage(movie);
            endTask();
            return;
        }

        // Delete the torrent file from real-debrid
        realDebridHandler.deleteTorrent();

        // Add the movie to the database
        DatabaseDataManager.addMovie(movie.imdbID, movie.Title, movie.Year, torrentHandler.getTorrentQuality(), downloadManager.getFilename());

        // Send a message to the new-movies notification channel stating the movie is now available on Plex
        Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.newMoviesChannelId()).ifPresent(textChannel ->
                textChannel.sendMessage(new EmbedBuilder()
                        .setTitle(movie.Title)
                        .setDescription("**Year:** " + movie.Year + "\n" +
                                "**Director(s):** " + movie.Director + "\n" +
                                "**Plot:** " + movie.Plot)
                        .setImage(movie.Poster)
                        .setColor(BotColors.SUCCESS))
                        .exceptionally(ExceptionLogger.get()
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
        WaitlistManager.deleteWaitlistItem(movie.imdbID);

        // Remove the task info from the bot status manager
        endTask();
    }
}