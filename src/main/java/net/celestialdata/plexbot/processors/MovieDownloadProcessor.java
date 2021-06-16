package net.celestialdata.plexbot.processors;

import io.quarkus.arc.log.LoggerName;
import io.smallrye.mutiny.Multi;
import net.celestialdata.plexbot.clients.models.omdb.OmdbResult;
import net.celestialdata.plexbot.clients.models.rdb.RdbTorrent;
import net.celestialdata.plexbot.clients.models.rdb.RdbTorrentFile;
import net.celestialdata.plexbot.clients.models.rdb.RdbUnrestrictedLink;
import net.celestialdata.plexbot.clients.models.rdb.enums.RdbTorrentStatusEnum;
import net.celestialdata.plexbot.clients.models.yts.YtsMovie;
import net.celestialdata.plexbot.clients.models.yts.YtsMovieTorrent;
import net.celestialdata.plexbot.clients.services.RdbService;
import net.celestialdata.plexbot.clients.services.YtsService;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.entities.Movie;
import net.celestialdata.plexbot.utilities.BotProcess;
import net.celestialdata.plexbot.enumerators.FileTypes;
import net.celestialdata.plexbot.utilities.FileUtilities;
import net.celestialdata.plexbot.enumerators.MovieDownloadSteps;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class MovieDownloadProcessor extends BotProcess {

    @LoggerName(value = "net.celestialdata.plexbot.processors.MovieDownloadProcessor")
    Logger logger;

    @ConfigProperty(name = "FolderSettings.movieFolder")
    String movieFolder;

    @ConfigProperty(name = "FolderSettings.tempFolder")
    String tempFolder;

    @Inject
    MessageFormatter messageFormatter;

    @Inject
    @RestClient
    YtsService ytsService;

    @Inject
    @RestClient
    RdbService rdbService;

    @Inject
    FileUtilities fileUtilities;

    public Multi<Map<MovieDownloadSteps, EmbedBuilder>> processDownload(OmdbResult movieToDownload, Message statusMessage) {
        configureProcess("Download " + movieToDownload.title + " (" + movieToDownload.year + ")", statusMessage);
        return processDownload(movieToDownload);
    }

    public Multi<Map<MovieDownloadSteps, EmbedBuilder>> processDownload(OmdbResult movieToDownload) {
        // Configure the process if it is now already configured
        if (processId == null) {
            configureProcess("Download " + movieToDownload.title + " (" + movieToDownload.year + ")");
        }


        return Multi.createFrom().emitter(processEmitter -> {
            // Create the list of torrents here so that if an error occurs the bot can remove them from real-debrid
            var finalTorrentList = new ArrayList<RdbTorrent>();

            try {
                // Show that the process is attempting to locate the movie
                processEmitter.emit(Map.of(
                        MovieDownloadSteps.LOCATE_MOVIE,
                        messageFormatter.formatDownloadProgressMessage(
                                movieToDownload, MovieDownloadSteps.LOCATE_MOVIE)
                ));

                // Search YTS for the movie
                var ytsResponse = ytsService.search(movieToDownload.imdbID);

                // Verify that the search was successful, fail if it was not successful
                if (!ytsResponse.status.equals("ok")) {
                    processEmitter.fail(new InterruptedException("Failure to query YTS API: " + ytsResponse.statusMessage));
                    endProcess();
                    return;
                }

                // Verify that the search returned results, otherwise add movie to the waiting list and then fail
                if (ytsResponse.results.resultCount == 0) {
                    // TODO: Add movie to waiting list
                    processEmitter.fail(new InterruptedException("No match found on yts"));
                    endProcess();
                    return;
                }

                // Build a list of torrents available to download
                var availableTorrents = new ArrayList<YtsMovieTorrent>();
                for (YtsMovie movie : ytsResponse.results.movies) {
                    if (movie.imdbCode.equalsIgnoreCase(movieToDownload.imdbID)) {
                        availableTorrents.addAll(movie.torrents);
                    }
                }

                // Verify that there are torrents available to download, otherwise fail
                if (availableTorrents.isEmpty()) {
                    processEmitter.fail(new InterruptedException("No torrents found in yts movie listing"));
                    endProcess();
                    return;
                }

                // Generate a magnet
                var selectedTorrent = selectTorrent(availableTorrents);
                var magnet = "magnet:?xt=urn:btih:" + selectedTorrent.hash + "&tr=udp://open.demonii.com:1337/announce&tr=" +
                        "udp://tracker.openbittorrent.com:80&tr=udp://tracker.coppersurfer.tk:6969&tr=" +
                        "udp://glotorrents.pw:6969/announce&tr=udp://tracker.opentrackr.org:1337/announce&tr=" +
                        "udp://torrent.gresille.org:80/announce&tr=udp://p4p.arenabg.com:1337&tr=" +
                        "udp://tracker.leechers-paradise.org:6969";

                // Emit the updated status
                processEmitter.emit(Map.of(
                        MovieDownloadSteps.MASK_DOWNLOAD_INIT,
                        messageFormatter.formatDownloadProgressMessage(
                                movieToDownload, MovieDownloadSteps.MASK_DOWNLOAD_INIT)
                ));

                // Add the generated magnet to real-debrid to get a link and ensure it is valid
                var rdbMagnetLink = rdbService.addMagnet(magnet);

                // Get the information about the torrent file now in real-debrid
                var torrentInformation = rdbService.getTorrentInfo(rdbMagnetLink.id);

                // Make sure the magnet was added successfully and that it has finished its conversion
                if (torrentInformation.status == RdbTorrentStatusEnum.ERROR || torrentInformation.status == RdbTorrentStatusEnum.MAGNET_ERROR) {
                    processEmitter.fail(new InterruptedException("Adding movie to real-debrid failed: " + torrentInformation.status));
                    endProcess();
                    return;
                } else if (torrentInformation.status == RdbTorrentStatusEnum.MAGNET_CONVERSION) {
                    while (torrentInformation.status == RdbTorrentStatusEnum.MAGNET_CONVERSION) {
                        torrentInformation = rdbService.getTorrentInfo(rdbMagnetLink.id);
                    }
                }

                // Choose the files to download
                var selectedFiles = new ArrayList<RdbTorrentFile>();
                for (RdbTorrentFile file : torrentInformation.files) {
                    if (file.path.toLowerCase().endsWith(FileTypes.AVI.getExtension())) {
                        selectedFiles.add(file);
                    } else if (file.path.toLowerCase().endsWith(FileTypes.DIVX.getExtension())) {
                        selectedFiles.add(file);
                    } else if (file.path.toLowerCase().endsWith(FileTypes.FLV.getExtension())) {
                        selectedFiles.add(file);
                    } else if (file.path.toLowerCase().endsWith(FileTypes.M4V.getExtension())) {
                        selectedFiles.add(file);
                    } else if (file.path.toLowerCase().endsWith(FileTypes.MKV.getExtension())) {
                        selectedFiles.add(file);
                    } else if (file.path.toLowerCase().endsWith(FileTypes.MP4.getExtension())) {
                        selectedFiles.add(file);
                    } else if (file.path.toLowerCase().endsWith(FileTypes.MPEG.getExtension())) {
                        selectedFiles.add(file);
                    } else if (file.path.toLowerCase().endsWith(FileTypes.MPG.getExtension())) {
                        selectedFiles.add(file);
                    } else if (file.path.toLowerCase().endsWith(FileTypes.WMV.getExtension())) {
                        selectedFiles.add(file);
                    } else if (file.path.toLowerCase().endsWith(FileTypes.SRT.getExtension())) {
                        selectedFiles.add(file);
                    } else if (file.path.toLowerCase().endsWith(FileTypes.SMI.getExtension())) {
                        selectedFiles.add(file);
                    } else if (file.path.toLowerCase().endsWith(FileTypes.SSA.getExtension())) {
                        selectedFiles.add(file);
                    } else if (file.path.toLowerCase().endsWith(FileTypes.ASS.getExtension())) {
                        selectedFiles.add(file);
                    } else if (file.path.toLowerCase().endsWith(FileTypes.VTT.getExtension())) {
                        selectedFiles.add(file);
                    }
                }

                // Add the appropriate number of torrents to real-debrid
                if ((selectedFiles.size() - 1) > 0) {
                    // Add the torrent already being used to the list
                    finalTorrentList.add(torrentInformation);

                    // Create new torrents so there are enough for each of the files
                    for (int i = 0; i < (selectedFiles.size() - 1); i++) {
                        rdbMagnetLink = rdbService.addMagnet(magnet);
                        finalTorrentList.add(rdbService.getTorrentInfo(rdbMagnetLink.id));
                    }
                } else {
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

                    while (updatedTorrentInfo.status != RdbTorrentStatusEnum.DOWNLOADED) {
                        if (lastUpdated.plus(5, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                            updatedTorrentInfo = rdbService.getTorrentInfo(entry.getKey().id);

                            if (updatedTorrentInfo.status == RdbTorrentStatusEnum.WAITING_FILES_SELECTION || updatedTorrentInfo.status == RdbTorrentStatusEnum.QUEUED) {
                                processEmitter.emit(Map.of(
                                        MovieDownloadSteps.MASK_DOWNLOAD_INIT,
                                        messageFormatter.formatDownloadProgressMessage(
                                                movieToDownload,
                                                MovieDownloadSteps.MASK_DOWNLOAD_INIT
                                        )
                                ));
                            } else if (updatedTorrentInfo.status == RdbTorrentStatusEnum.DOWNLOADING) {
                                processEmitter.emit(Map.of(
                                        MovieDownloadSteps.MASK_DOWNLOAD_DOWNLOADING,
                                        messageFormatter.formatDownloadProgressMessage(
                                                movieToDownload,
                                                MovieDownloadSteps.MASK_DOWNLOAD_DOWNLOADING,
                                                updatedTorrentInfo.progress
                                        )
                                ));
                            } else if (updatedTorrentInfo.status == RdbTorrentStatusEnum.VIRUS ||
                                    updatedTorrentInfo.status == RdbTorrentStatusEnum.ERROR ||
                                    updatedTorrentInfo.status == RdbTorrentStatusEnum.MAGNET_ERROR ||
                                    updatedTorrentInfo.status == RdbTorrentStatusEnum.DEAD) {
                                processEmitter.fail(new InterruptedException("Torrent is in a failed state: " + updatedTorrentInfo.status));
                                endProcess();
                                return;
                            } else {
                                processEmitter.emit(Map.of(
                                        MovieDownloadSteps.MASK_DOWNLOAD_PROCESSING,
                                        messageFormatter.formatDownloadProgressMessage(
                                                movieToDownload,
                                                MovieDownloadSteps.MASK_DOWNLOAD_PROCESSING
                                        )
                                ));
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
                
                processEmitter.emit(Map.of(
                        MovieDownloadSteps.MASK_DOWNLOAD_PROCESSING,
                        messageFormatter.formatDownloadProgressMessage(
                                movieToDownload,
                                MovieDownloadSteps.MASK_DOWNLOAD_PROCESSING
                        )
                ));

                // Unrestrict the file links
                var unrestrictedLinks = new HashMap<RdbTorrent, RdbUnrestrictedLink>();
                long totalDownloadSize = 0;
                for (Map.Entry<RdbTorrent, RdbTorrentFile> entry : selectedTorrentMapping.entrySet()) {
                    var link = rdbService.unrestrictLink(String.valueOf(entry.getKey().links.get(0)));
                    unrestrictedLinks.put(entry.getKey(), link);
                    totalDownloadSize = totalDownloadSize + link.filesize;
                }

                // Download the files
                var filenames = new HashMap<String, FileTypes>();
                AtomicBoolean downloadFailed = new AtomicBoolean(false);
                for (Map.Entry<RdbTorrent, RdbUnrestrictedLink> entry : unrestrictedLinks.entrySet()) {
                    // Normalize the filename to lowercase letters
                    var filename = entry.getValue().filename;

                    // Add selected files to their proper HashMap
                    FileTypes fileType;
                    if (filename.toLowerCase().endsWith(FileTypes.AVI.getExtension())) {
                        fileType = FileTypes.AVI;
                    } else if (filename.toLowerCase().endsWith(FileTypes.DIVX.getExtension())) {
                        fileType = FileTypes.DIVX;
                    } else if (filename.toLowerCase().endsWith(FileTypes.FLV.getExtension())) {
                        fileType = FileTypes.FLV;
                    } else if (filename.toLowerCase().endsWith(FileTypes.M4V.getExtension())) {
                        fileType = FileTypes.M4V;
                    } else if (filename.toLowerCase().endsWith(FileTypes.MKV.getExtension())) {
                        fileType = FileTypes.MKV;
                    } else if (filename.toLowerCase().endsWith(FileTypes.MP4.getExtension())) {
                        fileType = FileTypes.MP4;
                    } else if (filename.toLowerCase().endsWith(FileTypes.MPEG.getExtension())) {
                        fileType = FileTypes.MPEG;
                    } else if (filename.toLowerCase().endsWith(FileTypes.MPG.getExtension())) {
                        fileType = FileTypes.MPG;
                    } else if (filename.toLowerCase().endsWith(FileTypes.WMV.getExtension())) {
                        fileType = FileTypes.WMV;
                    } else if (filename.toLowerCase().endsWith(FileTypes.SRT.getExtension())) {
                        fileType = FileTypes.SRT;
                    } else if (filename.toLowerCase().endsWith(FileTypes.SMI.getExtension())) {
                        fileType = FileTypes.SMI;
                    } else if (filename.toLowerCase().endsWith(FileTypes.SSA.getExtension())) {
                        fileType = FileTypes.SSA;
                    } else if (filename.toLowerCase().endsWith(FileTypes.ASS.getExtension())) {
                        fileType = FileTypes.ASS;
                    } else if (filename.toLowerCase().endsWith(FileTypes.VTT.getExtension())) {
                        fileType = FileTypes.VTT;
                    } else {
                        processEmitter.fail(new InterruptedException("File is of unknown type: " + filename));
                        endProcess();
                        return;
                    }

                    // If file is a video file, rename it to use the movie information
                    if (fileType.isVideo()) {
                        filename = fileUtilities.generateFilename(movieToDownload, fileType);
                    }

                    // Add the filename that will be downloaded to the list of filenames for processing later
                    filenames.put(filename, fileType);

                    long finalTotalDownloadSize = totalDownloadSize;
                    fileUtilities.downloadFile(String.valueOf(entry.getValue().download), filename).subscribe().with(
                            progress -> {
                                var percentage = (((double) progress / finalTotalDownloadSize) * 100);
                                processEmitter.emit(Map.of(
                                        MovieDownloadSteps.DOWNLOAD_MOVIE,
                                        messageFormatter.formatDownloadProgressMessage(
                                                movieToDownload,
                                                MovieDownloadSteps.DOWNLOAD_MOVIE,
                                                percentage
                                        )
                                ));
                            },
                            failure -> {
                                processEmitter.fail(new InterruptedException("Failed to download file: " + entry.getValue().filename));
                                downloadFailed.set(true);
                            },
                            () -> rdbService.deleteTorrent(entry.getKey().id)
                    );

                    if (downloadFailed.get()) {
                        endProcess();
                        return;
                    }
                }

                // Ensure folder is created
                var folderCreated = fileUtilities.createFolder(movieToDownload);
                if (!folderCreated) {
                    processEmitter.fail(new InterruptedException("Failed to create the media folder"));
                    endProcess();
                    return;
                }

                processEmitter.emit(Map.of(
                        MovieDownloadSteps.IMPORT_MOVIE,
                        messageFormatter.formatDownloadProgressMessage(
                                movieToDownload,
                                MovieDownloadSteps.IMPORT_MOVIE
                        )
                ));

                // Move files into their proper place
                var moveSucceeded = false;
                for (Map.Entry<String, FileTypes> entry : filenames.entrySet()) {
                    // Move the media
                    moveSucceeded = fileUtilities.moveMedia(
                            tempFolder + entry.getKey(),
                            movieFolder + fileUtilities.generateFilename(movieToDownload) + "/" + entry.getKey(),
                            false
                    );

                    // Fail if the move failed for some reason
                    if (!moveSucceeded) {
                        processEmitter.fail(new InterruptedException("Failed to move media file: " + entry.getKey()));
                        endProcess();
                        return;
                    }
                }

                // Add movie to the database
                for (Map.Entry<String, FileTypes> entry : filenames.entrySet()) {
                    if (entry.getValue().isVideo()) {
                        try {
                            addMovie(movieToDownload, entry);
                        } catch (StringIndexOutOfBoundsException e) {
                            processEmitter.fail(new InterruptedException("File is corrupted: " + entry.getKey()));
                            endProcess();
                            return;
                        }
                    }
                }

                endProcess();
                processEmitter.complete();
            } catch (Exception e) {
                // Delete torrents from real-debrid if there was an error
                if (!finalTorrentList.isEmpty()) {
                    for (RdbTorrent torrent : finalTorrentList) {
                        rdbService.deleteTorrent(torrent.id);
                    }
                }

                // Fail the process
                processEmitter.fail(e);
                reportError(e);
                endProcess();
            }
        });
    }

    @Transactional
    public void addMovie(OmdbResult movieToAdd, Map.Entry<String, FileTypes> entry) {
        var movieFileData = fileUtilities.getMediaInfo(movieFolder + fileUtilities.generateFilename(movieToAdd) + "/" + entry.getKey());
        Movie movie = new Movie();

        // Configure the new movie items
        movie.id = movieToAdd.imdbID;
        movie.title = movieToAdd.title;
        movie.year = movieToAdd.year;
        movie.resolution = movieFileData.resolution();
        movie.height = movieFileData.resolution();
        movie.width = movieFileData.width;
        movie.duration = movieFileData.duration;
        movie.codec = movieFileData.codec;
        movie.filename = entry.getKey();
        movie.filetype = entry.getValue().getExtension().replace(".", "");
        movie.folderName = fileUtilities.generateFilename(movieToAdd);
        movie.isOptimized = movieFileData.isOptimized();

        // Save movie to the database
        movie.persist();
    }

    private YtsMovieTorrent selectTorrent(List<YtsMovieTorrent> availableTorrents) {
        var selectedTorrent = new YtsMovieTorrent();

        for (YtsMovieTorrent torrent : availableTorrents) {
            if (torrent.quality.contains("720")) {
                selectedTorrent = torrent;
            }
        }

        for (YtsMovieTorrent torrent : availableTorrents) {
            if (torrent.quality.contains("720") && torrent.quality.contains("bluray")) {
                selectedTorrent = torrent;
            }
        }

        for (YtsMovieTorrent torrent : availableTorrents) {
            if (torrent.quality.contains("1080")) {
                selectedTorrent = torrent;
            }
        }

        for (YtsMovieTorrent torrent : availableTorrents) {
            if (torrent.quality.contains("1080") && torrent.quality.contains("bluray")) {
                selectedTorrent = torrent;
            }
        }

        for (YtsMovieTorrent torrent : availableTorrents) {
            if (torrent.quality.contains("2160")) {
                selectedTorrent = torrent;
            }
        }

        for (YtsMovieTorrent torrent : availableTorrents) {
            if (torrent.quality.contains("2160") && torrent.quality.contains("bluray")) {
                selectedTorrent = torrent;
            }
        }

        return selectedTorrent;
    }
}