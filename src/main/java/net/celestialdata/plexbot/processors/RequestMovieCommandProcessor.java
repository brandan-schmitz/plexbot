package net.celestialdata.plexbot.processors;

import io.quarkus.arc.log.LoggerName;
import io.smallrye.mutiny.Uni;
import net.celestialdata.plexbot.clients.models.tmdb.TmdbMovie;
import net.celestialdata.plexbot.clients.models.tmdb.TmdbSourceIdType;
import net.celestialdata.plexbot.clients.services.TmdbService;
import net.celestialdata.plexbot.db.daos.MovieDao;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.enumerators.MovieDownloadSteps;
import net.celestialdata.plexbot.utilities.BotProcess;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
@Dependent
public class RequestMovieCommandProcessor extends BotProcess {
    private Message replyMessage;
    private Message incomingMessage;

    @LoggerName("net.celestialdata.plexbot.discord.commandrunners.RequestMovieCommandRunner")
    Logger logger;

    @ConfigProperty(name = "BotSettings.prefix")
    String commandPrefix;

    @ConfigProperty(name = "ChannelSettings.newMovieNotificationChannel")
    String newMovieNotificationChannel;

    @Inject
    @RestClient
    TmdbService tmdbService;

    @Inject
    MessageFormatter messageFormatter;

    @Inject
    DiscordApi discordApi;

    @Inject
    Instance<MovieDownloadProcessor> movieDownloadProcessor;

    @Inject
    Instance<ManagedExecutor> managedExecutor;

    @Inject
    MovieDao movieDao;

    private ActionRow determineSelectionButtons(int pos, int size) {
        if (size == 1) {
            return ActionRow.of(
                    org.javacord.api.entity.message.component.Button.success("select-" + incomingMessage.getId(), "Select"),
                    org.javacord.api.entity.message.component.Button.danger("cancel-" + incomingMessage.getId(), "Cancel")
            );
        } else if (pos == 0) {
            return ActionRow.of(
                    org.javacord.api.entity.message.component.Button.secondary("next-" + incomingMessage.getId(), "Next"),
                    org.javacord.api.entity.message.component.Button.success("select-" + incomingMessage.getId(), "Select"),
                    org.javacord.api.entity.message.component.Button.danger("cancel-" + incomingMessage.getId(), "Cancel")
            );
        } else if (pos == (size - 1)) {
            return ActionRow.of(
                    org.javacord.api.entity.message.component.Button.secondary("previous-" + incomingMessage.getId(), "Previous"),
                    org.javacord.api.entity.message.component.Button.success("select-" + incomingMessage.getId(), "Select"),
                    org.javacord.api.entity.message.component.Button.danger("cancel-" + incomingMessage.getId(), "Cancel")
            );
        } else {
            return ActionRow.of(
                    org.javacord.api.entity.message.component.Button.secondary("previous-" + incomingMessage.getId(), "Previous"),
                    org.javacord.api.entity.message.component.Button.secondary("next-" + incomingMessage.getId(), "Next"),
                    org.javacord.api.entity.message.component.Button.success("select-" + incomingMessage.getId(), "Select"),
                    Button.danger("cancel-" + incomingMessage.getId(), "Cancel")
            );
        }
    }

