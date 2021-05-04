package net.celestialdata.plexbot.workhandlers;

import net.celestialdata.plexbot.client.model.OmdbItem;
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
class MediaSelectionHandler {
    public final Object lock = new Object();
    private final ArrayList<EmbedBuilder> selectScreens = new ArrayList<>();
    private final List<OmdbItem> mediaList;
    private final Message sentMessage;
    private int currentPage = 0;
    private OmdbItem selectedMedia;
    private boolean beenSet = false;
    private boolean wasCanceled = false;

    /**
     * This is the main constructor for the selection handler. It is responsible for
     * building a list of the movies the search returned, creating the screens with each
     * movie and registering the ReactionHandlers for the handler.
     *
     * @param mediaList   The list of movies the user can choose from.
     * @param sentMessage The javacord message entity that the bot replied with to the
     */
    MediaSelectionHandler(List<OmdbItem> mediaList, Message sentMessage) {
        this.sentMessage = sentMessage;
        this.mediaList = mediaList;

        // Create the screens for displaying the movies and send the first one
        if (mediaList.size() == 1) {
            sentMessage.edit(new EmbedBuilder()
                    .setTitle("Is this the correct media?")
                    .setDescription("Please verify that this is the correct media. If it is, please click the " + BotEmojis.CHECK_MARK +
                            " reaction to begin adding it to the server. If this is not the correct media please press the " + BotEmojis.X +
                            " reaction to cancel this action.\n\u200b")
                    .addField(mediaList.get(0).getTitle(),
                            "**Year:** " + mediaList.get(0).getYear() + "\n" +
                                    "**Director(s):** " + mediaList.get(0).getDirector() + "\n" +
                                    "**Plot:** " + mediaList.get(0).getPlot())
                    .setImage(mediaList.get(0).getPoster())
                    .setColor(BotColors.INFO)
            ).exceptionally(ExceptionLogger.get());

            // Add the reactions
            sentMessage.addReaction(BotEmojis.X);
            sentMessage.addReaction(BotEmojis.CHECK_MARK);
        } else {
            // Build a screen for each movie in the list and add it to the array of screens.
            int posCounter = 1;
            for (OmdbItem m : mediaList) {
                selectScreens.add(new EmbedBuilder()
                        .setTitle("Choose your media")
                        .setDescription("It looks like your search returned " + mediaList.size() + " results. Please use the arrow reactions to " +
                                "navigate the results until your intended media appears. Once your intended media is shown, please press the " + BotEmojis.CHECK_MARK +
                                " reaction to begin adding it to the server. If your media is not shown, press the " + BotEmojis.X + " reaction to cancel this process.\n\u200b")
                        .addField(m.getTitle() + " (" + posCounter + "/" + mediaList.size() + ")",
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
                selectCurrentPage();
            } else if (!event.getUser().map(User::isBot).orElseThrow() && event.getEmoji().equalsEmoji(BotEmojis.ARROW_LEFT)) {
                previousPage();
            } else if (!event.getUser().map(User::isBot).orElseThrow() && event.getEmoji().equalsEmoji(BotEmojis.ARROW_RIGHT)) {
                nextPage();
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
                previousPage();
            } else if (!event.getUser().map(User::isBot).orElseThrow() && event.getEmoji().equalsEmoji(BotEmojis.ARROW_RIGHT)) {
                nextPage();
            }
        }).removeAfter(5, TimeUnit.MINUTES)
                .addRemoveHandler(sentMessage::removeAllReactions);
    }

    /**
     * This handles what occurs if the reaction listener detects the left arrow.
     */
    private void nextPage() {
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
    private void previousPage() {
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
    private void selectCurrentPage() {
        selectedMedia = mediaList.get(currentPage);

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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
     * @return The OmdbItem entity for the selected movie.
     * @throws NullPointerException If a movie was not selected for some reason it throws a NPE.
     */
    OmdbItem getSelectedMedia() {
        return selectedMedia;
    }
}