package net.celestialdata.plexbot.periodictasks;

import io.quarkus.scheduler.Scheduled;
import net.celestialdata.plexbot.clients.services.TmdbService;
import net.celestialdata.plexbot.clients.services.YtsService;
import net.celestialdata.plexbot.db.daos.WaitlistMovieDao;
import net.celestialdata.plexbot.db.entities.WaitlistMovie;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.processors.MovieDownloadProcessor;
import net.celestialdata.plexbot.utilities.BotProcess;
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
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class WaitlistChecker extends BotProcess {
    private final DecimalFormat decimalFormatter = new DecimalFormat("#0.00");

    @ConfigProperty(name = "ChannelSettings.movieWaitlistChannel")
    String movieWaitlistChannel;

    @ConfigProperty(name = "ChannelSettings.newMovieNotificationChannel")
    String newMovieNotificationChannel;

    @Inject
    MessageFormatter messageFormatter;

    @Inject
    @RestClient
    TmdbService tmdbService;

    @Inject
    @RestClient
    YtsService ytsService;

    @Inject
    DiscordApi discordApi;

    @Inject
    Instance<ManagedExecutor> managedExecutor;

    @Inject
    Instance<MovieDownloadProcessor> movieDownloadProcessor;

    @Inject
    WaitlistMovieDao waitlistMovieDao;

    @Scheduled(every = "6h", delay = 10, delayUnit = TimeUnit.SECONDS)
    public void runCheck() {
        configureProcess("Waitlist Checker");
        int progress = 0;

        try {
            // Fetch all the items in the database
            List<WaitlistMovie> movies = waitlistMovieDao.listALl();

            // Process all movies to see if one is now available for download on YTS
            for (WaitlistMovie movie : movies) {
                updateProcessString("Waitlist Checker - " + decimalFormatter.format(((double) progress / movies.size()) * 100) + "%");
                try {
                    // If the movie is already in the system, remove if from the waitlist
                    if (waitlistMovieDao.existsByTmdbId(movie.tmdbId)) {
                        waitlistMovieDao.delete(movie.id);
                        progress++;
                        continue;
                    }

                    // Search on YTS for the movie
                    var ytsResponse = ytsService.search(movie.imdbId);

                    // Get the information about the movie on imdb
                    var movieInfo = tmdbService.getMovie(movie.tmdbId);

                    // If the search failed or the movie was not found, continue to the next movie
                    if (!ytsResponse.status.equals("ok") || ytsResponse.results.resultCount == 0) {
                        discordApi.getTextChannelById(movieWaitlistChannel)
                                .ifPresent(textChannel ->
                                        textChannel.getMessageById(movie.messageId).join()
                                                .edit(messageFormatter.waitlistNotification(movieInfo))
                                );
                    } else {
                        movieDownloadProcessor.get().processDownload(movieInfo).runSubscriptionOn(managedExecutor.get()).subscribe().with(
                                process -> {},
                                failure -> discordApi.getTextChannelById(movieWaitlistChannel)
                                        .ifPresent(textChannel ->
                                                textChannel.getMessageById(movie.messageId).join()
                                                        .edit(messageFormatter.waitlistNotification(movieInfo))),
                                () -> {
                                    discordApi.getUserById(movie.requestedBy).join().sendMessage(messageFormatter.newMovieUserNotification(movieInfo));
                                    new MessageBuilder()
                                            .setEmbed(messageFormatter.newMovieNotification(movieInfo))
                                            .send(discordApi.getTextChannelById(newMovieNotificationChannel).orElseThrow()).exceptionally(ExceptionLogger.get()
                                    ).exceptionally(ExceptionLogger.get()).join();
                                    waitlistMovieDao.delete(movie.id);
                                }
                        );
                    }
                } catch (Exception e) {
                    reportError(e);
                }

                progress++;
            }
        } catch (Exception e) {
            reportError(e);
        }

        discordApi.getTextChannelById(movieWaitlistChannel)
                .flatMap(Channel::asServerTextChannel).ifPresent(serverTextChannel -> serverTextChannel.updateTopic(
                "Last checked: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        .format(ZonedDateTime.now()) + " CST"));

        endProcess();
    }
}