    public Uni<TmdbMovie> handleMovieSelection(List<TmdbMovie> movieList) {
        return Uni.createFrom().emitter(uniEmitter -> {
            var selectScreens = new ArrayList<EmbedBuilder>();

            // Build the screens for each movie
            for (TmdbMovie movie : movieList) {
                selectScreens.add(new EmbedBuilder()
                        .setTitle("Select Movie")
                        .setDescription("It looks like your search returned " + movieList.size() + " results. " +
                                "Please use the buttons below to navigate and select the correct movie.\n\u200b")
                        .addField("Title:", "```" + movie.title + "```")
                        .addInlineField("Release Date:", "```" + movie.releaseDate + "```")
                        .addInlineField("TMDB ID:", "```" + movie.tmdbId + "```")
                        .addInlineField("IMDB ID:", "```" + movie.getImdbId() + "```")
                        .addField("Overview:", "```" + movie.getOverview() + "```")
                        .setImage(movie.getPoster())
                        .setColor(Color.BLUE)
                );
            }

            // Replace the reply message with one that uses components
            AtomicInteger currentScreen = new AtomicInteger(0);
            replyMessage.delete();
            replyMessage = new MessageBuilder()
                    .setEmbed(selectScreens.get(currentScreen.get()))
                    .addComponents(determineSelectionButtons(currentScreen.get(), selectScreens.size()))
                    .replyTo(incomingMessage)
                    .send(incomingMessage.getChannel())
                    .join();

            // Add the button click listener that allows navigation through the screens
            replyMessage.getChannel().addButtonClickListener(clickEvent -> {
                if (clickEvent.getButtonInteraction().getCustomId().equals("next-" + incomingMessage.getId())) {
                    // Proceed to the next screen
                    currentScreen.getAndIncrement();

                    // Update the displayed screen and buttons
                    clickEvent.getInteraction().asMessageComponentInteraction().orElseThrow()
                            .createOriginalMessageUpdater()
                            .removeAllEmbeds()
                            .addEmbed(selectScreens.get(currentScreen.get()))
                            .removeAllComponents()
                            .addComponents(determineSelectionButtons(currentScreen.get(), selectScreens.size()))
                            .update();
                } else if (clickEvent.getButtonInteraction().getCustomId().equals("previous-" + incomingMessage.getId())) {
                    // Proceed to the previous screen
                    currentScreen.getAndDecrement();

                    // Update the displayed screen and buttons
                    clickEvent.getInteraction().asMessageComponentInteraction().orElseThrow()
                            .createOriginalMessageUpdater()
                            .removeAllEmbeds()
                            .addEmbed(selectScreens.get(currentScreen.get()))
                            .removeAllComponents()
                            .addComponents(determineSelectionButtons(currentScreen.get(), selectScreens.size()))
                            .update();
                } else if (clickEvent.getButtonInteraction().getCustomId().equals("select-" + incomingMessage.getId())) {
                    // Remove the buttons from the message
                    clickEvent.getInteraction().asMessageComponentInteraction().orElseThrow()
                            .createOriginalMessageUpdater()
                            .removeAllComponents()
                            .update();

                    // Submit the selected movie
                    uniEmitter.complete(movieList.get(currentScreen.get()));
                } else if (clickEvent.getButtonInteraction().getCustomId().equals("cancel-" + incomingMessage.getId())) {
                    // Remove the buttons from the message
                    clickEvent.getInteraction().asMessageComponentInteraction().orElseThrow()
                            .createOriginalMessageUpdater()
                            .removeAllComponents()
                            .update();

                    // Submit the cancellation
                    uniEmitter.fail(new InterruptedException("User has canceled the selection process."));
                }
            }).removeAfter(2, TimeUnit.MINUTES).addRemoveHandler(() -> uniEmitter.fail(new InterruptedException("Timeout occurred while waiting for user to select a movie.")));
        });
    }

