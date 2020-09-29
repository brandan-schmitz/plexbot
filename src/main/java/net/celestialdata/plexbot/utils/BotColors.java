package net.celestialdata.plexbot.utils;

import java.awt.*;

/**
 * Provides a central location to manage the colors used within the bot.
 *
 * @author Celestialdeath99
 */
public interface BotColors {
    /**
     * Returns the color used for bot info messages
     */
    Color INFO = Color.blue;

    /**
     * Returns the color used for bot warning messages
     */
    Color WARNING = Color.YELLOW;

    /**
     * Returns the color used for bot error messages
     */
    Color ERROR = Color.RED;

    /**
     * Returns the color used for messages about a stopped/killed process or task
     */
    Color KILLED = Color.BLACK;

    /**
     * Returns the color used for bot success messages
     */
    Color SUCCESS = Color.GREEN;
}
