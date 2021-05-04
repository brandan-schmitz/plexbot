package net.celestialdata.plexbot.managers.waitlist;

import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.client.model.OmdbItem;
import net.celestialdata.plexbot.configuration.BotConfig;
import net.celestialdata.plexbot.database.DbOperations;
import net.celestialdata.plexbot.utils.BotColors;
import org.hibernate.ObjectNotFoundException;
import org.javacord.api.entity.message.embed.EmbedBuilder;

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
     * @see OmdbItem
     */
    static void updateMessage(OmdbItem movieInfo) throws ObjectNotFoundException, CompletionException {
        // Get the channel the waitlist messages are in then fetch the message for the movie and update it with
        // the current date and time.
        Main.getBotApi().getTextChannelById(BotConfig.getInstance().waitlistChannelId()).ifPresent(textChannel ->
                textChannel.getMessageById(DbOperations.waitlistItemOps.getItemById(movieInfo.getImdbID()).getMessageId()).join().edit(new EmbedBuilder()
                        .setTitle(movieInfo.getTitle())
                        .setDescription("**Year:** " + movieInfo.getYear() + "\n" +
                                "**Director(s):** " + movieInfo.getDirector() + "\n" +
                                "**Plot:** " + movieInfo.getPlot())
                        .setImage(movieInfo.getPoster())
                        .setColor(BotColors.INFO)
                        .setFooter("Last Checked: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                                .format(ZonedDateTime.now()) + " CST")));
    }
}