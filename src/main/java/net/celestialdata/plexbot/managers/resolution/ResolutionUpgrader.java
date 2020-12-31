package net.celestialdata.plexbot.managers.resolution;

import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.client.ApiException;
import net.celestialdata.plexbot.client.BotClient;
import net.celestialdata.plexbot.client.model.*;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.database.DbOperations;
import net.celestialdata.plexbot.database.builders.MovieBuilder;
import net.celestialdata.plexbot.database.models.UpgradeItem;
import net.celestialdata.plexbot.managers.DownloadManager;
import net.celestialdata.plexbot.utils.BotColors;
import net.celestialdata.plexbot.utils.CustomRunnable;
import net.celestialdata.plexbot.workhandlers.TorrentHandler;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.io.File;

public class ResolutionUpgrader implements CustomRunnable {
    private final OmdbMovieInfo movieInfo;
    private final Object rdbLock = new Object();

    public ResolutionUpgrader(OmdbMovieInfo movieInfo) {
        this.movieInfo = movieInfo;
    }

    @Override
    public String taskName() {
        return "Upgrade " + movieInfo.getTitle() + " (" + movieInfo.getYear() + ")";
    }

    @Override
    public boolean cancelOnDuplicate() {
        return true;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void run() {
        // Configure task to run the endTask method if there was an error
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> endTask(e));

        TorrentHandler torrentHandler;
        DownloadManager downloadManager;
        String magnetLink;

        // Setup the torrent handler
        torrentHandler = new TorrentHandler(movieInfo.getImdbID());

        // Search YTS for the movie
        try {
            torrentHandler.searchYts();
        } catch (Exception e) {
            endTask();
            return;
        }

        // If the movie was not found then cancel the download
        if (torrentHandler.didSearchReturnNoResults()) {
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

        // Get the magnet link
        magnetLink = torrentHandler.getMagnetLink();

        // Add the magnet link to real-debrid
        RdbMagnetLink rdbMagnetLink;
        try {
            rdbMagnetLink = BotClient.getInstance().rdbApi.addMagnet(magnetLink);
        } catch (Exception e) {
            endTask(e);
            return;
        }

        // Get the torrent file information
        RdbTorrentInfo rdbTorrentInfo;
        try {
            rdbTorrentInfo = BotClient.getInstance().rdbApi.getTorrentInfo(rdbMagnetLink.getId());
        } catch (Exception e) {
            endTask(e);
            return;
        }

        // Select the files to download
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

        // Select the proper files on RDB
        try {
            BotClient.getInstance().rdbApi.selectTorrentFiles(rdbTorrentInfo.getId(), fileToSelect);
        } catch (Exception e) {
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
                endTask();
                return;
            }
        } catch (InterruptedException | ApiException e) {
            endTask(e);
            return;
        }

        // Unrestrict the download link on real-debrid to allow the bot to download it
        // Make the link unrestricted
        RdbUnrestrictedLink unrestrictedLink;
        try {
            unrestrictedLink = BotClient.getInstance().rdbApi.unrestrictLink(rdbTorrentInfo.getLinks().get(0));
        } catch (Exception e) {
            try {
                BotClient.getInstance().rdbApi.deleteTorrent(rdbTorrentInfo.getId());
            } catch (ApiException e1) {
                endTask(e1);
                return;
            }
            endTask(e);
            return;
        }

        // Get the download link and create the DownloadHandler for the movie
        String downloadLink = unrestrictedLink.getDownload();
        downloadManager = new DownloadManager(downloadLink, movieInfo, fileExtension);

        // Start the download process
        downloadManager.run();

        // Wait for the download to finish downloading and processing the movie file
        synchronized (downloadManager.lock) {
            // Download the movie file
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
            try {
                BotClient.getInstance().rdbApi.deleteTorrent(rdbTorrentInfo.getId());
            } catch (Exception e) {
                endTask(e);
                return;
            }
            endTask();
            return;
        }

        // Process the movie file
        synchronized (downloadManager.lock) {
            while (downloadManager.isProcessing()) {
                try {
                    downloadManager.lock.wait();
                } catch (InterruptedException e) {
                    endTask(e);
                    return;
                }
            }
        }

        if (downloadManager.didProcessingFail()) {
            if (!downloadManager.isFileServerMounted()) {
                Main.getBotApi().getUserById(ConfigProvider.BOT_SETTINGS.adminUserId()).join().sendMessage(
                        new EmbedBuilder()
                                .setTitle("Bot Error")
                                .setDescription("The bot attempted to download a movie, however the NFS server is not mounted. " +
                                        "Please mount the server and manually finish processing the file and add it to the database.\n")
                                .addField("Process Commands:", "```bash\n" +
                                        "mv " + ConfigProvider.BOT_SETTINGS.tempFolder() + "'" + downloadManager.getFilename() + ".pbdownload' " +
                                        ConfigProvider.BOT_SETTINGS.movieFolder() + "'" + downloadManager.getFilename() + fileExtension + "'\n\n" +
                                        "rm " + ConfigProvider.BOT_SETTINGS.movieFolder() + "'" + DbOperations.movieOps.getMovieById(movieInfo.getImdbID()).getFilename() +"'\n```")
                                .addField("SQL Scripts:", "```sql\n" +
                                        "UPDATE `Movies` SET `movie_filename` = '" + downloadManager.getFilename() + fileExtension + "', " +
                                        "`movie_resolution` = '" + movieInfo.getYear() + "', " +
                                        "`movie_title` = '" + movieInfo.getTitle() + "', " +
                                        "`movie_year` = '" + movieInfo.getYear() + "' " +
                                        "WHERE `Movies`.`movie_id` = '" + movieInfo.getImdbID() + "';\n\n" +
                                        "DELETE FROM `Upgrades` WHERE `Upgrades`.`upgrade_movie` = '" + movieInfo.getImdbID() + "';\n```")
                                .setColor(BotColors.ERROR)
                                .setFooter("This message was sent by the Plexbot and no reply will be received to messages sent here.")
                );
            }
            endTask();
            return;
        }

        // Attempt to delete the old movie files
        File oldVersion = new File(ConfigProvider.BOT_SETTINGS.movieFolder() +
                DbOperations.movieOps.getMovieById(movieInfo.getImdbID()).getFilename());
        if (!oldVersion.delete()) {
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
        } catch (ApiException ignored) {
        }

        // Update the movie in the database
        DbOperations.saveObject(new MovieBuilder()
                .withId(movieInfo.getImdbID())
                .withTitle(movieInfo.getTitle())
                .withYear(movieInfo.getYear())
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

        // Send a message to the upgraded-movies notification channel
        Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.upgradedMoviesChannelId()).ifPresent(textChannel ->
                textChannel.sendMessage(new EmbedBuilder()
                        .setTitle(movieInfo.getTitle())
                        .setDescription("**Year:** " + movieInfo.getYear() + "\n" +
                                "**Director(s):** " + movieInfo.getDirector() + "\n" +
                                "**Plot:** " + movieInfo.getPlot())
                        .setImage(movieInfo.getPoster())
                        .setColor(BotColors.SUCCESS)
                        .setFooter(torrentHandler.getTorrentQuality() >= 2160 ?
                                "Upgraded to 4k" :
                                "Upgraded to " + torrentHandler.getTorrentQuality() + "p")
                )
        );

        // Delete the movie from the list of upgradable movies
        DbOperations.deleteItem(UpgradeItem.class, movieInfo.getImdbID());

        // Remove the task info from the bot status manager
        endTask();
    }
}