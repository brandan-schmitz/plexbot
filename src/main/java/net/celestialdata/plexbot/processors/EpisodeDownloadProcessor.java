package net.celestialdata.plexbot.processors;

import lombok.SneakyThrows;
import net.celestialdata.plexbot.clients.models.rdb.RdbTorrent;
import net.celestialdata.plexbot.clients.models.rdb.RdbTorrentFile;
import net.celestialdata.plexbot.clients.models.rdb.enums.RdbTorrentStatusEnum;
import net.celestialdata.plexbot.clients.models.sg.enums.SgStatus;
import net.celestialdata.plexbot.clients.models.tmdb.TmdbSourceIdType;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbEpisode;
import net.celestialdata.plexbot.clients.services.PlexService;
import net.celestialdata.plexbot.clients.services.RdbService;
import net.celestialdata.plexbot.clients.services.TmdbService;
import net.celestialdata.plexbot.clients.services.TvdbService;
import net.celestialdata.plexbot.clients.utilities.SgServiceWrapper;
import net.celestialdata.plexbot.db.daos.*;
import net.celestialdata.plexbot.db.entities.DownloadQueueItem;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.enumerators.FileType;
import net.celestialdata.plexbot.utilities.BotProcess;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import org.jboss.logging.Logger;

import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Dependent
public class EpisodeDownloadProcessor extends BotProcess implements Runnable {
    private final Logger logger = Logger.getLogger(EpisodeDownloadProcessor.class);
    DecimalFormat decimalFormatter = new DecimalFormat("#0.00");

    @ConfigProperty(name = "BotSettings.ownerID")
    Long botOwner;

    @ConfigProperty(name = "ChannelSettings.newEpisodeNotificationChannel")
    Long newEpisodeNotificationChannel;

    @ConfigProperty(name = "FolderSettings.tvFolder")
    String tvFolder;

    @ConfigProperty(name = "FolderSettings.tempFolder")
    String tempFolder;

    @ConfigProperty(name = "SickgearSettings.torrentFolder")
    String torrentFolder;

    @Inject
    DownloadQueueItemDao downloadQueueItemDao;

    @Inject
    DownloadHistoryItemDao downloadHistoryItemDao;

    @Inject
    @RestClient
    RdbService rdbService;

    @Inject
    @RestClient
    TvdbService tvdbService;

    @Inject
    @RestClient
    TmdbService tmdbService;

    @Inject
    SgServiceWrapper sgServiceWrapper;

    @Inject
    @RestClient
    PlexService plexService;

    @Inject
    DiscordApi discordApi;

    @Inject
    FileUtilities fileUtilities;

    @Inject
    ShowDao showDao;

    @Inject
    EpisodeDao episodeDao;

    @Inject
    EpisodeSubtitleDao episodeSubtitleDao;

    @Inject
    MessageFormatter messageFormatter;

