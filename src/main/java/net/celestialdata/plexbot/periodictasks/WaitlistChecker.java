package net.celestialdata.plexbot.periodictasks;

import io.quarkus.scheduler.Scheduled;
import net.celestialdata.plexbot.clients.services.OmdbService;
import net.celestialdata.plexbot.clients.services.YtsService;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.entities.EntityUtilities;
import net.celestialdata.plexbot.entities.WaitlistMovie;
import net.celestialdata.plexbot.processors.MovieDownloadProcessor;
import net.celestialdata.plexbot.utilities.BotProcess;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Dependent
public class WaitlistChecker extends BotProcess {
    private final DecimalFormat decimalFormatter = new DecimalFormat("#0.00");

    @ConfigProperty(name = "ChannelSettings.waitlistChannelId")
    String waitlistChannel;

    @ConfigProperty(name = "ApiKeys.omdbApiKey")
    String omdbApiKey;

    @ConfigProperty(name = "ChannelSettings.newMoviesChannelId")
    String newMoviesChannel;

    @Inject
    EntityUtilities entityUtilities;

    @Inject
    MessageFormatter messageFormatter;

    @Inject
    @RestClient
    OmdbService omdbService;

    @Inject
    @RestClient
    YtsService ytsService;

    @Inject
    DiscordApi discordApi;

    @Inject
    MovieDownloadProcessor movieDownloadProcessor = new MovieDownloadProcessor();

    @Scheduled(every = "6h", delay = 10, delayUnit = TimeUnit.SECONDS)
    public void runCheck() {
        configureProcess("Waitlist Checker");
        int progress = 0;

        try {
            // Fetch all the items in the database
            List<WaitlistMovie> movies = WaitlistMovie.listAll();

            // Process all movies to see if one is now available for download on YTS
            for (WaitlistMovie movie : movies) {
                try {
                    // If the movie is already in the system, remove if from the waitlist
                    if (entityUtilities.movieExists(movie.id)) {
                        movie.delete();
                        continue;
                    }

                    // Search on YTS for the movie
                    var ytsResponse = ytsService.search(movie.id);

                    // Get the information about the movie on imdb
                    var movieInfo = omdbService.getById(movie.id, omdbApiKey);

                    // If the search failed or the movie was not found, continue to the next movie
                    if (!ytsResponse.status.equals("ok") || ytsResponse.results.resultCount == 0) {
                        discordApi.getTextChannelById(waitlistChannel)
                                .ifPresent(textChannel ->
                                        textChannel.getMessageById(movie.messageId).join()
                                                .edit(messageFormatter.waitlistNotification(movieInfo))
                                );
                    } else {
                        movieDownloadProcessor.processDownload(movieInfo).onFailure().invoke(failure -> discordApi.getTextChannelById(waitlistChannel)
                                .ifPresent(textChannel ->
                                        textChannel.getMessageById(movie.messageId).join()
                                                .edit(messageFormatter.waitlistNotification(movieInfo))
                                )).onCompletion().invoke(() -> {
                            discordApi.getUserById(movie.requestedBy).join()
                                    .sendMessage(messageFormatter.formatMovieAddedDirectMessage(movieInfo));
                            new MessageBuilder()
                                    .setEmbed(messageFormatter.formatNewMovieNotification(movieInfo))
                                    .send(discordApi.getTextChannelById(newMoviesChannel).orElseThrow()).exceptionally(ExceptionLogger.get()
                            ).exceptionally(ExceptionLogger.get()).join();
                            movie.delete();
                        });
                    }
                } catch (Exception e) {
                    reportError(e);
                }

                progress++;
                updateProcessString("Waitlist Checker - " + decimalFormatter.format(((double) progress / movies.size()) * 100) + "%");
            }
        } catch (Exception e) {
            reportError(e);
        }

        endProcess();
    }
}