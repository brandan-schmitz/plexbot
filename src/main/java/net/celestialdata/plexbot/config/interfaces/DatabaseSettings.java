package net.celestialdata.plexbot.config.interfaces;

/**
 * Returns the values from the DatabaseSettings Config Section
 *
 * @author Celestialdeath99
 */
public interface DatabaseSettings {

    /**
     * The IP address of the database to connect to
     *
     * @return The database
     */
    String ipAddress();

    /**
     * The port of the database to connect to
     *
     * @return The database port
     */
    Integer port();

    /**
     * The username for the database to connect to
     *
     * @return The database username
     */
    String dbName();

    /**
     * The name of the database to connect to
     *
     * @return The database name
     */
    String username();

    /**
     * The password for the database to connect to
     *
     * @return The database password
     */
    String password();
}
