package net.celestialdata.plexbot.workhandlers;

import net.celestialdata.plexbot.apis.omdb.Omdb;
import net.celestialdata.plexbot.apis.omdb.objects.movie.OmdbMovie;
import net.celestialdata.plexbot.apis.omdb.objects.search.SearchResult;
import net.celestialdata.plexbot.apis.omdb.objects.search.SearchResultResponse;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.utils.BotColors;
import net.celestialdata.plexbot.utils.BotEmojis;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.logging.ExceptionLogger;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * This class handles displaying and allowing a user to
 * use reactions to select the correct movie that they
 * are looking for if more than one are returned in the
 * search results.
 *
 * @author Celestialdeath99
 */
class MovieSelectionHandler {
    private final ArrayList<EmbedBuilder> selectScreens = new ArrayList<>();
    private final ArrayList<OmdbMovie> movieList = new ArrayList<>();
    private int currentPage = 0;
    private final Message sentMessage;
    private OmdbMovie selectedMovie;
    private boolean beenSet = false;
    private boolean wasCanceled = false;
    public final Object lock = new Object();

    /**
     * This is the main constructor for the selection handler. It is responsible for
     * building a list of the movies the search returned, creating the screens with each
     * movie and registering the ReactionHandlers for the handler.
     *
     * @param response The SearchResultResponse from the OMDB API containing a list of
     *                 movies that the API found.
     * @param sentMessage The javacord message entity that the bot replied with to the
     *                    original request message.
     */
    MovieSelectionHandler(SearchResultResponse response, Message sentMessage) {
        this.sentMessage = sentMessage;

        // Build the list of movies returned in the search
        for (SearchResult r : response.Search) {
            movieList.add(Omdb.getMovieInfo(r.imdbID));
        }

        // Create the screens for displaying the movies and send the first one
        if (movieList.size() == 1) {
            sentMessage.edit(new EmbedBuilder()
                    .setTitle("Is this the correct movie?")
                    .setDescription("Please verify that this is the correct movie. If it is, please click the " + BotEmojis.CHECK_MARK +
                            " reaction to begin adding it to the server. If this is not the correct movie please press the " + BotEmojis.X +
                            " reaction to cancel this action.\n\u200b")
                    .addField(movieList.get(0).Title,
                            "**Year:** " + movieList.get(0).Year + "\n" +
                                    "**Director(s):** " + movieList.get(0).Director + "\n" +
                                    "**Plot:** " + movieList.get(0).Plot)
                    .setImage(movieList.get(0).Poster)
                    .setColor(BotColors.INFO)
            ).exceptionally(ExceptionLogger.get());

            // Add the reactions
            sentMessage.addReaction(BotEmojis.X);
            sentMessage.addReaction(BotEmojis.CHECK_MARK);
        } else {
            // Build a screen for each movie in the list and add it to the array of screens.
            int posCounter = 1;
            for (OmdbMovie m : movieList) {
                String imgUrl = ConfigProvider.BOT_SETTINGS.noPosterImageUrl();
                if (!m.Poster.equalsIgnoreCase("N/A"))  {
                    imgUrl = m.Poster;
                }

                selectScreens.add(new EmbedBuilder()
                        .setTitle("Choose your movie")
                        .setDescription("It looks like your search returned " + movieList.size() + " results. Please use the arrow reactions to " +
                                "navigate the results until your intended movie appears. Once your intended movie is shown, please press the " + BotEmojis.CHECK_MARK +
                                " reaction to begin adding it to the server. If your movie is not shown, press the " + BotEmojis.X + " reaction to cancel this process.\n\u200b")
                        .addField(m.Title + " (" + posCounter + "/" + movieList.size() + ")",
                                "**Year:** " + m.Year + "\n" +
                                        "**Director(s):** " + m.Director + "\n" +
                                        "**Plot:** " + m.Plot)
                        .setImage(imgUrl)
                        .setColor(BotColors.INFO)
                );
                posCounter++;
            }

            // Update the message with the screens and add the reactions
            sentMessage.edit(selectScreens.get(0)).exceptionally(ExceptionLogger.get());
            sentMessage.addReaction(BotEmojis.ARROW_RIGHT);
            sentMessage.addReaction(BotEmojis.CHECK_MARK);
            sentMessage.addReaction(BotEmojis.X);
        }

        // Add the reaction listeners to the message
        sentMessage.addReactionAddListener(event -> {
            if (!event.getUser().map(User::isBot).orElseThrow() && event.getEmoji().equalsEmoji(BotEmojis.X)) {
                sentMessage.removeAllReactions();
                synchronized (lock) {
                    beenSet = true;
                    wasCanceled = true;
                    lock.notifyAll();
                }
            } else if (!event.getUser().map(User::isBot).orElseThrow() && event.getEmoji().equalsEmoji(BotEmojis.CHECK_MARK)) {
                sentMessage.removeAllReactions();
                selectCurrentMovie();
            } else if (!event.getUser().map(User::isBot).orElseThrow() && event.getEmoji().equalsEmoji(BotEmojis.ARROW_LEFT)) {
                previousMovie();
            } else if (!event.getUser().map(User::isBot).orElseThrow() && event.getEmoji().equalsEmoji(BotEmojis.ARROW_RIGHT)) {
                nextMovie();
            }
        }).removeAfter(5, TimeUnit.MINUTES).addRemoveHandler(() -> {
            sentMessage.removeAllReactions();
            synchronized (lock) {
                beenSet = true;
                wasCanceled = true;
                lock.notifyAll();
            }
        });

        sentMessage.addReactionRemoveListener(event -> {
            if (!event.getUser().map(User::isBot).orElseThrow() && event.getEmoji().equalsEmoji(BotEmojis.ARROW_LEFT)) {
                previousMovie();
            } else if (!event.getUser().map(User::isBot).orElseThrow() && event.getEmoji().equalsEmoji(BotEmojis.ARROW_RIGHT)) {
                nextMovie();
            }
        }).removeAfter(5, TimeUnit.MINUTES)
                .addRemoveHandler(sentMessage::removeAllReactions);
    }

