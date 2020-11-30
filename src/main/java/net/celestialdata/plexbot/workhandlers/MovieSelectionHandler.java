package net.celestialdata.plexbot.workhandlers;

import net.celestialdata.plexbot.client.model.OmdbMovieInfo;
import net.celestialdata.plexbot.utils.BotColors;
import net.celestialdata.plexbot.utils.BotEmojis;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.logging.ExceptionLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class handles displaying and allowing a user to
 * use reactions to select the correct movie that they
 * are looking for if more than one are returned in the
 * search results.
 */
class MovieSelectionHandler {
    public final Object lock = new Object();
    private final ArrayList<EmbedBuilder> selectScreens = new ArrayList<>();
    private final List<OmdbMovieInfo> movieList;
    private final Message sentMessage;
    private int currentPage = 0;
    private OmdbMovieInfo selectedMovie;
    private boolean beenSet = false;
    private boolean wasCanceled = false;

    /**
     * This is the main constructor for the selection handler. It is responsible for
     * building a list of the movies the search returned, creating the screens with each
     * movie and registering the ReactionHandlers for the handler.
     *
     * @param movieList   The list of movies the user can choose from.
     * @param sentMessage The javacord message entity that the bot replied with to the
     *                    original request message.
     */
    MovieSelectionHandler(List<OmdbMovieInfo> movieList, Message sentMessage) {
        this.sentMessage = sentMessage;
        this.movieList = movieList;

        // Create the screens for displaying the movies and send the first one
        if (movieList.size() == 1) {
            sentMessage.edit(new EmbedBuilder()
                    .setTitle("Is this the correct movie?")
                    .setDescription("Please verify that this is the correct movie. If it is, please click the " + BotEmojis.CHECK_MARK +
                            " reaction to begin adding it to the server. If this is not the correct movie please press the " + BotEmojis.X +
                            " reaction to cancel this action.\n\u200b")
                    .addField(movieList.get(0).getTitle(),
                            "**Year:** " + movieList.get(0).getYear() + "\n" +
                                    "**Director(s):** " + movieList.get(0).getDirector() + "\n" +
                                    "**Plot:** " + movieList.get(0).getPlot())
                    .setImage(movieList.get(0).getPoster())
                    .setColor(BotColors.INFO)
            ).exceptionally(ExceptionLogger.get());

            // Add the reactions
            sentMessage.addReaction(BotEmojis.X);
            sentMessage.addReaction(BotEmojis.CHECK_MARK);
        } else {
            // Build a screen for each movie in the list and add it to the array of screens.
            int posCounter = 1;
            for (OmdbMovieInfo m : movieList) {
                selectScreens.add(new EmbedBuilder()
                        .setTitle("Choose your movie")
                        .setDescription("It looks like your search returned " + movieList.size() + " results. Please use the arrow reactions to " +
                                "navigate the results until your intended movie appears. Once your intended movie is shown, please press the " + BotEmojis.CHECK_MARK +
                                " reaction to begin adding it to the server. If your movie is not shown, press the " + BotEmojis.X + " reaction to cancel this process.\n\u200b")
                        .addField(m.getTitle() + " (" + posCounter + "/" + movieList.size() + ")",
                                "**Year:** " + m.getYear() + "\n" +
                                        "**Director(s):** " + m.getDirector() + "\n" +
                                        "**Plot:** " + m.getPlot())
                        .setImage(m.getPoster())
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
    OmdbMovieInfo getSelectedMovie() {
        return selectedMovie;
    }
}