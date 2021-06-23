package net.celestialdata.plexbot.periodictasks;

import io.quarkus.scheduler.Scheduled;
import net.celestialdata.plexbot.clients.models.omdb.enums.OmdbResponseEnum;
import net.celestialdata.plexbot.clients.models.yts.YtsMovie;
import net.celestialdata.plexbot.clients.models.yts.YtsMovieTorrent;
import net.celestialdata.plexbot.clients.services.OmdbService;
import net.celestialdata.plexbot.clients.services.PlexService;
import net.celestialdata.plexbot.clients.services.YtsService;
import net.celestialdata.plexbot.dataobjects.BotEmojis;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.entities.EntityUtilities;
import net.celestialdata.plexbot.entities.Movie;
import net.celestialdata.plexbot.entities.UpgradableMovie;
import net.celestialdata.plexbot.processors.MovieDownloadProcessor;
import net.celestialdata.plexbot.utilities.BotProcess;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ResolutionChecker extends BotProcess {
    private final DecimalFormat decimalFormatter = new DecimalFormat("#0.00");

    @Inject
    Instance<MovieDownloadProcessor> movieDownloadProcessor;

    @Inject
    @RestClient
    YtsService ytsService;

    @Inject
    @RestClient
    OmdbService omdbService;

    @Inject
    @RestClient
    PlexService plexService;

    @Inject
    DiscordApi discordApi;

    @ConfigProperty(name = "ApiKeys.omdbApiKey")
    String omdbApiKey;

    @ConfigProperty(name = "ChannelSettings.upgradeApprovalChannel")
    String upgradeApprovalChannel;

    @ConfigProperty(name = "ChannelSettings.upgradeNotificationChannel")
    String upgradeNotificationChannel;

    @ConfigProperty(name = "FolderSettings.movieFolder")
    String movieFolder;

    @Inject
    EntityUtilities entityUtilities;

    @Inject
    FileUtilities fileUtilities;

    @Inject
    Instance<ManagedExecutor> managedExecutor;

    @Inject
    MessageFormatter messageFormatter;

    @Scheduled(every = "12h", delay = 10, delayUnit = TimeUnit.SECONDS)
    public void runCheck() {
        configureProcess("Resolution Checker");
        int progress = 0;

        try {
            // Fetch all the movies in the database
            List<Movie> movies = Movie.listAll();

            // Process all movies and see if one has a better version available on YTS
            for (Movie movie : movies) {
                updateProcessString("Resolution Checker - " + decimalFormatter.format(((double) progress / movies.size()) * 100) + "%");
                try {
                    // Search for the movie on YTS
                    var ytsResponse = ytsService.search(movie.id);

                    // Verify that the search was successful, otherwise move to the next movie
                    if (!ytsResponse.status.equals("ok")) {
                        progress++;
                        continue;
                    }

                    // Verify that the search returned results, otherwise continue to the next movie
                    if (ytsResponse.results.resultCount == 0 || ytsResponse.results.movies == null) {
                        progress++;
                        continue;
                    }

                    // Build a list of torrents available to download
                    var availableTorrents = new ArrayList<YtsMovieTorrent>();
                    for (YtsMovie ytsMovie : ytsResponse.results.movies) {
                        if (ytsMovie.imdbCode.equalsIgnoreCase(movie.id)) {
                            availableTorrents.addAll(ytsMovie.torrents);
                        }
                    }

                    // Verify that there are torrents available to download, otherwise continue to the next movie
                    if (availableTorrents.isEmpty()) {
                        progress++;
                        continue;
                    }

                    // Select the highest quality torrent
                    var selectedTorrent = movieDownloadProcessor.get().selectTorrent(availableTorrents);

                    // Make sure that a valid movie torrent was actually located
                    if (selectedTorrent.quality == null) {
                        progress++;
                        continue;
                    }

                    // Compare the selected torrent to see if its better than what is on the server.
                    // Add the movie to the upgrade requests if it is a better resolution than what currently exists.
                    if (Integer.parseInt(selectedTorrent.quality.replace("p", "")) > movie.resolution) {
                        // Get information about the movie from OMDb
                        var omdbResult = omdbService.getById(movie.id, omdbApiKey);

                        // Verify the OMDb search was successful, if not move to the next movie
                        if (omdbResult.response == OmdbResponseEnum.FALSE) {
                            progress++;
                            continue;
                        }

                        // Add the movie to the database and message channel if it does not exist already
                        if (!entityUtilities.upgradableMovieExists(movie.id)) {
                            entityUtilities.addUpgradeableMovie(omdbResult, movie.resolution, Integer.parseInt(selectedTorrent.quality.replace("p", "")));
                        }
                    }
                } catch (Exception e) {
                    reportError(e);
                    progress++;
                    continue;
                }

                progress++;
            }

            // Cycle through all the movies listed as upgradable and start a download process to upgrade them if they have been approved
            List<UpgradableMovie> upgradableMovies = UpgradableMovie.listAll();
            for (UpgradableMovie movie : upgradableMovies) {
                try {
                    discordApi.getTextChannelById(upgradeApprovalChannel).flatMap(textChannel -> textChannel.getMessageById(movie.messageId).join()
                            .getReactionByEmoji(BotEmojis.THUMBS_UP)).ifPresent(reaction -> {

                                // Get information about this movie from OMDb
                                var omdbResult = omdbService.getById(movie.id, omdbApiKey);

                                // Verify the OMDb search was successful, if not move to the next movie
                                if (omdbResult.response == OmdbResponseEnum.FALSE) {
                                    return;
                                }

                                // Fetch the current instance of the movie from the database
                                Movie oldMovie = Movie.findById(movie.id);

                                // Rename the current file to use the .bak extension in the event something goes wrong
                                var moveStatus = false;
                                if (Files.exists(Paths.get(movieFolder + oldMovie.folderName + "/" + oldMovie.filename))) {
                                    moveStatus = fileUtilities.moveMedia(
                                            movieFolder + oldMovie.folderName + "/" + oldMovie.filename,
                                            movieFolder + oldMovie.folderName + "/" + oldMovie.filename + ".bak",
                                            true
                                    );
                                } else if (Files.exists(Paths.get(movieFolder + oldMovie.folderName + "/" + oldMovie.filename + ".bak"))) {
                                    moveStatus = true;
                                }

                                // Ensure the file was rename to the .bak extension properly
                                if (!moveStatus) {
                                    return;
                                }

                                movieDownloadProcessor.get().processDownload(omdbResult).runSubscriptionOn(managedExecutor.get()).subscribe().with(
                                        process -> {},
                                        this::reportError,
                                        () -> {
                                            // Delete the old file
                                            fileUtilities.deleteFile(movieFolder + oldMovie.folderName + "/" + oldMovie.filename + ".bak");

                                            // Send a upgrade notification
                                            new MessageBuilder()
                                                    .setEmbed(messageFormatter.upgradedNotification(omdbResult, oldMovie.resolution, movie.resolution))
                                                    .send(discordApi.getTextChannelById(upgradeNotificationChannel).orElseThrow())
                                                    .exceptionally(ExceptionLogger.get());

                                            // Update the channel status to show the last time this check was run
                                            discordApi.getTextChannelById(upgradeApprovalChannel)
                                                    .flatMap(Channel::asServerTextChannel).ifPresent(serverTextChannel -> serverTextChannel.updateTopic(
                                                    "Last checked: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                                                            .format(ZonedDateTime.now()) + " CST"));

                                            // Delete the upgradable movie entry from the database
                                            entityUtilities.deleteUpgradeMovie(movie.id);

                                            // Trigger a refresh of the libraries on the Plex server
                                            plexService.refreshLibraries();
                                        }
                                );
                            });
                } catch (Exception e) {
                    reportError(e);
                }
            }
        } catch (Exception e) {
            reportError(e);
        }

        endProcess();
    }


}