    /**
     * This handles what occurs if the reaction listener detects the left arrow.
     */
    private void nextMovie() {
        if (currentPage < selectScreens.size() - 1) {
            currentPage++;
        }

        //noinspection SuspiciousMethodCalls
        if (currentPage == 1 && !sentMessage.getReactions().contains(BotEmojis.ARROW_LEFT)) {
            sentMessage.removeAllReactions();
            sentMessage.addReaction(BotEmojis.ARROW_LEFT);
            sentMessage.addReaction(BotEmojis.ARROW_RIGHT);
            sentMessage.addReaction(BotEmojis.CHECK_MARK);
            sentMessage.addReaction(BotEmojis.X);
        } else if (currentPage == selectScreens.size() - 1) {
            sentMessage.removeAllReactions();
            sentMessage.addReaction(BotEmojis.ARROW_LEFT);
            sentMessage.addReaction(BotEmojis.CHECK_MARK);
            sentMessage.addReaction(BotEmojis.X);
        }

        sentMessage.edit(selectScreens.get(currentPage)).exceptionally(ExceptionLogger.get());
    }

    /**
     * This handles what occurs if the reaction listener detects the right arrow.
     */
    private void previousMovie() {
        if (currentPage > 0) {
            currentPage--;
        }

        //noinspection SuspiciousMethodCalls
        if (currentPage == selectScreens.size() - 2 && !sentMessage.getReactions().contains(BotEmojis.ARROW_RIGHT)) {
            sentMessage.removeAllReactions();
            sentMessage.addReaction(BotEmojis.ARROW_LEFT);
            sentMessage.addReaction(BotEmojis.ARROW_RIGHT);
            sentMessage.addReaction(BotEmojis.CHECK_MARK);
            sentMessage.addReaction(BotEmojis.X);
        }

        if (currentPage == 0) {
            sentMessage.removeAllReactions();
            sentMessage.addReaction(BotEmojis.ARROW_RIGHT);
            sentMessage.addReaction(BotEmojis.CHECK_MARK);
            sentMessage.addReaction(BotEmojis.X);
        }

        sentMessage.edit(selectScreens.get(currentPage)).exceptionally(ExceptionLogger.get());
    }

    /**
     * This handles what occurs if the reaction listener detects the green checkmark.
     */
    private void selectCurrentMovie() {
        selectedMovie = movieList.get(currentPage);

        // Set the movies poster to the image in the config if it is not available.
        if (selectedMovie.Poster.equalsIgnoreCase("n/a")) {
            selectedMovie.Poster = ConfigProvider.BOT_SETTINGS.noPosterImageUrl();
        }

        // Notify the command worker thread that the movie has been selected.
        synchronized (lock) {
            beenSet = true;
            lock.notifyAll();
        }
    }

    /**
     * Returns if a movie has been selected to not.
     *
     * @return The boolean value of the movie selection status.
     */
    boolean getBeenSet() {
        return beenSet;
    }

    /**
     * Returns if the selection process was canceled or not.
     *
     * @return The boolean value of the canceled status.
     */
    boolean getWasCanceled() {
        return wasCanceled;
    }

    /**
     * Returns the movie that was selected
     *
     * @return The OmdbMovie entity for the selected movie.
     * @throws NullPointerException If a movie was not selected for some reason it throws a NPE.
     */
    OmdbMovie getSelectedMovie() {
        return selectedMovie;
    }
}