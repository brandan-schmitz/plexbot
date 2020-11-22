package net.celestialdata.plexbot.managers.waitlist;

import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.apis.omdb.objects.movie.OmdbMovie;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.database.DbOperations;
import net.celestialdata.plexbot.database.builders.WaitlistItemBuilder;
import net.celestialdata.plexbot.database.models.WaitlistItem;
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
     * @param movie  the OmdbMovie to add
     * @param userId the ID of the user who requested the movie
     * @see OmdbMovie
     */
    public static void addWaitlistItem(OmdbMovie movie, long userId) {
        // Check if the movie is already in the waiting list, otherwise send a message about it and add it
        if (!DbOperations.waitlistItemOps.exists(movie.imdbID)) {
            Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.waitlistChannelId()).ifPresent(textChannel ->
                    textChannel.sendMessage(new EmbedBuilder()
                            .setTitle(movie.Title)
                            .setDescription("**Year:** " + movie.Year + "\n" +
                                    "**Director(s):** " + movie.Director + "\n" +
                                    "**Plot:** " + movie.Plot)
                            .setImage(movie.Poster)
                            .setColor(BotColors.INFO)
                            .setFooter("Last Checked: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                                    .format(ZonedDateTime.now()) + " CST")).exceptionally(ExceptionLogger.get()
                    ).thenAccept(message -> DbOperations.saveObject(
                            new WaitlistItemBuilder()
                                    .withId(movie.imdbID)
                                    .withTitle(movie.Title)
                                    .withYear(movie.Year)
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
     * @param movie the OmdbMovie to update the message for
     * @see OmdbMovie
     */
    static void updateMessage(OmdbMovie movie) {
        // Get the channel the waitlist messages are in then fetch the message for the movie and update it with
        // the current date and time.
        Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.waitlistChannelId()).ifPresent(textChannel ->
                textChannel.getMessageById(DbOperations.waitlistItemOps.getItemById(movie.imdbID).getMessageId()).join().edit(new EmbedBuilder()
                        .setTitle(movie.Title)
                        .setDescription("**Year:** " + movie.Year + "\n" +
                                "**Director(s):** " + movie.Director + "\n" +
                                "**Plot:** " + movie.Plot)
                        .setImage(movie.Poster)
                        .setColor(BotColors.INFO)
                        // TODO: Allow the timezone label to be changed in the bot configuration file
                        .setFooter("Last Checked: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                                .format(ZonedDateTime.now()) + " CST")).exceptionally(ExceptionLogger.get()));
    }
}