    @SuppressWarnings("DuplicatedCode")
    @SneakyThrows
    @Nullable
    private RdbTorrent addTorrent(DownloadQueueItem queueItem) {
        // Create the return object
        RdbTorrent torrentInformation;

        File queueItemFile = new File(torrentFolder + queueItem.filename);
        if (queueItem.filetype.equals("magnet")) {
            // Get the magnet link from the file
            var fileContents = FileUtils.readLines(queueItemFile, "UTF-8");

            // Add the magnet to real-debrid
            var rdbAddedTorrent = rdbService.addMagnet(fileContents.get(0));

            // Get the torrent information
            torrentInformation = rdbService.getTorrentInfo(rdbAddedTorrent.id);

            // Make sure the magnet was added successfully and that it has finished its conversion
            if (torrentInformation.status == RdbTorrentStatusEnum.ERROR || torrentInformation.status == RdbTorrentStatusEnum.MAGNET_ERROR) {
                rdbService.deleteTorrent(rdbAddedTorrent.id);
                return null;
            } else if (torrentInformation.status == RdbTorrentStatusEnum.MAGNET_CONVERSION) {
                LocalDateTime lastCheck = LocalDateTime.now();

                // Wait for the magnet link to get converted, only check every 3 seconds to avoid overloading the API
                while (torrentInformation.status == RdbTorrentStatusEnum.MAGNET_CONVERSION) {
                    if (lastCheck.plus(3, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        torrentInformation = rdbService.getTorrentInfo(rdbAddedTorrent.id);
                        lastCheck = LocalDateTime.now();
                    }
                }
            }
        } else {
            // Load the file
            File torrentFile = new File(torrentFolder + queueItem.filename);

            // Add the torrent file to real-debrid
            var rdbAddedTorrent = rdbService.addTorrent(new FileInputStream(torrentFile));

            // Get the torrent information
            torrentInformation = rdbService.getTorrentInfo(rdbAddedTorrent.id);

            // Make sure the torrent was added successfully and that it has finished its conversion
            if (torrentInformation.status == RdbTorrentStatusEnum.ERROR || torrentInformation.status == RdbTorrentStatusEnum.MAGNET_ERROR) {
                rdbService.deleteTorrent(rdbAddedTorrent.id);
                return null;
            } else if (torrentInformation.status == RdbTorrentStatusEnum.MAGNET_CONVERSION) {
                LocalDateTime lastCheck = LocalDateTime.now();

                // Wait for the magnet link to get converted, only check every 3 seconds to avoid overloading the API
                while (torrentInformation.status == RdbTorrentStatusEnum.MAGNET_CONVERSION) {
                    if (lastCheck.plus(3, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        torrentInformation = rdbService.getTorrentInfo(rdbAddedTorrent.id);
                        lastCheck = LocalDateTime.now();
                    }
                }
            }
        }

        // Return the information about the torrent
        return torrentInformation;
    }

    @SuppressWarnings("DuplicatedCode")
    @SneakyThrows
    @Override
    public void run() {
        // Fetch the next item to download
        var queueItem = downloadQueueItemDao.getNext();

        // If it returned null, then there was nothing to download and this should exit
        if (queueItem == null) {
            return;
        }

        // Create the real-debrid torrent information object so if a failure occurs we can remove the torrent from real-debrid
        RdbTorrent torrentInformation = null;

        // Create the variable that will store the filename as it will be in the temp folder during download
        var tempDownloadFilename = "";

        try {
            // Configure the process name as it will appear in the bot process manager
            configureProcess("Download " + queueItem.showId + " s" + queueItem.seasonNumber + "e" + queueItem.episodeNumber + " - loading...");

            // Mark the item as downloading since the process has been started
            queueItem = downloadQueueItemDao.updateStatus(queueItem.id, "downloading");

            // Fetch information about this show
            var showInfo = tvdbService.getSeries(queueItem.showId);

            // Verify that it was able to fetch information about the show
            if (!showInfo.status.equals("success")) {
                // Log the issue to the log
                logger.error("An error occurred while trying to fetch information on TVDB about this torrent file's show: " + queueItem.filename);

                // Manage the failure status of the queue item
                handleFailed(queueItem);

                // Stop the process
                endProcess();
                return;
            }

            // Fetch a list of episodes in the season this episode is in
            var seriesEpisodeList = tvdbService.getSeriesEpisodes(queueItem.showId);

            // Verify that it was able to get a list of episodes for the season
            if (!seriesEpisodeList.status.equalsIgnoreCase("success")) {
                // Log the issue to the log
                logger.error("An error occurred while trying to fetch information on TVDB about this torrent file's season episodes: " + queueItem.filename);

                // Manage the failure status of the queue item
                handleFailed(queueItem);

                // Stop the process
                endProcess();
                return;
            }

            // Locate the proper episode from the list of episodes obtained above
            TvdbEpisode episodeInformation = null;
            for (TvdbEpisode episode : seriesEpisodeList.seriesEpisodes.episodes) {
                if (episode.number == queueItem.episodeNumber && episode.seasonNumber == queueItem.seasonNumber) {
                    episodeInformation = episode;
                }
            }

            // Verify that an episode was loaded
            if (episodeInformation == null) {
                // Log the issue to the log
                logger.error("Unable to locate the proper episode on TVDB for the following torrent file: " + queueItem.filename);

                // Manage the failure status of the queue item
                handleFailed(queueItem);

                // Stop the process
                endProcess();
                return;
            }

            // If the episode name is not present, check to see if TMDB has it
            if (StringUtils.isBlank(episodeInformation.name)) {
                // Attempt to locate the episode on TMDB using its TVDB ID
                var findResponse = tmdbService.findByExternalId(String.valueOf(episodeInformation.id), TmdbSourceIdType.TVDB.getValue());

                // Attempt to load the episode name if TMDB returned a result
                if (findResponse.isSuccessful() && findResponse.episodes.size() > 0 && !StringUtils.isBlank(findResponse.episodes.get(0).name)) {
                    episodeInformation.name = findResponse.episodes.get(0).name;
                }
            }

            // Update the process of the download in the bot status manager
            updateProcessString("Download " + queueItem.showId + " s" + queueItem.seasonNumber + "e" + queueItem.episodeNumber + " - masking...");

            // Add the torrent to real-debrid, use the proper method based on the file type
            torrentInformation = addTorrent(queueItem);

            // Make sure that it was successful in adding the torrent, it is returned a null, an error has occurred
            if (torrentInformation == null) {
                // Log the issue to the log
                logger.error("An error occurred while trying to add the following torrent to real-debrid: " + queueItem.filename);

                // Manage the failure status of the queue item
                handleFailed(queueItem);

                // Stop the process
                endProcess();
                return;
            }

            // Locate possible media files to be downloaded from within the torrent file
            var possibleFiles = new ArrayList<RdbTorrentFile>();
            for (RdbTorrentFile file : torrentInformation.files) {
                // Convert the pathname to all lowercase letters for easier parsing
                var normalizedPathname = file.path.toLowerCase();

                // Replace any spaces with '.' characters to account for the few torrent file names with spaces
                normalizedPathname = normalizedPathname.replace(" ", ".");

                // Determine the type of file
                var type = FileType.determineFiletype(normalizedPathname);

                // Add the video files that match the right season/episode string to the list of possible files
                // This looks for the s##e## format and verifies that there is a '.' on both sides so that we do not accidentally fetch combined episodes.
                if (type.isVideo() && normalizedPathname.contains("." + fileUtilities.buildSeasonAndEpisodeString(queueItem.episodeNumber, queueItem.seasonNumber) + ".")) {
                    possibleFiles.add(file);
                }
            }

            // Make sure the bot was able to locate some files
            if (possibleFiles.isEmpty()) {
                // Send a notice that no files were found
                new MessageBuilder().setEmbed(new EmbedBuilder()
                        .setTitle("Unable to determine which file to download")
                        .setDescription("The bot was unable to locate any possible files to download for the following " +
                                "episode within the torrent file. You will need to manually download and import this episode.")
                        .addField("Show:", "```" + showInfo.series.name + "{" + showInfo.series.id + "}```")
                        .addInlineField("Season:", "```" + queueItem.seasonNumber + "```")
                        .addInlineField("Episode:", "```" + queueItem.episodeNumber + "```")
                        .addField("Torrent File:", "```" + queueItem.filename + "```")
                        .setColor(Color.YELLOW)
                ).send(discordApi.getUserById(botOwner).join()).join();

                // Mark the download as a skipped download in the history
                downloadHistoryItemDao.create(queueItem, "skipped");
                downloadQueueItemDao.delete(queueItem);

                // Delete the torrent file from real-debrid
                rdbService.deleteTorrent(torrentInformation.id);

                // Update the episode status in sickgear to show that the download was skipped
                sgServiceWrapper.setEpisodeStatus(queueItem.showId, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.SKIPPED);

                // Stop the process
                endProcess();
                return;
            }

            // Select the largest torrent file if there is more than one. Some release groups provide sample files among other
            // media files that we do not want to select and those are smaller than the actual file we want.
            RdbTorrentFile selectedFile = null;
            if (possibleFiles.size() > 1) {
                var largestSize = 0L;
                for (RdbTorrentFile file : possibleFiles) {
                    if (file.bytes > largestSize) {
                        largestSize = file.bytes;
                        selectedFile = file;
                    }
                }
            } else {
                selectedFile = possibleFiles.get(0);
            }

            // Make sure the bot was able to select a file from the possible files
            if (selectedFile == null) {
                // Send a notice that no files were found
                new MessageBuilder().setEmbed(new EmbedBuilder()
                        .setTitle("Unable to determine which file to download")
                        .setDescription("The bot was unable to select a file for the following episode within the torrent file. " +
                                "You will need to manually download and import this episode.")
                        .addField("Show:", "```" + showInfo.series.name + "{" + showInfo.series.id + "}```")
                        .addInlineField("Season:", "```" + queueItem.seasonNumber + "```")
                        .addInlineField("Episode:", "```" + queueItem.episodeNumber + "```")
                        .addField("Torrent File:", "```" + queueItem.filename + "```")
                        .setColor(Color.YELLOW)
                ).send(discordApi.getUserById(botOwner).join()).join();

                // Mark the download as a skipped download in the history
                downloadHistoryItemDao.create(queueItem, "skipped");
                downloadQueueItemDao.delete(queueItem);

                // Delete the torrent file from real-debrid
                rdbService.deleteTorrent(torrentInformation.id);

                // Update the episode status in sickgear to show that the download was skipped
                sgServiceWrapper.setEpisodeStatus(queueItem.showId, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.SKIPPED);

                // Stop the process
                endProcess();
                return;
            }

            // Select the proper file on real-debrid
            rdbService.selectFiles(torrentInformation.id, String.valueOf(selectedFile.id));

            // Ensure that real-debrid has the file downloaded, otherwise wait for it to be downloaded.
            // This is checked once every 5 seconds to avoid getting blocked by sending too many queries.
            torrentInformation = rdbService.getTorrentInfo(torrentInformation.id);
            var lastUpdated = LocalDateTime.now();
            while (torrentInformation.status != RdbTorrentStatusEnum.DOWNLOADED) {
                if (lastUpdated.plus(5, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                    torrentInformation = rdbService.getTorrentInfo(torrentInformation.id);

                    // Update the process sting with the download progress
                    if (torrentInformation.status == RdbTorrentStatusEnum.DOWNLOADING) {
                        updateProcessString("Download " + queueItem.showId + " s" + queueItem.seasonNumber + "e" +
                                queueItem.episodeNumber + " - masking: " + torrentInformation.progress + "%");
                    } else {
                        updateProcessString("Download " + queueItem.showId + " s" + queueItem.seasonNumber + "e" +
                                queueItem.episodeNumber + " - masking: processing...");
                    }

                    // If real-debrid encountered an error while downloading the file then stop
                    if (torrentInformation.status == RdbTorrentStatusEnum.VIRUS ||
                            torrentInformation.status == RdbTorrentStatusEnum.ERROR ||
                            torrentInformation.status == RdbTorrentStatusEnum.MAGNET_ERROR ||
                            torrentInformation.status == RdbTorrentStatusEnum.DEAD) {

                        // Log the issue to the log
                        logger.error("An error occurred while waiting for real-debrid to download the file: " + queueItem.filename);

                        // Manage the failure status of the queue item
                        handleFailed(queueItem);

                        // Delete the torrent file from real-debrid
                        rdbService.deleteTorrent(torrentInformation.id);

                        endProcess();
                        return;
                    }

                    lastUpdated = LocalDateTime.now();
                }
            }

            // Make sure we have updated information on this torrent
            torrentInformation = rdbService.getTorrentInfo(torrentInformation.id);

            // Unrestrict the download link so the bot can download the file
            var unrestrictedLink = rdbService.unrestrictLink(String.valueOf(torrentInformation.links.get(0)));

            // Set the name of the file as it will be in the temp folder
            tempDownloadFilename = unrestrictedLink.filename;

            // Create a status tracker for the download failure status
            AtomicBoolean downloadFailed = new AtomicBoolean(false);

            updateProcessString("Download " + queueItem.showId + " s" + queueItem.seasonNumber + "e" +
                    queueItem.episodeNumber + " - downloading");

            // Run the download task
            DownloadQueueItem finalQueueItem = queueItem;
            RdbTorrent finalTorrentInformation = torrentInformation;
            String finalTempDownloadFilename = tempDownloadFilename;
            fileUtilities.downloadFile(String.valueOf(unrestrictedLink.download), tempDownloadFilename, null)
                    .subscribe().with(
                            progress -> {
                                var percentage = (((double) progress / unrestrictedLink.filesize) * 100);
                                updateProcessString("Download " + finalQueueItem.showId + " s" + finalQueueItem.seasonNumber + "e" +
                                        finalQueueItem.episodeNumber + " - downloading: " + decimalFormatter.format(percentage) + "%");
                            },
                            failure -> {
                                logger.error(new InterruptedException("Failed to download file " + finalTempDownloadFilename + " for the following torrent file: " + finalQueueItem.filename));
                                downloadFailed.set(true);
                            },
                            () -> rdbService.deleteTorrent(finalTorrentInformation.id)
                    );

            // Handle the failure of the download if one occurred
            if (downloadFailed.get()) {
                // Manage the failure status of the queue item
                handleFailed(queueItem);

                // Attempt to delete the failed download file(s)
                FileUtils.deleteQuietly(new File(tempFolder + tempDownloadFilename));
                FileUtils.deleteQuietly(new File(tempFolder + tempDownloadFilename + ".pbdownload"));

                // Delete all the torrent from real-debrid
                rdbService.deleteTorrent(torrentInformation.id);

                endProcess();
                return;
            }

            updateProcessString("Download " + queueItem.showId + " s" + queueItem.seasonNumber + "e" +
                    queueItem.episodeNumber + " - processing");

            // Generate the season and show folder names
            var showFoldername = fileUtilities.generatePathname(showInfo.series);
            var seasonFoldername = "Season " + episodeInformation.seasonNumber;

            // Create the show folder
            fileUtilities.createFolder(tvFolder + showFoldername);

            // Add or fetch the show from the database
            var show = showDao.create(showInfo.series.id, showInfo.series.name, showFoldername);

            // Create the season folder
            fileUtilities.createFolder(tvFolder + showFoldername + "/" + seasonFoldername);

            // Generate the final filename of the episode
            var finalEpisodeFilename = fileUtilities.generateEpisodeFilename(episodeInformation, show, FileType.determineFiletype(tempDownloadFilename));

            // Check if the item is in the database at all
            var existsInDatabase = episodeDao.existsByTvdbId(episodeInformation.id);

            // Ensure that old media files get deleted if they are being replaced by a file of a different type
            if (existsInDatabase && !episodeDao.getByTvdbId(episodeInformation.id).filename.equalsIgnoreCase(finalEpisodeFilename)) {
                fileUtilities.deleteFile(tvFolder + showFoldername + "/" + seasonFoldername + "/" + episodeDao.getByTvdbId(episodeInformation.id).filename);
            }

            // Ensure that old subtitles are deleted if the media file is being overwritten
            if (existsInDatabase) {
                // Fetch a list of subtitles matching this episode
                var subtitleList = new ArrayList<>(episodeSubtitleDao.getByEpisode(episodeDao.getByTvdbId(episodeInformation.id)));

                // Delete the file from the filesystem and database
                subtitleList.forEach(subtitle -> {
                    fileUtilities.deleteFile(tvFolder + showFoldername + "/" + seasonFoldername + "/" + subtitle.filename);
                    episodeSubtitleDao.delete(subtitle.id);
                });
            }

            // Move the media file into place
            fileUtilities.moveMedia(tempFolder + tempDownloadFilename, tvFolder + showFoldername + "/" + seasonFoldername + "/" + finalEpisodeFilename, true);

            // Add or update the episode in the database
            episodeDao.createOrUpdate(episodeInformation, finalEpisodeFilename, show.id);

            // Add this item to the download history showing it was successfully downloaded
            // then delete it from the queue.
            downloadHistoryItemDao.create(queueItem, "downloaded");
            downloadQueueItemDao.delete(queueItem);

            // Update the status on sickgear to reflect the downloaded episode
            sgServiceWrapper.setEpisodeStatus(showInfo.series.id, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.DOWNLOADED, queueItem.quality);

            // Send a notice that the episode has been added to the server if this was not an upgrade of an existing file
            if (!existsInDatabase) {
                // Attempt to locate the episode on TMDB in order to display an overview in the episode notification
                var findResponse = tmdbService.findByExternalId(String.valueOf(episodeInformation.id), TmdbSourceIdType.TVDB.getValue());

                // Set the overview if the search was successful
                var overview = "";
                if (findResponse.isSuccessful() && findResponse.episodes.size() > 0 && !StringUtils.isBlank(findResponse.episodes.get(0).overview)) {
                    overview = findResponse.episodes.get(0).overview;
                }

                // Send the notification message
                new MessageBuilder()
                        .setEmbed(messageFormatter.newEpisodeNotification(episodeInformation, showInfo.series, overview))
                        .send(discordApi.getTextChannelById(newEpisodeNotificationChannel).orElseThrow())
                        .exceptionally(ExceptionLogger.get())
                        .join();
            }

            // Trigger a refresh of the libraries on the Plex server
            /*
            try {
                plexService.refreshLibraries();
            } catch (Exception e) {
                reportError(e);
            }
             */

            // Make sure the process is removed from the bot status manager.
            endProcess();
        } catch (ProcessingException e) {
            // Delete torrent from real-debrid if there was an error
            try {
                if (torrentInformation != null) {
                    rdbService.deleteTorrent(torrentInformation.id);
                }
            } catch (Throwable e1) {
                logger.trace("Unable to delete torrent after error, the torrent may have already been deleted.");
            }

            // Mark the download as dead download in the history and reset the
            // status to queued so that it can be tried again.
            downloadHistoryItemDao.create(queueItem, "dead");
            downloadQueueItemDao.updateStatus(queueItem.id, "queued");

            // Display the error and exit
            logger.error(e);
            endProcess(e);
        } catch (Throwable e) {
            // Delete torrent from real-debrid if there was an error
            try {
                if (torrentInformation != null) {
                    rdbService.deleteTorrent(torrentInformation.id);
                }
            } catch (Throwable e1) {
                logger.trace("Unable to delete torrent after error, the torrent may have already been deleted.");
            }

            // Manage the failure status of the queue item
            handleFailed(queueItem);

            // Attempt to delete the temp download files if they exist
            if (!tempDownloadFilename.isBlank()) {
                FileUtils.deleteQuietly(new File(tempFolder + tempDownloadFilename));
                FileUtils.deleteQuietly(new File(tempFolder + tempDownloadFilename + ".pbdownload"));
            }

            // Report the error and exit
            logger.error(e.getCause());
            e.printStackTrace();
            endProcess(e);
        }
    }

    private void handleFailed(DownloadQueueItem queueItem) {
        // Mark the download as a failed download in the history
        downloadHistoryItemDao.create(queueItem, "failed");
        downloadQueueItemDao.delete(queueItem);

        // Re-add the queue item to the end of the queue only if it has not failed to be downloaded
        // more than three times. If it has failed 3 times then it should not be added to the queue
        // and the episode status in SickGear should be set as failed
        if (downloadHistoryItemDao.countFailed(queueItem.showId, queueItem.seasonNumber, queueItem.episodeNumber,
                queueItem.quality, queueItem.filename) <= 3) {
            downloadQueueItemDao.create(queueItem);
        } else {
            sgServiceWrapper.setEpisodeStatus(queueItem.showId, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.FAILED);
        }
    }
}