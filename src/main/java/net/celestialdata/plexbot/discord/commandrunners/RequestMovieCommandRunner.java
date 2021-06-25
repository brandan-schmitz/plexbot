package net.celestialdata.plexbot.discord.commandrunners;

import io.quarkus.arc.log.LoggerName;
import net.celestialdata.plexbot.clients.models.omdb.OmdbResult;
import net.celestialdata.plexbot.clients.models.omdb.enums.OmdbResponseEnum;
import net.celestialdata.plexbot.clients.models.omdb.enums.OmdbResultTypeEnum;
import net.celestialdata.plexbot.clients.models.omdb.enums.OmdbSearchTypeEnum;
import net.celestialdata.plexbot.clients.services.OmdbService;
import net.celestialdata.plexbot.dataobjects.BotEmojis;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.entities.EntityUtilities;
import net.celestialdata.plexbot.enumerators.MovieDownloadSteps;
import net.celestialdata.plexbot.processors.MovieDownloadProcessor;
import net.celestialdata.plexbot.utilities.BotProcess;
import net.celestialdata.plexbot.utilities.SelectionUtilities;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import org.jboss.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.awt.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
@Dependent
public class RequestMovieCommandRunner extends BotProcess {

    @LoggerName("net.celestialdata.plexbot.discord.commandrunners.RequestMovieCommandRunner")
    Logger logger;

    @Inject
    SelectionUtilities selectionUtilities;

    @ConfigProperty(name = "BotSettings.prefix")
    String commandPrefix;

    @ConfigProperty(name = "ApiKeys.omdbApiKey")
    String omdbApiKey;

    @ConfigProperty(name = "ChannelSettings.newMovieNotificationChannel")
    String newMovieNotificationChannel;

    @Inject
    @RestClient
    OmdbService omdbService;

    @Inject
    MessageFormatter messageFormatter;

    @Inject
    EntityUtilities entityUtilities;

    @Inject
    DiscordApi discordApi;

    @Inject
    Instance<MovieDownloadProcessor> movieDownloadProcessor;

    @Inject
    Instance<ManagedExecutor> managedExecutor;

