package net.celestialdata.plexbot.managers.waitlist;

import net.celestialdata.plexbot.BotWorkPool;
import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.apis.omdb.objects.movie.OmdbMovie;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.database.DatabaseDataManager;
import net.celestialdata.plexbot.utils.BotColors;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.concurrent.TimeUnit;

import static net.celestialdata.plexbot.Main.scheduledExecutorService;

/**
 * A class for handling all things related to the waiting list.
 *
 * @author Celestialdeath99
 */
public class WaitlistManager {

    /**
     * Override the default constructor. This is used to schedule the checks used to
     * check if a movie in the waitlist is now available.
     */
    public WaitlistManager() {
        scheduledExecutorService.scheduleAtFixedRate(() -> BotWorkPool.getInstance().submitProcess(new WaitlistChecker()), 0, 1, TimeUnit.HOURS);
    }

    /**
     * Add a movie to the waitlist
     *
     * @param movie  the OmdbMovie to add
     * @param userId the ID of the user who requested the movie
     * @see OmdbMovie
     */
    public static void addWaitlistItem(OmdbMovie movie, long userId) {
        // Check if the movie is already in the waiting list, otherwise send a message about it and add it
        if (!DatabaseDataManager.isMovieInWaitlist(movie.imdbID)) {
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
                    ).thenAccept(message ->
                            DatabaseDataManager.addMovieToWaitlist(movie.imdbID, movie.Title, movie.Year, userId, message.getId())));
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
                textChannel.getMessageById(DatabaseDataManager.getWaitlistMessageId(movie.imdbID)).join().edit(new EmbedBuilder()
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

    /**
     * Delete a movie from the waiting list.
     *
     * @param movieId the IMDB ID of the movie
     */
    static void deleteWaitlistItem(String movieId) {
        // Remove the message about the movie from the waiting-list channel
        Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.waitlistChannelId()).ifPresent(textChannel ->
                textChannel.getMessageById(DatabaseDataManager.getWaitlistMessageId(movieId)).join().delete().exceptionally(ExceptionLogger.get()));

        // Remove the movie from the waiting-list table in the database
        DatabaseDataManager.removeMovieFromWaitlist(movieId);
    }
}