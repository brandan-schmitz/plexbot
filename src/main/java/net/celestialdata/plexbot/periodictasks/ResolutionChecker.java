package net.celestialdata.plexbot.periodictasks;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import net.celestialdata.plexbot.clients.models.yts.YtsMovie;
import net.celestialdata.plexbot.clients.models.yts.YtsMovieTorrent;
import net.celestialdata.plexbot.clients.services.PlexService;
import net.celestialdata.plexbot.clients.services.TmdbService;
import net.celestialdata.plexbot.clients.services.YtsService;
import net.celestialdata.plexbot.db.daos.MovieDao;
import net.celestialdata.plexbot.db.daos.MovieSubtitleDao;
import net.celestialdata.plexbot.db.daos.UpgradableMovieDao;
import net.celestialdata.plexbot.db.entities.Movie;
import net.celestialdata.plexbot.db.entities.UpgradableMovie;
import net.celestialdata.plexbot.processors.MovieDownloadProcessor;
import net.celestialdata.plexbot.utilities.BotProcess;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
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
    PlexService plexService;

    @Inject
    @RestClient
    TmdbService tmdbService;

    @Inject
    DiscordApi discordApi;

    @ConfigProperty(name = "ChannelSettings.upgradeApprovalChannel")
    String upgradeApprovalChannel;

    @ConfigProperty(name = "ChannelSettings.upgradeNotificationChannel")
    String upgradeNotificationChannel;

    @ConfigProperty(name = "FolderSettings.movieFolder")
    String movieFolder;

    @Inject
    FileUtilities fileUtilities;

    @Inject
    Instance<ManagedExecutor> managedExecutor;

    @Inject
    UpgradableMovieDao upgradableMovieDao;

    @Inject
    MovieDao movieDao;

    @Inject
    MovieSubtitleDao movieSubtitleDao;

    public void init(@Observes StartupEvent event) {
        TextChannel upgradeChannel = discordApi.getTextChannelById(upgradeApprovalChannel).orElseThrow();
        upgradeChannel.addButtonClickListener(clickEvent -> {
            if (clickEvent.getButtonInteraction().getCustomId().equals("approve-upgrade") || clickEvent.getButtonInteraction().getCustomId().equals("ignore-upgrade")) {
                // Get the ID of the message triggering this event
                var messageId = clickEvent.getButtonInteraction().getMessageId();

                // Fetch the upgradable movie from the database
                UpgradableMovie upgradableMovie = upgradableMovieDao.getByMessageId(messageId);

                // Upgrade the movie if the upgrade button was clicked
                if (clickEvent.getButtonInteraction().getCustomId().equals("approve-upgrade")) {
                    // Start the upgrade process
                    upgradeMovie(upgradableMovie).runSubscriptionOn(managedExecutor.get()).subscribe().with(
                            completion -> {},
                            failure -> {
                                // If the process was interrupted by error or other cause, re-add it this movie to the database
                                if (failure instanceof InterruptedException) {
                                    var movie = upgradableMovie.movie;
                                    var newResolution = upgradableMovie.newResolution;
                                    upgradableMovieDao.delete(upgradableMovie.id);
                                    upgradableMovieDao.create(movie, newResolution);
                                }
                            }
                    );

                    // Update the message to reflect that the upgrade has been started
                    clickEvent.getInteraction().asMessageComponentInteraction().orElseThrow()
                            .createOriginalMessageUpdater()
                            .removeAllComponents()
                            .removeAllEmbeds()
                            .addEmbed(new EmbedBuilder()
                                    .setTitle("Upgrade approved")
                                    .setDescription("You have approved an upgrade to the following movie. It is currently " +
                                            "being processed, this message will be removed when the upgrade is completed.")
                                    .addField("Title:", "```" + upgradableMovie.movie.title + "```")
                                    .addInlineField("TMDB ID:", "```" + upgradableMovie.movie.tmdbId + " ```")
                                    .addInlineField("Old Resolution:", "```" + upgradableMovie.movie.resolution + "```")
                                    .addInlineField("New Resolution:", "```" + upgradableMovie.newResolution + "```")
                                    .setColor(Color.BLUE)
                            )
                            .update();
                }

                // Remove the upgradable movie if the ignore button was clicked
                if (clickEvent.getButtonInteraction().getCustomId().equals("ignore-upgrade")) {
                    upgradableMovieDao.delete(upgradableMovie.id);
                }
            }
        });
    }

    public Uni<Void> upgradeMovie(UpgradableMovie upgradableMovie) {
        return Uni.createFrom().emitter(emitter -> {
            // Get information about this movie from tmdb
            var movieData = tmdbService.getMovie(upgradableMovie.movie.tmdbId);

            // Fail the process if unable to fetch the movie details
            if (!movieData.isSuccessful()) {
                emitter.fail(new InterruptedException("Failed data retrieval"));
                return;
            }

            // Rename the current file to use the .bak extension in the event something goes wrong
            var moveStatus = false;
            if (Files.exists(Paths.get(movieFolder + upgradableMovie.movie.folderName + "/" + upgradableMovie.movie.filename))) {
                moveStatus = fileUtilities.moveMedia(
                        movieFolder + upgradableMovie.movie.folderName + "/" + upgradableMovie.movie.filename,
                        movieFolder + upgradableMovie.movie.folderName + "/" + upgradableMovie.movie.filename + ".bak",
                        true
                );
            } else if (Files.exists(Paths.get(movieFolder + upgradableMovie.movie.folderName + "/" + upgradableMovie.movie.filename + ".bak"))) {
                moveStatus = true;
            }

            // Ensure the file was renamed to the .bak extension properly
            if (!moveStatus) {
                emitter.fail(new InterruptedException("Unable to backup old video file"));
            }

            // Rename old subtitle files to use the .bak extension
            var subtitleList = new ArrayList<>(movieSubtitleDao.getByMovie(upgradableMovie.movie));
            subtitleList.forEach(subtitle -> fileUtilities.moveMedia(movieFolder + upgradableMovie.movie.folderName + "/" + subtitle.filename,
                    movieFolder + upgradableMovie.movie.folderName + "/" + subtitle.filename + ".bak", true));

            movieDownloadProcessor.get().processDownload(movieData).runSubscriptionOn(managedExecutor.get()).subscribe().with(
                    progress -> {},
                    this::reportError,
                    () -> {
                        // Delete the old files
                        fileUtilities.deleteFile(movieFolder + upgradableMovie.movie.folderName + "/" + upgradableMovie.movie.filename + ".bak");
                        subtitleList.forEach(subtitle -> {
                            fileUtilities.deleteFile(movieFolder + upgradableMovie.movie.folderName + "/" + subtitle.filename + ".bak");
                            movieSubtitleDao.delete(subtitle.id);
                        });

                        // Send a upgrade notification
                        new MessageBuilder()
                                .setEmbed(new EmbedBuilder()
                                        .setTitle("Movie Upgraded")
                                        .addField("Title:", "```" + movieData.title + "```")
                                        .addInlineField("TMDB ID:", "```" + movieData.tmdbId + "```")
                                        .addInlineField("Old Resolution:", "```" + upgradableMovie.movie.resolution + "```")
                                        .addInlineField("New Resolution:", "```" + upgradableMovie.newResolution + "```")
                                        .addField("Overview:", "```" + movieData.getOverview() + "```")
                                        .setImage(movieData.getPoster())
                                        .setColor(Color.GREEN))
                                .send(discordApi.getTextChannelById(upgradeNotificationChannel).orElseThrow())
                                .exceptionally(ExceptionLogger.get());

                        // Delete the upgradable movie entry from the database
                        upgradableMovieDao.delete(upgradableMovie.id);

                        // Trigger a refresh of the libraries on the Plex server
                        plexService.refreshLibraries();
                    }
            );

            emitter.complete(null);
        });
    }

    @Scheduled(every = "12h", delay = 10, delayUnit = TimeUnit.SECONDS)
    public void runCheck() {
        configureProcess("Resolution Checker");
        int progress = 0;

        try {
            // Fetch all the movies in the database
            List<Movie> movies = movieDao.listALl();

            // Process all movies and see if one has a better version available on YTS
            for (Movie movie : movies) {
                updateProcessString("Resolution Checker - " + decimalFormatter.format(((double) progress / movies.size()) * 100) + "%");
                try {
                    // Search for the movie on YTS
                    var ytsResponse = ytsService.search(movie.imdbId);

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
                        if (ytsMovie.imdbCode.equalsIgnoreCase(movie.imdbId)) {
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

                    // Compare the selected torrent to see if it is better than what is on the server.
                    // Add the movie to the upgrade requests if it is a better resolution than what currently exists.
                    if (Integer.parseInt(selectedTorrent.quality.replace("p", "")) > movie.resolution) {
                        // Get information about the movie from OMDb
                        var movieData = tmdbService.getMovie(movie.tmdbId);

                        // Verify the data retrieval was successful, if not move to the next movie
                        if (!movieData.isSuccessful()) {
                            progress++;
                            continue;
                        }

                        // Add the movie to the database and message channel if it does not exist already
                        upgradableMovieDao.create(movie, Integer.parseInt(selectedTorrent.quality.replace("p", "")));
                    }
                } catch (Exception e) {
                    reportError(e);
                    progress++;
                    continue;
                }

                progress++;
            }
        } catch (Exception e) {
            reportError(e);
        }

        endProcess();
    }
}