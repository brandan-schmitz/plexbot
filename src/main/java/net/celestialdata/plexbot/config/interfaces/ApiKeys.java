package net.celestialdata.plexbot.config.interfaces;

/**
 * Returns the values from the ApiKeys config section
 *
 * @author Celestialdeath99
 */
public interface ApiKeys {

    /**
     * The API Key for the OMDb API
     *
     * @return The API Key
     */
    String omdbApiKey();

    /**
     * The API Key for real-debrid
     *
     * @return The API key
     */
    String realDebridKey();
}
