package net.celestialdata.plexbot.processors;

import io.quarkus.arc.log.LoggerName;
import io.smallrye.mutiny.Multi;
import net.celestialdata.plexbot.clients.models.omdb.OmdbResult;
import net.celestialdata.plexbot.clients.models.rdb.RdbTorrentFile;
import net.celestialdata.plexbot.clients.models.yts.YtsMovie;
import net.celestialdata.plexbot.clients.models.yts.YtsMovieTorrent;
import net.celestialdata.plexbot.clients.services.OmdbService;
import net.celestialdata.plexbot.clients.services.RdbService;
import net.celestialdata.plexbot.clients.services.YtsService;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.utilities.BotProcess;
import net.celestialdata.plexbot.utilities.MovieDownloadSteps;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MovieDownloadProcessor extends BotProcess {

    @LoggerName(value = "net.celestialdata.plexbot.processors.MovieDownloadProcessor")
    Logger logger;

    @Inject
    MessageFormatter messageFormatter;

    @Inject
    @RestClient
    OmdbService omdbService;

    @Inject
    @RestClient
    YtsService ytsService;

    @Inject
    @RestClient
    RdbService rdbService;

    public Multi<Map<MovieDownloadSteps, EmbedBuilder>> processDownload(OmdbResult movieToDownload, Message statusMessage) {
        configureProcess("Download " + movieToDownload.title + "(" + movieToDownload.year + ") - 5.00%", statusMessage);
        return processDownload(movieToDownload);
    }

    double overallCompletionPercentageCalculator(double selectStage, double locateStage, double maskStage, double downloadStage, double importStage, boolean maskAlteration) {
        if (maskAlteration) {
            return ((((selectStage * 100) + (locateStage * 5) + (maskStage * 40) + (downloadStage * 40) + (importStage * 10)) / 100) / 100);
        } else return ((((selectStage * 100) + (locateStage * 5) + (maskStage * 15) + (downloadStage * 65) + (importStage * 10)) / 100) / 100);
    }

    public Multi<Map<MovieDownloadSteps, EmbedBuilder>> processDownload(OmdbResult movieToDownload) {
        DecimalFormat decimalFormatter = new DecimalFormat("#0.00");
        var processName = "Download " + movieToDownload.title + "(" + movieToDownload.year + ") - ";

        // Configure the process if it is now already configured
        if (processId == null) {
            configureProcess(processName);
        }

        // Update the process string in the BotStatusManager to show overall percentage
        updateProcessString(processName + decimalFormatter.format(overallCompletionPercentageCalculator(
                5, 0, 0, 0, 0, false
        )));

        return Multi.createFrom().emitter(processEmitter -> {
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

            // Update the overall process in the BotStatusManager
            updateProcessString(processName + decimalFormatter.format(overallCompletionPercentageCalculator(
                    5, 1.5, 0, 0, 0, false
            )));

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

            // Update the overall process in the BotStatusManager
            updateProcessString(processName + decimalFormatter.format(overallCompletionPercentageCalculator(
                    5, 3.5, 0, 0, 0, false
            )));

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

            // Update the overall process in the BotStatusManager
            updateProcessString(processName + decimalFormatter.format(overallCompletionPercentageCalculator(
                    5, 5, 0, 0, 0, false
            )));

            // Emit the updated status
            processEmitter.emit(Map.of(
                    MovieDownloadSteps.MASK_DOWNLOAD,
                    messageFormatter.formatDownloadProgressMessage(
                            movieToDownload, MovieDownloadSteps.MASK_DOWNLOAD)
            ));

            // Add the generated magnet to real-debrid to get a link and ensure it is valid
            var rdbMagnetLink = rdbService.addMagnet(magnet);

            // Get the information about the torrent file now in real-debrid
            var torrentInformation = rdbService.getTorrentInfo(rdbMagnetLink.id);

            // Choose the files to download
            /* TODO: Select subtitle files and allow them to be downloaded as well.
                    This will require changing from a single string to a comma-separated list of strings.
             */
            var selectedFile = "";
            var fileExtension = "";
            for (RdbTorrentFile file : torrentInformation.files) {
                if (file.path.endsWith(".mp4") || file.path.endsWith(".MP4")) {
                    selectedFile = String.valueOf(file.id);
                    fileExtension = ".mp4";
                } else if (file.path.endsWith(".mkv") || file.path.endsWith(".MKV")) {
                    selectedFile = String.valueOf(file.id);
                    fileExtension = ".mkv";
                }
            }

            // Select the file to download on real-debrid
            /* TODO: Make this loop through selected files in above todo then create a new torrent entry for each and
                    select only one file from the selection. This will ensure files are not compressed into a rar file.
             */
            rdbService.selectFiles(torrentInformation.id, selectedFile);

            // Allow real-debrid to download the file to their servers and mask it

            endProcess();
        });
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