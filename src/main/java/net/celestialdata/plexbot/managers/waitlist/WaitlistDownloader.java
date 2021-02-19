package net.celestialdata.plexbot.managers.waitlist;

import net.celestialdata.plexbot.BotConfig;
import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.client.ApiException;
import net.celestialdata.plexbot.client.BotClient;
import net.celestialdata.plexbot.client.model.*;
import net.celestialdata.plexbot.database.DbOperations;
import net.celestialdata.plexbot.database.builders.MovieBuilder;
import net.celestialdata.plexbot.database.models.WaitlistItem;
import net.celestialdata.plexbot.managers.DownloadManager;
import net.celestialdata.plexbot.utils.BotColors;
import net.celestialdata.plexbot.utils.CustomRunnable;
import net.celestialdata.plexbot.workhandlers.TorrentHandler;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

public class WaitlistDownloader implements CustomRunnable {
    private final TorrentHandler torrentHandler;
    private final OmdbItem movieInfo;
    private final Object rdbLock = new Object();

    public WaitlistDownloader(TorrentHandler torrentHandler, OmdbItem movieInfo) {
        this.torrentHandler = torrentHandler;
        this.movieInfo = movieInfo;
    }

    @Override
    public String taskName() {
        return "Download " + movieInfo.getTitle() + " (" + movieInfo.getYear() + ")";
    }

    @Override
    public boolean cancelOnDuplicate() {
        return true;
    }

    @SuppressWarnings({"DuplicatedCode", "RedundantSuppression"})
    @Override
    public void run() {
        // Configure task to run the endTask method if there was an error
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> endTask(e));

        DownloadManager downloadManager;
        String magnetLink;

        // Get a list of movies matching the movie id
        torrentHandler.buildMovieList();
        if (torrentHandler.didBuildMovieListFail()) {
            WaitlistUtilities.updateMessage(movieInfo);
            endTask();
            return;
        }

        // Build the list of torrent files for the movie
        torrentHandler.buildTorrentList();
        if (torrentHandler.areNoTorrentsAvailable()) {
            WaitlistUtilities.updateMessage(movieInfo);
            endTask();
            return;
        }

        // Generate the magnet link for the movie torrent file
        torrentHandler.generateMagnetLink();
        if (torrentHandler.isNotMagnetLink()) {
            WaitlistUtilities.updateMessage(movieInfo);
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
            WaitlistUtilities.updateMessage(movieInfo);
            endTask(e);
            return;
        }

