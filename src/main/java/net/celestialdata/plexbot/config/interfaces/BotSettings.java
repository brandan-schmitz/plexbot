package net.celestialdata.plexbot.config.interfaces;

/**
 * Returns the values from the BotSettings config section
 *
 * @author Celestialdeath99
 */
public interface BotSettings {
    /**
     * The Discord token for the bot
     *
     * @return The token
     */
    String token();

    /**
     * The name of the bot to  be displayed in commands and such
     *
     * @return The name of the bot
     */
    String botName();

    /**
     * The folder to save the movie files into
     *
     * @return The folder path
     */
    String movieDownloadFolder();

    /**
     * The URL of the image to display if there is not an image available
     *
     * @return The image URL
     */
    String noPosterImageUrl();

    /**
     * The URL of the current YTS domain name
     *
     * @return The YTS URL
     */
    String currentYtsDomain();

    /**
     * The ID of the channel for listing movies that can be upgraded to
     * a better quality
     *
     * @return The channel id
     */
    long upgradableMoviesChannelId();

    /**
     * The ID of the channel for notifications about movies that have been upgraded to a better resolution
     *
     * @return The channel id
     */
    long upgradedMoviesChannelId();

    /**
     * The ID of the channel where the bot's status message will be located
     *
     * @return The channel id
     */
    long botStatusChannelId();

    /**
     * The ID of the channel for notifications about movies that have been added to the library
     *
     * @return The channel id
     */
    long newMoviesChannelId();

    /**
     * The ID of the channel for notifications about movies that have been added to the waiting list
     *
     * @return The channel id
     */
    long waitlistChannelId();
}