    @SuppressWarnings("unused")
    public void runCommand(Message incomingMessage, String parameterString) {
        // Reply to the command message and save that message so it can be modified later
        var replyMessage = incomingMessage.reply(new EmbedBuilder()
                .setTitle("Processing Request")
                .setDescription("I am processing your command request. Please stand-by for this process to be completed.")
                .setColor(Color.BLUE)
        ).exceptionally(ExceptionLogger.get()).join();

        // Configure the process for this command
        configureProcess("Movie Request (" + incomingMessage.getAuthor().getDiscriminatedName() + "): " + parameterString, replyMessage);

        // Variables that store the parsed results of the command parameters
        String titleArgument;
        String yearArgument = "";
        String idArgument = "";

        // Variables that store items related to the search and selection process
        List<OmdbResult> searchResultList = new ArrayList<>();
        OmdbResult selectedMovie;

        // Split command arguments by the space character
        var args = parameterString.split("\\s+");

        // Verify that the command has arguments
        if (args.length == 0) {
            replyMessage.edit(messageFormatter.errorMessage(
                    "You must specify the movie you wish to request. For more information on this command " +
                            "please use " + commandPrefix + "help rm"
            )).exceptionally(ExceptionLogger.get());
            endProcess();
            return;
        }

        // Parse year and ID arguments from movie title if they were provided
        StringBuilder titleBuilder = new StringBuilder();
        int titlePartPos = 0;
        for (String arg : args) {
            if (arg.startsWith("--year=")) {
                yearArgument = arg.replace("--year=", "");
            } else if (arg.startsWith("--id=")) {
                idArgument = arg.replace("--id=", "");
            } else {
                if (titlePartPos == 0) {
                    titleBuilder.append(arg);
                } else titleBuilder.append(" ").append(arg);
                titlePartPos++;
            }
        }

        // Assemble the movie title from the title builder
        titleArgument = titleBuilder.toString();

        /*
        Search for the movie on OMDB. Search priority:
            1. IMDb ID
            2. Title with year
            3. Title
        */
        if (!idArgument.isEmpty()) {
            // Search by the ID given
            var result = omdbService.getById(idArgument, omdbApiKey);

            // Add the result to the list of results if it was successful otherwise display an error
            if (result.response == OmdbResponseEnum.TRUE) {
                searchResultList.add(result);
            } else if (result.type != OmdbResultTypeEnum.MOVIE) {
                replyMessage.edit(messageFormatter.errorMessage(
                        "The IMDb code you provided was for a TV series or episode. Please provide a code for a movie only."
                )).exceptionally(ExceptionLogger.get());
                endProcess();
                return;
            } else {
                replyMessage.edit(messageFormatter.errorMessage(
                        "An invalid IMDb code was provided and it returned no search results. Please verify your code and try again."
                )).exceptionally(ExceptionLogger.get());
                endProcess();
                return;
            }
        } else if (!yearArgument.isEmpty()) {
            // Verify that there is a movie title to accompany the year otherwise display an error
            if (titleArgument.isEmpty()) {
                replyMessage.edit(messageFormatter.errorMessage(
                        "You must provide a movie title in addition to a year."
                )).exceptionally(ExceptionLogger.get());
                endProcess();
                return;
            } else {
                // Search by title and filter by year. If no results were found display an error
                var result = omdbService.search(titleArgument, OmdbSearchTypeEnum.MOVIE, yearArgument, omdbApiKey);
                if (result.response == OmdbResponseEnum.TRUE) {
                    searchResultList = result.search;
                } else {
                    replyMessage.edit(messageFormatter.errorMessage(
                            "No results returned. Please adjust your search parameters and try again."
                    )).exceptionally(ExceptionLogger.get());
                    endProcess();
                    return;
                }
            }
        } else {
            // Search by title, if no results were found display an error
            var result = omdbService.search(titleArgument, OmdbSearchTypeEnum.MOVIE, omdbApiKey);
            if (result.response == OmdbResponseEnum.TRUE) {
                searchResultList = result.search;
            } else {
                replyMessage.edit(messageFormatter.errorMessage(
                        "No results returned. Please adjust your search parameters and try again."
                )).exceptionally(ExceptionLogger.get());
                endProcess();
                return;
            }
        }

        // Filter out items that are not movies. If there are no items left after filtering then send an error
        searchResultList.removeIf(r -> r.type != OmdbResultTypeEnum.MOVIE);
        if (searchResultList.size() == 0) {
            replyMessage.edit(messageFormatter.errorMessage(
                    "No results returned. Please adjust your search parameters and try again."
            )).exceptionally(ExceptionLogger.get());
            endProcess();
            return;
        }

        // Fetch more detailed information about each movie returned in the results from above
        for (int i = 0; i < searchResultList.size(); i++) {
            searchResultList.set(i, omdbService.getById(searchResultList.get(i).imdbID, omdbApiKey));
        }

        // Send the list of results to the movie selection handler method and await its result
        AtomicBoolean selectionFailed = new AtomicBoolean(false);
        try {
            selectedMovie = selectionUtilities.handleMovieSelection(replyMessage, searchResultList).onFailure().invoke(returnedError -> {
                selectionFailed.set(true);
                if (returnedError instanceof InterruptedException) {
                    if (returnedError.getMessage().endsWith("movie.")) {
                        replyMessage.edit(new EmbedBuilder()
                                .setTitle("Command Timed Out")
                                .setDescription("The bot is no longer processing your request as you let it sit too long before selecting a movie. " +
                                        "Please run the command again if you wish to restart your request.")
                                .setFooter("Exited: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(ZonedDateTime.now()) + " CST")
                                .setColor(Color.BLACK));
                        endProcess();
                    } else if (returnedError.getMessage().equals("User has canceled the selection process.")) {
                        replyMessage.edit(new EmbedBuilder()
                                .setTitle("Request Canceled")
                                .setDescription("The bot is no longer processing your request since you requested it to be canceled by pressing the " +
                                        BotEmojis.X + " emoji.")
                                .setColor(Color.BLACK));
                        endProcess();
                    } else {
                        endProcess(returnedError);
                    }
                } else {
                    endProcess(returnedError);
                }
            }).await().indefinitely();
        } catch (Exception e) {
            endProcess(e);
            return;
        }

        // Ensure we exit if the selection process failed, was interrupted, or some other reason.
        if (selectionFailed.get()) {
            return;
        }

        // Verify that the movie requested does not already exist in the system
        if (entityUtilities.movieExists(selectedMovie.imdbID)) {
            replyMessage.edit(messageFormatter.errorMessage(
                    "This movie already exists in the system."
            )).exceptionally(ExceptionLogger.get());
            endProcess();
            return;
        }

        // Process the download of this movie using the movie download processor
        OmdbResult finalSelectedMovie = selectedMovie;
        movieDownloadProcessor.get().processDownload(selectedMovie, replyMessage, incomingMessage.getAuthor().getId()).runSubscriptionOn(managedExecutor.get()).subscribe().with(
                progress -> {
                    for (Map.Entry<MovieDownloadSteps, EmbedBuilder> entry : progress.entrySet()) {
                        replyMessage.edit(entry.getValue()).exceptionally(ExceptionLogger.get());
                    }
                },
                failure -> {
                    if (failure instanceof InterruptedException) {
                        if (failure.getMessage().equals("No match found on yts")) {
                            replyMessage.edit(messageFormatter.warningMessage("Unable to locate a copy of this movie to download. " +
                                    "It has been added to the waiting list and will be downloaded automatically when it becomes available."));
                        } else if (failure.getMessage().equals("Already in waitlist")) {
                            replyMessage.edit(messageFormatter.warningMessage("Unable to locate a copy of this movie to download. The movie has " +
                                    "already been added to the waiting list by someone else and will automatically downloaded when it becomes available."));
                        } else {
                            replyMessage.edit(messageFormatter.errorMessage("An unknown error has occurred while processing the download of this file. " +
                                    "Please try again later.", failure.getMessage()));
                        }
                    } else {
                        replyMessage.edit(messageFormatter.errorMessage("An unknown error has occurred while processing the download of this file. " +
                                "Please try again later.", failure.getMessage()));
                    }
                },
                () -> {
                    replyMessage.edit(messageFormatter.downloadFinishedMessage(finalSelectedMovie)).exceptionally(ExceptionLogger.get());
                    discordApi.getUserById(incomingMessage.getAuthor().getId()).join().sendMessage(messageFormatter.newMovieUserNotification(finalSelectedMovie));
                    new MessageBuilder()
                            .setEmbed(messageFormatter.newMovieNotification(finalSelectedMovie))
                            .send(discordApi.getTextChannelById(newMovieNotificationChannel).orElseThrow()).exceptionally(ExceptionLogger.get()
                    ).exceptionally(ExceptionLogger.get()).join();
                }
        );
        endProcess();
    }
}