package net.celestialdata.plexbot.utilities;

import io.smallrye.mutiny.Uni;
import net.celestialdata.plexbot.clients.models.omdb.OmdbResult;
import net.celestialdata.plexbot.dataobjects.BotEmojis;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.logging.ExceptionLogger;

import javax.enterprise.context.ApplicationScoped;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SelectionUtilities {

    public Uni<OmdbResult> handleMovieSelection(Message replyMessage, List<OmdbResult> movieList) {
        return Uni.createFrom().emitter(uniEmitter -> {
            var selectScreens = new ArrayList<EmbedBuilder>();

            // Create a inner class to handle changing the selection screens
            class SelectionScreenChanger {
                private int currentPage = 0;

                /**
                 * Return the integer of the current movie being shown for selection
                 * @return integer of the current page being displayed
                 */
                int getCurrentPage() {
                    return currentPage;
                }

                /**
                 * Change the view to the next movie in the result list
                 */
                void nextScreen() {
                    // Increment the current page counter if its not already at the end
                    if (currentPage < selectScreens.size() - 1) {
                        currentPage++;
                    }

                    // Update the reactions on the message based upon the page counter and if its at the end or beginning
                    if (currentPage == 1 && replyMessage.getReactionByEmoji(BotEmojis.ARROW_LEFT).isEmpty()) {
                        replyMessage.removeAllReactions();
                        replyMessage.addReaction(BotEmojis.ARROW_LEFT);
                        replyMessage.addReaction(BotEmojis.ARROW_RIGHT);
                        replyMessage.addReaction(BotEmojis.CHECK_MARK);
                        replyMessage.addReaction(BotEmojis.X);
                    } else if (currentPage == selectScreens.size() - 1) {
                        replyMessage.removeAllReactions();
                        replyMessage.addReaction(BotEmojis.ARROW_LEFT);
                        replyMessage.addReaction(BotEmojis.CHECK_MARK);
                        replyMessage.addReaction(BotEmojis.X);
                    }

                    // Update the message with the new screen
                    replyMessage.edit(selectScreens.get(currentPage)).exceptionally(ExceptionLogger.get());
                }

                /**
                 * Change the view to the previous movie in the result list
                 */
                void previousScreen() {
                    // Decrement the current page counter if its not already at the beginning
                    if (currentPage > 0) {
                        currentPage--;
                    }

                    // Update the reactions on the message if the page counter is at the end
                    if (currentPage == selectScreens.size() - 2 && replyMessage.getReactionByEmoji(BotEmojis.ARROW_RIGHT).isEmpty()) {
                        replyMessage.removeAllReactions();
                        replyMessage.addReaction(BotEmojis.ARROW_LEFT);
                        replyMessage.addReaction(BotEmojis.ARROW_RIGHT);
                        replyMessage.addReaction(BotEmojis.CHECK_MARK);
                        replyMessage.addReaction(BotEmojis.X);
                    }

                    // Update the reactions on the message if the page counter is at the beginning
                    if (currentPage == 0) {
                        replyMessage.removeAllReactions();
                        replyMessage.addReaction(BotEmojis.ARROW_RIGHT);
                        replyMessage.addReaction(BotEmojis.CHECK_MARK);
                        replyMessage.addReaction(BotEmojis.X);
                    }

                    // Update the message with the new screen
                    replyMessage.edit(selectScreens.get(currentPage)).exceptionally(ExceptionLogger.get());
                }
            }
            SelectionScreenChanger selectionScreenChanger = new SelectionScreenChanger();

            // Create the screens for displaying the movies and send the first one
            if (movieList.size() == 1) {
                replyMessage.edit(new EmbedBuilder()
                        .setTitle("Is this the correct movie?")
                        .setDescription("Please verify that this is the correct movie. If it is, please click the " + BotEmojis.CHECK_MARK +
                                " reaction to begin adding it to the server. If this is not the correct movie please press the " + BotEmojis.X +
                                " reaction to cancel this action.\n\u200b")
                        .addField(movieList.get(0).title,
                                "**Year:** " + movieList.get(0).year + "\n" +
                                        "**Director(s):** " + movieList.get(0).director + "\n" +
                                        "**Plot:** " + movieList.get(0).plot)
                        .setImage(movieList.get(0).getPoster())
                        .setColor(Color.BLUE)
                ).exceptionally(ExceptionLogger.get());

                // Add the reactions
                replyMessage.addReaction(BotEmojis.X);
                replyMessage.addReaction(BotEmojis.CHECK_MARK);
            } else {
                // Build a screen for each movie in the list and add it to the array of screens.
                int posCounter = 1;
                for (OmdbResult m : movieList) {
                    selectScreens.add(new EmbedBuilder()
                            .setTitle("Choose your movie")
                            .setDescription("It looks like your search returned " + movieList.size() + " results. Please use the arrow reactions to " +
                                    "navigate the results until your intended movie appears. Once your intended movie is shown, please press the " + BotEmojis.CHECK_MARK +
                                    " reaction to begin adding it to the server. If your movie is not shown, press the " + BotEmojis.X + " reaction to cancel this process.\n\u200b")
                            .addField(m.title + " (" + posCounter + "/" + movieList.size() + ")",
                                    "**Year:** " + m.year + "\n" +
                                            "**Director(s):** " + m.director + "\n" +
                                            "**Plot:** " + m.plot)
                            .setImage(m.getPoster())
                            .setColor(Color.BLUE)
                    );
                    posCounter++;
                }

                // Update the message with the screens and add the reactions
                replyMessage.edit(selectScreens.get(0)).exceptionally(ExceptionLogger.get());
                replyMessage.addReaction(BotEmojis.ARROW_RIGHT);
                replyMessage.addReaction(BotEmojis.CHECK_MARK);
                replyMessage.addReaction(BotEmojis.X);
            }

            // Add the reaction listeners to the message
            replyMessage.addReactionAddListener(event -> {
                if (!event.getUser().map(User::isBot).orElseThrow() && event.getEmoji().equalsEmoji(BotEmojis.X)) {
                    replyMessage.removeAllReactions();
                    uniEmitter.fail(new InterruptedException("User has canceled the selection process."));
                } else if (!event.getUser().map(User::isBot).orElseThrow() && event.getEmoji().equalsEmoji(BotEmojis.CHECK_MARK)) {
                    replyMessage.removeAllReactions();
                    uniEmitter.complete(movieList.get(selectionScreenChanger.getCurrentPage()));
                } else if (!event.getUser().map(User::isBot).orElseThrow() && event.getEmoji().equalsEmoji(BotEmojis.ARROW_LEFT)) {
                    selectionScreenChanger.previousScreen();
                } else if (!event.getUser().map(User::isBot).orElseThrow() && event.getEmoji().equalsEmoji(BotEmojis.ARROW_RIGHT)) {
                    selectionScreenChanger.nextScreen();
                }
            }).removeAfter(5, TimeUnit.MINUTES).addRemoveHandler(() -> {
                replyMessage.removeAllReactions();
                uniEmitter.fail(new InterruptedException("Timeout occurred while waiting for user to select a movie."));
            });

            replyMessage.addReactionRemoveListener(event -> {
                if (!event.getUser().map(User::isBot).orElseThrow() && event.getEmoji().equalsEmoji(BotEmojis.ARROW_LEFT)) {
                    selectionScreenChanger.previousScreen();
                } else if (!event.getUser().map(User::isBot).orElseThrow() && event.getEmoji().equalsEmoji(BotEmojis.ARROW_RIGHT)) {
                    selectionScreenChanger.nextScreen();
                }
            }).removeAfter(5, TimeUnit.MINUTES)
                    .addRemoveHandler(replyMessage::removeAllReactions);
        });
    }
}