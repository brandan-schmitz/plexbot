package net.celestialdata.plexbot.utilities;

import io.smallrye.mutiny.Multi;
import net.celestialdata.plexbot.clients.models.omdb.OmdbResult;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbEpisode;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbExtendedEpisode;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbSeason;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbSeries;
import net.celestialdata.plexbot.dataobjects.BlacklistedCharacters;
import net.celestialdata.plexbot.dataobjects.MediaInfoData;
import net.celestialdata.plexbot.enumerators.FileType;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import uk.co.caprica.vlcjinfo.MediaInfoFile;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@ApplicationScoped
public class FileUtilities {

    @Inject
    BlacklistedCharacters blacklistedCharacters;

    @ConfigProperty(name = "FolderSettings.tempFolder")
    String tempFolder;

    @ConfigProperty(name = "FolderSettings.movieFolder")
    String movieFolder;

    @ConfigProperty(name = "FolderSettings.tvFolder")
    String tvFolder;

    public Multi<Long> downloadFile(String url, String outputFilename, String outputFoldername) {
        return Multi.createFrom().emitter(multiEmitter -> {
            long progress = 0;
            try {
                // Open a connection to the file being downloaded
                URLConnection connection = new URL(url).openConnection();
                var tempPath = Paths.get(tempFolder +
                        (outputFoldername != null ? (outputFoldername + "/" + outputFilename) : outputFilename) +
                        ".pbdownload"
                );

                // If the file has started being downloaded, start the download where it left off
                if (Files.exists(tempPath)) {
                    connection.setRequestProperty("Range", "bytes=" + Files.size(tempPath) + "-");
                    progress = Files.size(tempPath);
                }
                ReadableByteChannel readableByteChannel = Channels.newChannel(connection.getInputStream());
                FileChannel fileOutputStream = new FileOutputStream(String.valueOf(tempPath), Files.exists(tempPath)).getChannel();

                // Download the file
                var lastEmitted = LocalDateTime.now();
                while (!multiEmitter.isCancelled() && fileOutputStream.transferFrom(readableByteChannel, fileOutputStream.size(), 1024) > 0) {
                    progress += 1024;

                    // Send the progress every 3 seconds
                    if (lastEmitted.plus(3, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        multiEmitter.emit(progress);
                        lastEmitted = LocalDateTime.now();
                    }
                }

                // Close the connection to the server
                fileOutputStream.close();

                // Rename the file to specified name
                if (!multiEmitter.isCancelled()) {
                    moveMedia(tempPath.toString(), tempPath.toString().replace(".pbdownload", ""), true);
                }

                multiEmitter.complete();
            } catch (IOException e) {
                multiEmitter.fail(e);
            }
        });
    }

    public Multi<Long> downloadFile(String url, String outputFilename) {
        return downloadFile(url, outputFilename, null);
    }

    public boolean isFolderEmpty(String folder) {
        var path = Paths.get(folder);

        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return entries.findFirst().isEmpty();
            } catch (IOException e) {
                return false;
            }
        }

        return false;
    }

    public boolean moveMedia(String source, String destination, boolean overwrite) {
        boolean success = true;

        try {
            // Copy the file to the destination
            if (overwrite) {
                Files.copy(
                        Paths.get(source),
                        Paths.get(destination),
                        StandardCopyOption.REPLACE_EXISTING
                );
            } else {
                Files.copy(
                        Paths.get(source),
                        Paths.get(destination)
                );
            }

            // Delete the source file if the copy was successful
            Files.delete(Paths.get(source));
        } catch (Exception e) {
            success = false;
        }

        return success;
    }

    public boolean createFolder(OmdbResult mediaItem) {
        boolean success = true;

        try {
            Files.createDirectory(Paths.get(movieFolder + generatePathname(mediaItem)));
        } catch (IOException e) {
            if (!(e instanceof FileAlreadyExistsException)) {
                success = false;
            }
        }

        return success;
    }

    public boolean createFolder(String folderPath) {
        boolean success = true;

        try {
            Files.createDirectory(Paths.get(folderPath));
        } catch (IOException e) {
            if (!(e instanceof FileAlreadyExistsException)) {
                success = false;
            }
        }

        return success;
    }

    public boolean deleteFile(String file) {
        boolean success = true;

        try {
            Files.deleteIfExists(Paths.get(file));
        } catch (IOException e) {
            success = false;
        }

        return success;
    }

    public String generateFilename(OmdbResult mediaItem, FileType fileType) {
        return generatePathname(mediaItem) + fileType.getExtension();
    }

    public String generatePathname(OmdbResult mediaItem) {
        return sanitizeFilesystemNames(mediaItem.title + " (" + mediaItem.year + ") {imdb-" + mediaItem.imdbID + "}");
    }

    public String generatePathname(TvdbSeries mediaItem) {
        return sanitizeFilesystemNames(mediaItem.name + " {tvdb-" + mediaItem.id + "}");
    }

    public String generateEpisodeFilename(TvdbExtendedEpisode mediaItem, FileType fileType, String seasonAndEpisode, TvdbSeries series) {
        if (mediaItem.name == null || mediaItem.name.isBlank()) {
            return sanitizeFilesystemNames(series.name + " - " + seasonAndEpisode) + fileType.getExtension();
        } else return sanitizeFilesystemNames(series.name + " - " + seasonAndEpisode + " - " + mediaItem.name) + fileType.getExtension();
    }

    public String sanitizeFilesystemNames(String input) {
        // Remove any characters listed by the removal section
        for (String removalCharacter : blacklistedCharacters.remove()) {
            input = input.replace(removalCharacter, "");
        }

        // Replace and characters listed by the replacement policy
        for (BlacklistedCharacters.Replacements replacement : blacklistedCharacters.replace()) {
            input = input.replace(replacement.original(), replacement.replacement());
        }

        // Strip accents and return the sanitized string
        return StringUtils.stripAccents(input);
    }

    public MediaInfoData getMediaInfo(String pathToVideo) {
        MediaInfoFile file = new MediaInfoFile(pathToVideo);
        MediaInfoData mediaInfoData = new MediaInfoData();

        if (file.open()) {
            // Get the unparsed duration
            var durationString = file.info("General;%Duration/String3%");

            // Set the media info information
            mediaInfoData.codec = file.info("Video;%Encoded_Library_Name%");
            mediaInfoData.width = Integer.parseInt(file.info("Video;%Width%"));
            mediaInfoData.height = Integer.parseInt(file.info("Video;%Height%"));
            mediaInfoData.duration = (Integer.parseInt(durationString.substring(0, 2)) * 60) +
                    Integer.parseInt(durationString.substring(3, 5)) +
                    ((Integer.parseInt(durationString.substring(6, 8)) >= 30) ? 1 : 0);
        }

        return mediaInfoData;
    }
}