        // Get the torrent file information
        RdbTorrentInfo rdbTorrentInfo;
        try {
            rdbTorrentInfo = BotClient.getInstance().rdbApi.getTorrentInfo(rdbMagnetLink.getId());
        } catch (Exception e) {
            WaitlistUtilities.updateMessage(movieInfo);
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
                    WaitlistUtilities.updateMessage(movieInfo);
                    endTask();
                    return;
                }
            }
        } else {
            WaitlistUtilities.updateMessage(movieInfo);
            endTask();
            return;
        }

        // Select the proper files on RDB
        try {
            BotClient.getInstance().rdbApi.selectTorrentFiles(rdbTorrentInfo.getId(), fileToSelect);
        } catch (Exception e) {
            WaitlistUtilities.updateMessage(movieInfo);
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
                WaitlistUtilities.updateMessage(movieInfo);
                endTask();
                return;
            }
        } catch (InterruptedException | ApiException e) {
            WaitlistUtilities.updateMessage(movieInfo);
            endTask(e);
            return;
        }

        // Unrestrict the download link on real-debrid to allow the bot to download it
        // Make the link unrestricted
        RdbUnrestrictedLink unrestrictedLink;
        try {
            if (rdbTorrentInfo.getLinks() != null) {
                unrestrictedLink = BotClient.getInstance().rdbApi.unrestrictLink(rdbTorrentInfo.getLinks().get(0));
            } else {
                WaitlistUtilities.updateMessage(movieInfo);
                endTask();
                return;
            }
        } catch (Exception e) {
            try {
                BotClient.getInstance().rdbApi.deleteTorrent(rdbTorrentInfo.getId());
            } catch (ApiException e1) {
                WaitlistUtilities.updateMessage(movieInfo);
                endTask(e1);
                return;
            }
            WaitlistUtilities.updateMessage(movieInfo);
            endTask(e);
            return;
        }

        // Get the download link and create the DownloadHandler for the movie
        String downloadLink;
        if (unrestrictedLink.getDownload() != null) {
            downloadLink = unrestrictedLink.getDownload().toString();
        } else {
            WaitlistUtilities.updateMessage(movieInfo);
            endTask();
            return;
        }
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
                    WaitlistUtilities.updateMessage(movieInfo);
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
                WaitlistUtilities.updateMessage(movieInfo);
                endTask(e);
                return;
            }
            WaitlistUtilities.updateMessage(movieInfo);
            endTask();
            return;
        }

        // Process the movie file
        synchronized (downloadManager.lock) {
            while (downloadManager.isProcessing()) {
                try {
                    downloadManager.lock.wait();
                } catch (InterruptedException e) {
                    WaitlistUtilities.updateMessage(movieInfo);
                    endTask(e);
                    return;
                }
            }
        }

        if (downloadManager.didProcessingFail()) {
            if (!downloadManager.isFileServerMounted()) {
                Main.getBotApi().getUserById(BotConfig.getInstance().adminUserId()).join().sendMessage(
                        new EmbedBuilder()
                                .setTitle("Bot Error")
                                .setDescription("The bot attempted to download a movie, however the NFS server is not mounted. " +
                                        "Please mount the server and manually finish processing the file and add it to the database.\n")
                                .addField("Process Command:", "```bash\n" +
                                        "mv " + BotConfig.getInstance().tempFolder() + "'" + downloadManager.getFilename() + ".pbdownload' " +
                                        BotConfig.getInstance().movieFolder() + "'" + downloadManager.getFilename() + fileExtension + "'\n```")
                                .addField("SQL Scripts:", "```sql\n" +
                                        "INSERT INTO `Movies` (`movie_id`, `movie_filename`, `movie_resolution`, `movie_title`, `movie_year`) VALUES (" +
                                        "'" + movieInfo.getImdbID() + "', " +
                                        "'" + downloadManager.getFilename() + fileExtension + "', " +
                                        "'" + torrentHandler.getTorrentQuality() + "', " +
                                        "'" + movieInfo.getTitle() + "', " +
                                        "'" + movieInfo.getYear() + "');\n\n" +
                                        "DELETE FROM `Waitinglist` WHERE `Waitinglist`.`item_id` = '" + movieInfo.getImdbID() + "';\n```")
                                .setColor(BotColors.ERROR)
                                .setFooter("This message was sent by the Plexbot and no reply will be received to messages sent here.")
                );
            }
            WaitlistUtilities.updateMessage(movieInfo);
            endTask();
            return;
        }

        // Delete the torrent from RealDebrid
        try {
            BotClient.getInstance().rdbApi.deleteTorrent(rdbTorrentInfo.getId());
        } catch (ApiException ignored) {
            WaitlistUtilities.updateMessage(movieInfo);
        }

        // Add the movie to the database
        DbOperations.saveObject(new MovieBuilder()
                .withId(movieInfo.getImdbID())
                .withTitle(movieInfo.getTitle())
                .withYear(movieInfo.getYear())
                .withResolution(torrentHandler.getTorrentQuality())
                .withFilename(downloadManager.getFilename() + fileExtension)
                .withExtension(fileExtension.replace(".", ""))
                .withFolderName(downloadManager.getFilename())
                .build()
        );

        // Trigger a refresh of the media libraries on the plex server
        try {
            BotClient.getInstance().plexApi.refreshLibraries();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send a message to the new-movies notification channel stating the movie is now available on Plex
        Main.getBotApi().getTextChannelById(BotConfig.getInstance().newMoviesChannelId()).ifPresent(textChannel ->
                textChannel.sendMessage(new EmbedBuilder()
                        .setTitle(movieInfo.getTitle())
                        .setDescription("**Year:** " + movieInfo.getYear() + "\n" +
                                "**Director(s):** " + movieInfo.getDirector() + "\n" +
                                "**Plot:** " + movieInfo.getPlot())
                        .setImage(movieInfo.getPoster())
                        .setColor(BotColors.SUCCESS))
                        .exceptionally(ExceptionLogger.get()
                        )
        );

        // Send a message to the person who requested the movie stating it is now available on Plex
        Main.getBotApi().getUserById(DbOperations.waitlistItemOps.getItemById(movieInfo.getImdbID()).getRequestedBy().getId()).join().sendMessage(new EmbedBuilder()
                .setTitle("Movie Added")
                .setDescription("You requested the following movie be added to the Celestial Movies Plex Server. This message is to notify you that the movie is now available on Plex.\n\n" +
                        "**Title:** " + movieInfo.getTitle() + "\n" +
                        "**Year:** " + movieInfo.getYear() + "\n" +
                        "**Director(s):** " + movieInfo.getDirector() + "\n" +
                        "**Plot:** " + movieInfo.getPlot())
                .setImage(movieInfo.getPoster())
                .setColor(BotColors.SUCCESS)
                .setFooter("This message was sent by the Plexbot and no reply will be received to messages sent here.")
        );

        // Delete the movie from the waiting list
        DbOperations.deleteItem(WaitlistItem.class, movieInfo.getImdbID());

        // Remove the task info from the bot status manager
        endTask();
    }
}