package net.celestialdata.plexbot.utilities;

import io.smallrye.mutiny.Multi;
import net.celestialdata.plexbot.clients.models.tmdb.TmdbMovie;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbEpisode;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbSeries;
import net.celestialdata.plexbot.dataobjects.BlacklistedCharacters;
import net.celestialdata.plexbot.dataobjects.MediaInfoData;
import net.celestialdata.plexbot.dataobjects.ParsedSubtitleFilename;
import net.celestialdata.plexbot.db.entities.Episode;
import net.celestialdata.plexbot.db.entities.Movie;
import net.celestialdata.plexbot.db.entities.Show;
import net.celestialdata.plexbot.enumerators.FileType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import uk.co.caprica.vlcjinfo.MediaInfoFile;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@ApplicationScoped
public class FileUtilities {

    @SuppressWarnings("CdiInjectionPointsInspection")
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
                    renameMedia(tempPath.toString(), tempPath.toString().replace(".pbdownload", ""), true);
                }

                multiEmitter.complete();
            } catch (IOException e) {
                e.printStackTrace();
                multiEmitter.fail(e);
            }
        });
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

    public boolean renameMedia(String source, String destination, boolean overwrite) {
        boolean success;

        // If the file exists and the overwrite flag is false, then do not write the file
        if (Files.exists(Paths.get(destination)) && !overwrite) {
            success = false;
        } else {
            try {
                success = new File(source).renameTo(new File(destination));
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
        }

        return success;
    }

    public boolean moveMedia(String source, String destination, boolean overwrite) {
        boolean success = true;

        // If the file exists and the overwrite flag is false, then do not write the file
        if (Files.exists(Paths.get(destination)) && !overwrite) {
            success = false;
        } else {
            try {
                success = new File(source).renameTo(new File(destination));
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
        }

        return success;
    }

    public boolean createFolder(TmdbMovie mediaItem) {
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean createFolder(TvdbSeries show) {
        boolean success = true;

        try {
            Files.createDirectory(Paths.get(tvFolder + generatePathname(show)));
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

    public String generatePathname(TmdbMovie mediaItem) {
        return sanitizeFilesystemNames(mediaItem.title + " (" + mediaItem.getYear() + ") {tmdb-" + mediaItem.tmdbId + "}");
    }

    public String generatePathname(TvdbSeries mediaItem) {
        return sanitizeFilesystemNames(mediaItem.name + " {tvdb-" + mediaItem.id + "}");
    }

    public String generateMovieFilename(TmdbMovie mediaItem, FileType fileType) {
        return generatePathname(mediaItem) + fileType.getExtension();
    }

    public String subtitleSuffixBuilder(ParsedSubtitleFilename parsedFilename) {
        // Create the StringBuilder used to build the file suffix
        var suffixBuilder = new StringBuilder();

        // Append the language code
        suffixBuilder.append(".").append(parsedFilename.language);

        // Add the .sdh or .cc flags if the applicable
        if (parsedFilename.isSDH) {
            suffixBuilder.append(".").append("sdh");
        } else if (parsedFilename.isCC) {
            suffixBuilder.append(".").append("cc");
        }

        // Add the .forced flag if applicable
        if (parsedFilename.isForced) {
            suffixBuilder.append(".").append("forced");
        }

        // Add the file extension
        suffixBuilder.append(parsedFilename.fileType.getExtension());

        return suffixBuilder.toString();
    }

    public String generateMovieSubtitleFilename(TmdbMovie linkedMovie, ParsedSubtitleFilename parsedFilename) {
        return generatePathname(linkedMovie) + subtitleSuffixBuilder(parsedFilename);
    }

    public String buildSeasonAndEpisodeString(TvdbEpisode episode, Show show) {
        StringBuilder seasonAndEpisodeString = new StringBuilder();

        // Build the season part of the string
        if (episode.seasonNumber > 9) {
            seasonAndEpisodeString.append("s");
        } else seasonAndEpisodeString.append("s0");
        seasonAndEpisodeString.append(episode.seasonNumber);

        // Build the episode part of the string
        if (episode.number > 9) {
            seasonAndEpisodeString.append("e");
        } else seasonAndEpisodeString.append("e0");
        seasonAndEpisodeString.append(episode.number);

        return seasonAndEpisodeString.toString();
    }

    public String buildSeasonAndEpisodeString(int episodeNumber, int seasonNumber) {
        StringBuilder seasonAndEpisodeString = new StringBuilder();

        // Build the season part of the string
        if (seasonNumber > 9) {
            seasonAndEpisodeString.append("s");
        } else seasonAndEpisodeString.append("s0");
        seasonAndEpisodeString.append(seasonNumber);

        // Build the episode part of the string
        if (episodeNumber > 9) {
            seasonAndEpisodeString.append("e");
        } else seasonAndEpisodeString.append("e0");
        seasonAndEpisodeString.append(episodeNumber);

        return seasonAndEpisodeString.toString();
    }

    public String generateEpisodeFilename(TvdbEpisode episode, Show show, FileType fileType) {
        if (StringUtils.isBlank(episode.name)) {
            return sanitizeFilesystemNames(show.name + " - " + buildSeasonAndEpisodeString(episode, show)) + fileType.getExtension();
        } else return sanitizeFilesystemNames(show.name + " - " + buildSeasonAndEpisodeString(episode, show) + " - " + episode.name) + fileType.getExtension();
    }

    public String generateEpisodeSubtitleFilename(TvdbEpisode linkedEpisode, Show linkedShow, ParsedSubtitleFilename parsedFilename) {
        return sanitizeFilesystemNames(linkedShow.name + " - " + buildSeasonAndEpisodeString(linkedEpisode, linkedShow)) + subtitleSuffixBuilder(parsedFilename);
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

    public MediaInfoData getMediaInfo(Movie movie) {
        return getMediaInfo(movieFolder + movie.folderName + "/" + movie.filename);
    }

    public MediaInfoData getMediaInfo(Episode episode) {
        return getMediaInfo(tvFolder + episode.show.foldername + "/Season " + episode.season + "/" + episode.filename);
    }

    public MediaInfoData getMediaInfo(String pathToVideo) {
        MediaInfoFile file = new MediaInfoFile(pathToVideo);
        MediaInfoData mediaInfoData = new MediaInfoData();

        if (file.open()) {
            // Get the unparsed duration and then calculate the duration to the nearest minute
            int duration;
            try {
                duration = Integer.parseInt(file.info("General;%Duration%"));
                if ((duration / 60000.0) < 0.5) {
                    duration = 0;
                } else if ((duration / 60000.0) < 1 && (duration / 60000.0) >= 0.5) {
                    duration = 1;
                } else {
                    duration = (duration / 60000) + ((((duration % 60000) / 1000) >= 30) ? 1 : 0);
                }
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Unable to determine playback length.");
            }

            // Get the codec type and verify it is not empty, if it is fetch and parse from another source
            var codecString = file.info("Video;%Encoded_Library_Name%");
            if (StringUtils.isBlank(codecString)) {
                codecString = file.info("Video;%InternetMediaType%").replace("video/", "");
            }

            // Set the media info information
            mediaInfoData.codec = codecString;
            mediaInfoData.width = Integer.parseInt(file.info("Video;%Width%"));
            mediaInfoData.height = Integer.parseInt(file.info("Video;%Height%"));
            mediaInfoData.duration = duration;
        }

        return mediaInfoData;
    }
}