    @SuppressWarnings("unused")
    public void runCommand(Message incomingMessage, String parameterString) {
        this.incomingMessage = incomingMessage;

        // Reply to the command message and save that message so that it can be modified later
        replyMessage = incomingMessage.reply(new EmbedBuilder()
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
        List<TmdbMovie> searchResultList = new ArrayList<>();
        TmdbMovie selectedMovie;

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
            // Verify that the ID is either a IMDB or TMDB ID
            if (idArgument.matches("^tt[0-9]{7,8}")) {
                // Fetch any results matching this ID
                var results = tmdbService.findByExternalId(idArgument, TmdbSourceIdType.IMDB.getValue());

                // Ensure that the search was successful
                if (results.isSuccessful()) {
                    // Fetch the list of movies that was returned
                    var movieResults = results.movies;

                    // Add the movie results to the search list
                    searchResultList.addAll(movieResults);
                } else {
                    // Display that the search was not successful and exit
                    replyMessage.edit("The ID you provided is not a recognized IMDB or TMDB ID for a movie. " +
                            "Please verify you are using a valid ID.");
                    endProcess();
                    return;
                }
            } else if (idArgument.matches("^[0-9]{1,12}")) {
                // Fetch the movie using the provided TMDB ID
                var result = tmdbService.getMovie(Long.parseLong(idArgument));

                // Verify that the result was successful
                if (result.isSuccessful()) {
                    // Add the result to the search list
                    searchResultList.add(result);
                } else {
                    // Display that the search was not successful and exit
                    replyMessage.edit("The ID you provided is not a recognized IMDB or TMDB ID for a movie. " +
                            "Please verify you are using a valid ID.");
                    endProcess();
                    return;
                }
            } else {
                // Display that the search was not successful and exit
                replyMessage.edit("The ID you provided is not a recognized IMDB or TMDB ID for a movie. " +
                        "Please verify you are using a valid ID.");
                endProcess();
                return;
            }
        } else if (!yearArgument.isEmpty()) {
            // Verify that there is a movie title to accompany the year otherwise display an error
            if (titleArgument.isEmpty()) {
                replyMessage.edit(messageFormatter.errorMessage("You must provide a movie title in addition to a year."));
                endProcess();
                return;
            } else {
                // Search by title and filter by year. If no results were found display an error
                var results = tmdbService.searchForMovie(titleArgument, yearArgument);

                // Verify the search was successful and add the results to the search list if it was
                if (results.isSuccessful()) {
                    searchResultList.addAll(results.results);
                } else {
                    // Display that the search was not successful and exit
                    replyMessage.edit(messageFormatter.errorMessage("No results returned. Please adjust your search parameters and try again."));
                    endProcess();
                    return;
                }
            }
        } else {
            // Fetch a list of movies matching the search title
            var results = tmdbService.searchForMovie(titleArgument);

            // Verify that the search was successful and add the results to the search list if it was
            if (results.isSuccessful()) {
                searchResultList.addAll(results.results);
            } else {
                // Display that the search was not successful and exit
                replyMessage.edit(messageFormatter.errorMessage("No results returned. Please adjust your search parameters and try again."));
                endProcess();
                return;
            }
        }

        // Stop if there were no results found in the search process
        if (searchResultList.isEmpty()) {
            replyMessage.edit(messageFormatter.errorMessage("No results returned. Please adjust your search parameters and try again."));
            endProcess();
            return;
        }

        // Fetch more detailed information about each movie in the results from above
        for (int i = 0; i < searchResultList.size(); i++) {
            // Fetch more detailed information about this movie that is not included in the base search results
            var result = tmdbService.getMovie(searchResultList.get(i).tmdbId);

            // Ensure that the data retrieval was successful and update the movie in the results list if it was
            if (result.isSuccessful()) {
                searchResultList.set(i, result);
            } else {
                // Display a message that there was an error and exit
                replyMessage.edit(messageFormatter.errorMessage("There was an error while processing the list of search results. " +
                        "Please try again later."));
                endProcess();
                return;
            }
        }

        // Send the list of results to the movie selection handler method and await its result
        AtomicBoolean selectionFailed = new AtomicBoolean(false);
        try {
            selectedMovie = handleMovieSelection(searchResultList).onFailure().invoke(returnedError -> {
                selectionFailed.set(true);
                if (returnedError instanceof InterruptedException) {
                    if (returnedError.getMessage().equals("Timeout occurred while waiting for user to select a movie.")) {
                        discordApi.getMessageById(replyMessage.getId(), incomingMessage.getChannel()).join().delete().join();
                        replyMessage = new MessageBuilder()
                                .setEmbed(new EmbedBuilder()
                                        .setTitle("Command Timed Out")
                                        .setDescription("The bot is no longer processing your request as you let it sit too long before selecting a movie. " +
                                                "Please run the command again if you wish to restart your request.")
                                        .setFooter("Timed out on " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(ZonedDateTime.now()) + " CST")
                                        .setColor(Color.BLACK))
                                .replyTo(incomingMessage)
                                .send(incomingMessage.getChannel())
                                .join();
                        endProcess();
                    } else if (returnedError.getMessage().equals("User has canceled the selection process.")) {
                        replyMessage.edit(new EmbedBuilder()
                                .setTitle("Request Canceled")
                                .setDescription("Your request has been canceled.")
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
        if (movieDao.existsByTmdbId(selectedMovie.tmdbId)) {
            replyMessage.edit(messageFormatter.errorMessage(
                    "This movie already exists in the system."
            )).exceptionally(ExceptionLogger.get());
            endProcess();
            return;
        }

        // Process the download of this movie using the movie download processor
        TmdbMovie finalSelectedMovie = selectedMovie;
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