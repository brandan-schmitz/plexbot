package net.celestialdata.plexbot.managers.waitlist;

import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.client.model.OmdbMovieInfo;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.database.DbOperations;
import net.celestialdata.plexbot.database.builders.WaitlistItemBuilder;
import net.celestialdata.plexbot.utils.BotColors;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * A class for handling all things related to the waiting list.
 *
 * @author Celestialdeath99
 */
public class WaitlistUtilities {

    /**
     * Add a movie to the waitlist
     *
     * @param movieInfo the OmdbMovie to add
     * @param userId    the ID of the user who requested the movie
     * @see OmdbMovieInfo
     */
    public static void addWaitlistItem(OmdbMovieInfo movieInfo, long userId) {
        // Check if the movie is already in the waiting list, otherwise send a message about it and add it
        if (!DbOperations.waitlistItemOps.exists(movieInfo.getImdbID())) {
            Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.waitlistChannelId()).ifPresent(textChannel ->
                    textChannel.sendMessage(new EmbedBuilder()
                            .setTitle(movieInfo.getTitle())
                            .setDescription("**Year:** " + movieInfo.getYear() + "\n" +
                                    "**Director(s):** " + movieInfo.getDirector() + "\n" +
                                    "**Plot:** " + movieInfo.getPlot())
                            .setImage(movieInfo.getPoster())
                            .setColor(BotColors.INFO)
                            .setFooter("Last Checked: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                                    .format(ZonedDateTime.now()) + " CST")).exceptionally(ExceptionLogger.get()
                    ).thenAccept(message -> DbOperations.saveObject(
                            new WaitlistItemBuilder()
                                    .withId(movieInfo.getImdbID())
                                    .withTitle(movieInfo.getTitle())
                                    .withYear(movieInfo.getYear())
                                    .withRequestedBy(userId)
                                    .withMessageId(message.getId())
                                    .build()
                    ))
            );
        }
    }

    /**
     * Update the message about a movie with an updated timestamp for when it was last checked for.
     *
     * @param movieInfo the OmdbMovie to update the message for
     * @see OmdbMovieInfo
     */
    static void updateMessage(OmdbMovieInfo movieInfo) {
        // Get the channel the waitlist messages are in then fetch the message for the movie and update it with
        // the current date and time.
        Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.waitlistChannelId()).ifPresent(textChannel ->
                textChannel.getMessageById(DbOperations.waitlistItemOps.getItemById(movieInfo.getImdbID()).getMessageId()).join().edit(new EmbedBuilder()
                        .setTitle(movieInfo.getTitle())
                        .setDescription("**Year:** " + movieInfo.getYear() + "\n" +
                                "**Director(s):** " + movieInfo.getDirector() + "\n" +
                                "**Plot:** " + movieInfo.getPlot())
                        .setImage(movieInfo.getPoster())
                        .setColor(BotColors.INFO)
                        .setFooter("Last Checked: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                                .format(ZonedDateTime.now()) + " CST")).exceptionally(ExceptionLogger.get()));
    }
}