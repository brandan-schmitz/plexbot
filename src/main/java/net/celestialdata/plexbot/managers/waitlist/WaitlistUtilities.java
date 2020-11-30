package net.celestialdata.plexbot.managers.waitlist;

import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.client.model.OmdbMovieInfo;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.database.DbOperations;
import net.celestialdata.plexbot.utils.BotColors;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.concurrent.CompletionException;

/**
 * A class for handling all things related to the waiting list.
 *
 * @author Celestialdeath99
 */
public class WaitlistUtilities {
    /**
     * Update the message about a movie with an updated timestamp for when it was last checked for.
     *
     * @param movieInfo the OmdbMovie to update the message for
     * @see OmdbMovieInfo
     */
    static void updateMessage(OmdbMovieInfo movieInfo) {
        // Get the channel the waitlist messages are in then fetch the message for the movie and update it with
        // the current date and time.
        try {
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
        } catch (CompletionException ignored) {}
    }
}