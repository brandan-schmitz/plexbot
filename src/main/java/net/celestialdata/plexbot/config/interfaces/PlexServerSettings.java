package net.celestialdata.plexbot.config.interfaces;

/**
 * Returns the values from the PlexServerSettings config section
 *
 * @author Celestialdeath99
 */
@SuppressWarnings("unused")
public interface PlexServerSettings {
    /**
     * The IP address to access the Plex Media Server on
     *
     * @return The IP address
     */
    String ipAddress();

    /**
     * The port to access the Plex Media Server on
     *
     * @return The port
     */
    Integer port();

    /**
     * The username to the plex.tv account
     *
     * @return The username
     */
    String username();

    /**
     * The password to the plex.tv account
     *
     * @return The password
     */
    String password();

    /**
     * The client identifier for the bot
     *
     * @return The identifier
     */
    String clientIdentifier();
}
