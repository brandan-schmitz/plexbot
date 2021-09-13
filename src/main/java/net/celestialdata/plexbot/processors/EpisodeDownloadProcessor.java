package net.celestialdata.plexbot.processors;

import lombok.SneakyThrows;
import net.celestialdata.plexbot.clients.models.rdb.RdbTorrent;
import net.celestialdata.plexbot.clients.models.rdb.RdbTorrentFile;
import net.celestialdata.plexbot.clients.models.rdb.RdbUnrestrictedLink;
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
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Dependent
public class EpisodeDownloadProcessor extends BotProcess implements Runnable {
    private final Logger logger = Logger.getLogger(EpisodeDownloadProcessor.class);

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
    ImportMediaProcessor importMediaProcessor;

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
        // Create the list of torrents here so that if an error occurs the bot can remove them from real-debrid
        var finalTorrentList = new ArrayList<RdbTorrent>();

        // Fetch the next item to download
        var queueItem = downloadQueueItemDao.getNext();

        // Create a variable to store where the temporary file location will be
        var tempDownloadFolder = "";

        // If it returned null, then there was nothing to download and this should exit
        if (queueItem == null) {
            return;
        }

        try {
            // Configure the process name as it will appear in the bot process manager
            configureProcess("Downloading " + queueItem.showId + " - s" + queueItem.seasonNumber + "e" + queueItem.episodeNumber);

            // Mark the item as downloading since the process has been started
            queueItem = downloadQueueItemDao.updateStatus(queueItem.id, "downloading");

            // Fetch information about this show
            var showInfo = tvdbService.getSeries(queueItem.showId);

            // Verify that it was able to fetch information about the show
            if (!showInfo.status.equals("success")) {
                // Log the issue to the log
                logger.error("An error occurred while trying to fetch information on TVDB about this torrent file's show: " + queueItem.filename);

                // Mark the download as a failed download in the history
                downloadHistoryItemDao.create(queueItem, "failed");
                downloadQueueItemDao.delete(queueItem);

                // Delete the failed torrent file - a new one is downloaded by SickGear
                fileUtilities.deleteFile(torrentFolder + queueItem.filename);

                // Update the episode status in sickgear to show that the download failed
                sgServiceWrapper.setEpisodeStatus(queueItem.showId, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.FAILED);

                // Stop the process
                endProcess();
                return;
            }

            // Fetch a list of episodes in the season this episode is in
            var seasonEpisodeList = tvdbService.getSeriesEpisodes(queueItem.showId, queueItem.seasonNumber);

            // Verify that it was able to get a list of episodes for the season
            if (!seasonEpisodeList.status.equalsIgnoreCase("success")) {
                // Log the issue to the log
                logger.error("An error occurred while trying to fetch information on TVDB about this torrent file's season episodes: " + queueItem.filename);

                // Mark the download as a failed download in the history
                downloadHistoryItemDao.create(queueItem, "failed");
                downloadQueueItemDao.delete(queueItem);

                // Delete the failed torrent file - a new one is downloaded by SickGear
                fileUtilities.deleteFile(torrentFolder + queueItem.filename);

                // Update the episode status in sickgear to show that the download failed
                sgServiceWrapper.setEpisodeStatus(queueItem.showId, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.FAILED);

                // Stop the process
                endProcess();
                return;
            }

            // Locate the proper episode from the list of episodes obtained above
            TvdbEpisode episodeInformation = null;
            for (TvdbEpisode episode : seasonEpisodeList.seriesEpisodes.episodes) {
                if (episode.number == queueItem.episodeNumber) {
                    episodeInformation = episode;
                }
            }

            // Verify that an episode was loaded
            if (episodeInformation == null) {
                // Log the issue to the log
                logger.error("Unable to locate the proper episode on TVDB for the following torrent file: " + queueItem.filename);

                // Mark the download as a failed download in the history
                downloadHistoryItemDao.create(queueItem, "failed");
                downloadQueueItemDao.delete(queueItem);

                // Delete the failed torrent file - a new one is downloaded by SickGear
                fileUtilities.deleteFile(torrentFolder + queueItem.filename);

                // Update the episode status in sickgear to show that the download failed
                sgServiceWrapper.setEpisodeStatus(queueItem.showId, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.FAILED);

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

            // Add the torrent to real-debrid, use the proper method based on the file type
            var torrentInformation = addTorrent(queueItem);

            // Make sure that it was successful in adding the torrent, it is returned a null, an error has occurred
            if (torrentInformation == null) {
                // Log the issue to the log
                logger.error("An error occurred while trying to add the following torrent to real-debrid: " + queueItem.filename);

                // Mark the download as a failed download in the history
                downloadHistoryItemDao.create(queueItem, "failed");
                downloadQueueItemDao.delete(queueItem);

                // Delete the failed torrent file - a new one is downloaded by SickGear
                fileUtilities.deleteFile(torrentFolder + queueItem.filename);

                // Update the episode status in sickgear to show that the download failed
                sgServiceWrapper.setEpisodeStatus(queueItem.showId, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.FAILED);

                // Stop the process
                endProcess();
                return;
            }

            // Choose the files to download
            var selectedFiles = new ArrayList<RdbTorrentFile>();
            for (RdbTorrentFile file : torrentInformation.files) {
                var type = FileType.determineFiletype(file.path.toLowerCase());
                if (type.isVideo() || type.isSubtitle()) {
                    selectedFiles.add(file);
                }
            }

            // Add the appropriate number of torrents to real-debrid
            if ((selectedFiles.size() - 1) > 0) {
                // Add the torrent already being used to the list
                finalTorrentList.add(torrentInformation);

                // Create new torrents so there are enough for each of the files
                for (int i = 0; i < (selectedFiles.size() - 1); i++) {
                    // Attempt to add another torrent file
                    var additionalTorrent = addTorrent(queueItem);

                    // If the torrent was not added then decrement the counter so that it can try again
                    if (additionalTorrent == null) {
                        i--;
                        continue;
                    }

                    // Add the additional torrent to the list
                    finalTorrentList.add(additionalTorrent);
                }
            } else {
                // If there are no other torrents required, add the one existing torrent to the list
                finalTorrentList.add(torrentInformation);
            }

            // Map the selected files to torrents
            int listPos = 0;
            var selectedTorrentMapping = new HashMap<RdbTorrent, RdbTorrentFile>();
            for (RdbTorrentFile file : selectedFiles) {
                // Select the file as defined in the selectedVideoFiles list
                rdbService.selectFiles(finalTorrentList.get(listPos).id, String.valueOf(file.id));

                // Get updated information about that torrent
                finalTorrentList.set(listPos, rdbService.getTorrentInfo(finalTorrentList.get(listPos).id));

                // Fetch updated RdbTorrentFile
                for (RdbTorrentFile torrentFile : finalTorrentList.get(listPos).files) {
                    if (torrentFile.id == file.id) {
                        file = torrentFile;
                    }
                }

                // Add the file to the selection mapping
                selectedTorrentMapping.put(finalTorrentList.get(listPos), file);

                // Increment the list position indicator
                listPos++;
            }

            // Ensure real-debrid has the files downloaded
            for (Map.Entry<RdbTorrent, RdbTorrentFile> entry : selectedTorrentMapping.entrySet()) {
                var updatedTorrentInfo = rdbService.getTorrentInfo(entry.getKey().id);
                var lastUpdated = LocalDateTime.now();

                // Check every 5 seconds for an updated status
                while (updatedTorrentInfo.status != RdbTorrentStatusEnum.DOWNLOADED) {
                    if (lastUpdated.plus(5, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        updatedTorrentInfo = rdbService.getTorrentInfo(entry.getKey().id);

                        // If real-debrid encountered an error while downloading the file then stop
                        if (updatedTorrentInfo.status == RdbTorrentStatusEnum.VIRUS ||
                                updatedTorrentInfo.status == RdbTorrentStatusEnum.ERROR ||
                                updatedTorrentInfo.status == RdbTorrentStatusEnum.MAGNET_ERROR ||
                                updatedTorrentInfo.status == RdbTorrentStatusEnum.DEAD) {

                            // Log the issue to the log
                            logger.error("An error occurred while waiting for real-debrid to download the file: " + queueItem.filename);

                            // Mark the download as a failed download in the history
                            downloadHistoryItemDao.create(queueItem, "failed");
                            downloadQueueItemDao.delete(queueItem);

                            // Delete the failed torrent file - a new one is downloaded by SickGear
                            fileUtilities.deleteFile(torrentFolder + queueItem.filename);

                            // Update the episode status in sickgear to show that the download failed
                            sgServiceWrapper.setEpisodeStatus(queueItem.showId, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.FAILED);

                            // Delete all the torrent files from real-debrid
                            for (RdbTorrent torrent : finalTorrentList) {
                                rdbService.deleteTorrent(torrent.id);
                            }

                            endProcess();
                            return;
                        }

                        lastUpdated = LocalDateTime.now();
                    }
                }
            }

            // Ensure list of mappings is updated
            var selectedTorrentMappingCopy = new HashMap<>(selectedTorrentMapping);
            for (Map.Entry<RdbTorrent, RdbTorrentFile> entry : selectedTorrentMappingCopy.entrySet()) {
                var torrentId = entry.getKey().id;
                var torrentFileId = entry.getValue().id;

                // Remove the old item from the map
                selectedTorrentMapping.remove(entry.getKey());

                // Add the updated items to the map
                var updatedTorrent = rdbService.getTorrentInfo(torrentId);
                for (RdbTorrentFile file : updatedTorrent.files) {
                    if (file.id == torrentFileId) {
                        selectedTorrentMapping.put(updatedTorrent, file);
                    }
                }
            }

            // Unrestrict the file links
            var unrestrictedLinks = new HashMap<RdbTorrent, RdbUnrestrictedLink>();
            long totalDownloadSize = 0;
            for (Map.Entry<RdbTorrent, RdbTorrentFile> entry : selectedTorrentMapping.entrySet()) {
                var link = rdbService.unrestrictLink(String.valueOf(entry.getKey().links.get(0)));
                unrestrictedLinks.put(entry.getKey(), link);
                totalDownloadSize = totalDownloadSize + link.filesize;
            }

            // Create the temporary download folder
            var createTempFolderSuccess = fileUtilities.createFolder(tempFolder + fileUtilities.generatePathname(showInfo.series) +
                    " s" + queueItem.seasonNumber + "e" + queueItem.episodeNumber);
            tempDownloadFolder = tempFolder + fileUtilities.generatePathname(showInfo.series) + " s" + queueItem.seasonNumber + "e" + queueItem.episodeNumber;
            if (!createTempFolderSuccess) {
                // Log the issue to the log
                logger.error("An error occurred while creating the temporary folder to store the download files for the following torrent file: " + queueItem.filename);

                // Mark the download as a failed download in the history
                downloadHistoryItemDao.create(queueItem, "failed");
                downloadQueueItemDao.delete(queueItem);

                // Delete the failed torrent file - a new one is downloaded by SickGear
                fileUtilities.deleteFile(torrentFolder + queueItem.filename);

                // Update the episode status in sickgear to show that the download failed
                sgServiceWrapper.setEpisodeStatus(queueItem.showId, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.FAILED);

                // Delete all the torrent files from real-debrid
                for (RdbTorrent torrent : finalTorrentList) {
                    rdbService.deleteTorrent(torrent.id);
                }

                endProcess();
                return;
            }

            // Download the files
            AtomicBoolean downloadFailed = new AtomicBoolean(false);
            for (Map.Entry<RdbTorrent, RdbUnrestrictedLink> entry : unrestrictedLinks.entrySet()) {
                // Normalize the filename to lowercase letters
                var filename = entry.getValue().filename;

                // Add selected files to their proper HashMap
                var fileType = FileType.determineFiletype(filename);

                // Make sure it is a valid filetype
                if (fileType == FileType.UNKNOWN) {
                    // Log the issue to the log
                    logger.error("Unable to download the media file for the following torrent file because it is of an unknown media type: " + queueItem.filename);

                    // Mark the download as a failed download in the history
                    downloadHistoryItemDao.create(queueItem, "failed");
                    downloadQueueItemDao.delete(queueItem);

                    // Delete the failed torrent file - a new one is downloaded by SickGear
                    fileUtilities.deleteFile(torrentFolder + queueItem.filename);

                    // Delete the temporary download folder if it is empty
                    FileUtils.deleteQuietly(new File(tempDownloadFolder));

                    // Update the episode status in sickgear to show that the download failed
                    sgServiceWrapper.setEpisodeStatus(queueItem.showId, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.FAILED);

                    // Delete all the torrent files from real-debrid
                    for (RdbTorrent torrent : finalTorrentList) {
                        rdbService.deleteTorrent(torrent.id);
                    }

                    endProcess();
                    return;
                }

                // Run the download task
                long finalTotalDownloadSize = totalDownloadSize;
                DownloadQueueItem finalQueueItem = queueItem;
                DownloadQueueItem finalQueueItem1 = queueItem;
                fileUtilities.downloadFile(String.valueOf(entry.getValue().download), filename, tempDownloadFolder)
                        .subscribe().with(
                                progress -> {
                                    var percentage = (((double) progress / finalTotalDownloadSize) * 100);
                                    updateProcessString("Downloading " + finalQueueItem.showId + " - s" +
                                            finalQueueItem.seasonNumber + "e" + finalQueueItem.episodeNumber + " - " + percentage + "%");
                                },
                                failure -> {
                                    logger.error(new InterruptedException("Failed to download file " + entry.getValue().filename + " for the following torrent file: " + finalQueueItem1.filename));
                                    downloadFailed.set(true);
                                },
                                () -> rdbService.deleteTorrent(entry.getKey().id)
                        );

                // Handle the failure of the download if one occurred
                if (downloadFailed.get()) {
                    // Mark the download as a failed download in the history
                    downloadHistoryItemDao.create(queueItem, "failed");
                    downloadQueueItemDao.delete(queueItem);

                    // Delete the failed torrent file - a new one is downloaded by SickGear
                    fileUtilities.deleteFile(tempDownloadFolder + queueItem.filename);

                    // Delete the temporary download folder if it is empty
                    FileUtils.deleteQuietly(new File(tempDownloadFolder));

                    // Update the episode status in sickgear to show that the download failed
                    sgServiceWrapper.setEpisodeStatus(queueItem.showId, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.FAILED);

                    // Delete all the torrent files from real-debrid
                    for (RdbTorrent torrent : finalTorrentList) {
                        rdbService.deleteTorrent(torrent.id);
                    }

                    endProcess();
                    return;
                }
            }

            // Update the instance of the import media manager to overwrite media files and not use its default logging methods
            importMediaProcessor.setOverwrite(true);
            importMediaProcessor.setSendFailureMessages(false);

            // Build a collection of media and subtitle files that were downloaded
            Collection<File> episodeMediaFiles = FileUtils.listFiles(new File(tempDownloadFolder), FileType.mediaFileExtensions, false);
            Collection<File> episodeSubtitleFiles = FileUtils.listFiles(new File(tempDownloadFolder), FileType.subtitleFileExtensions, false);

            // Ensure there are no directories listed as files
            episodeMediaFiles.removeIf(File::isDirectory);
            episodeSubtitleFiles.removeIf(File::isDirectory);

            // Remove any hidden files from the lists
            episodeMediaFiles.removeIf(File::isHidden);
            episodeSubtitleFiles.removeIf(File::isHidden);

            // If there is more than one media file, send a notice and skip the import process, otherwise if it did not detect any media files
            // it should also send an error about this.
            if (episodeMediaFiles.size() > 1) {
                // Build the list of files into a string
                StringBuilder stringBuilder = new StringBuilder();
                for (File file : episodeMediaFiles) {
                    stringBuilder.append("- ").append(file.getName()).append("\n");
                }

                // Send the notice
                new MessageBuilder().setEmbed(new EmbedBuilder()
                        .setTitle("Multiple Video Files Downloaded")
                        .setDescription("The bot encountered multiple video files while attempting to import the following episode from Sickgear. " +
                                "You will need to manually import the correct video file.")
                        .addField("Show:", "```" + showInfo.series.name + "{" + showInfo.series.id + "}```")
                        .addInlineField("Season:", "```" + queueItem.seasonNumber + "```")
                        .addInlineField("Episode:", "```" + queueItem.episodeNumber + "```")
                        .addField("Torrent File:", "```" + queueItem.filename + "```")
                        .addField("Conflicting File:", "```" + stringBuilder + "```")
                        .setColor(Color.YELLOW)
                ).send(discordApi.getUserById(botOwner).join()).join();

                // Mark the download as a completed download in the history
                downloadHistoryItemDao.create(queueItem, "downloaded");
                downloadQueueItemDao.delete(queueItem);

                // Update the episode status in sickgear to show that the download finished
                sgServiceWrapper.setEpisodeStatus(queueItem.showId, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.DOWNLOADED, queueItem.quality);

                // End the process
                endProcess();
                return;
            } else if (episodeMediaFiles.isEmpty()) {
                // Send the notice
                new MessageBuilder().setEmbed(new EmbedBuilder()
                        .setTitle("No Video Files Detected")
                        .setDescription("The bot did not find any video files within the download folder for the following episode:")
                        .addField("Show:", "```" + showInfo.series.name + "{" + showInfo.series.id + "}```")
                        .addInlineField("Season:", "```" + queueItem.seasonNumber + "```")
                        .addInlineField("Episode:", "```" + queueItem.episodeNumber + "```")
                        .addField("Torrent File:", "```" + queueItem.filename + "```")
                        .setColor(Color.RED)
                ).send(discordApi.getUserById(botOwner).join()).join();

                // Mark the download as a failed download in the history
                downloadHistoryItemDao.create(queueItem, "failed");
                downloadQueueItemDao.delete(queueItem);

                // Delete the failed torrent file - a new one is downloaded by SickGear
                fileUtilities.deleteFile(torrentFolder + queueItem.filename);

                // Delete the temporary download folder if it is empty
                FileUtils.deleteQuietly(new File(tempDownloadFolder));

                // Update the episode status in sickgear to show that the download finished
                sgServiceWrapper.setEpisodeStatus(queueItem.showId, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.FAILED);

                // End the process
                endProcess();
                return;
            }

            // If there are any subtitle files send a notice about them
            if (episodeSubtitleFiles.size() > 0) {
                // Build the list of files into a string
                StringBuilder stringBuilder = new StringBuilder();
                for (File file : episodeSubtitleFiles) {
                    stringBuilder.append("- ").append(file.getName()).append("\n");
                }

                // Send the notice
                new MessageBuilder().setEmbed(new EmbedBuilder()
                        .setTitle("New Subtitle Downloaded")
                        .setDescription("A TV episode was recently downloaded to the server and was accompanied by the following " +
                                "subtitle file(s). The file has been left in the temp folder and will require manual importing.")
                        .addField("Show:", "```" + showInfo.series.name + "{" + showInfo.series.id + "}```")
                        .addInlineField("Season:", "```" + queueItem.seasonNumber + "```")
                        .addInlineField("Episode:", "```" + queueItem.episodeNumber + "```")
                        .addField("Subtitle Filename(s):", "```" + stringBuilder + "```")
                        .setColor(Color.GREEN)
                ).send(discordApi.getUserById(botOwner).join()).join();
            }

            // Load the filename, it should be the only one in the file collection
            var downloadedEpisodeFilename = episodeMediaFiles.iterator().next().getAbsolutePath();

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
            var finalEpisodeFilename = fileUtilities.generateEpisodeFilename(episodeInformation, show, FileType.determineFiletype(downloadedEpisodeFilename));

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
            fileUtilities.moveMedia(downloadedEpisodeFilename, tvFolder + showFoldername + "/" + seasonFoldername + "/" + finalEpisodeFilename, true);

            // Add or update the episode in the database
            episodeDao.createOrUpdate(episodeInformation, finalEpisodeFilename, show.id);

            // Add this item to the download history showing it was successfully downloaded
            // then delete it from the queue.
            downloadHistoryItemDao.create(queueItem, "downloaded");
            downloadQueueItemDao.delete(queueItem);

            // Update the status on sickgear to reflect the downloaded episode
            sgServiceWrapper.setEpisodeStatus(showInfo.series.id, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.DOWNLOADED, queueItem.quality);

            // Delete the temporary download folder if it is empty
            try {
                if (fileUtilities.isFolderEmpty(tempDownloadFolder)) {
                    FileUtils.deleteDirectory(new File(tempDownloadFolder));
                }
            } catch (IOException e) {
                reportError(e);
            }

            // Delete the torrent file
            fileUtilities.deleteFile(torrentFolder + queueItem.filename);

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
            try {
                plexService.refreshLibraries();
            } catch (Exception e) {
                reportError(e);
            }

            // Make sure the process is removed from the bot status manager.
            endProcess();
        } catch (Throwable e) {
            // Delete torrents from real-debrid if there was an error
            if (!finalTorrentList.isEmpty()) {
                for (RdbTorrent torrent : finalTorrentList) {
                    rdbService.deleteTorrent(torrent.id);
                }
            }

            // Mark that the download failed
            // Mark the download as a failed download in the history
            downloadHistoryItemDao.create(queueItem, "failed");
            downloadQueueItemDao.delete(queueItem);

            // Delete the failed torrent file - a new one is downloaded by SickGear
            fileUtilities.deleteFile(torrentFolder + queueItem.filename);

            // Delete the temporary download folder if it is empty
            if (!tempDownloadFolder.isBlank()) {
                FileUtils.deleteQuietly(new File(tempDownloadFolder));
            }

            // Update the episode status in sickgear to show that the download finished
            sgServiceWrapper.setEpisodeStatus(queueItem.showId, queueItem.seasonNumber, queueItem.episodeNumber, SgStatus.FAILED);

            // Report the error and exit
            logger.error(e.getCause());
            endProcess(e);
        }
    }
}