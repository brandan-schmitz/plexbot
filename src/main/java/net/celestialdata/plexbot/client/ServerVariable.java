package net.celestialdata.plexbot.client;

import java.util.HashSet;

/**
 * Representing a Server Variable for server URL template substitution.
 */
public class ServerVariable {
    @SuppressWarnings("unused")
    public final String description;
    public final String defaultValue;
    public final HashSet<String> enumValues;

    /**
     * @param description  A description for the server variable.
     * @param defaultValue The default value to use for substitution.
     * @param enumValues   An enumeration of string values to be used if the
     *                     substitution options are from a limited set.
     */
    @SuppressWarnings("unused")
    public ServerVariable(String description, String defaultValue,
                          HashSet<String> enumValues) {
        this.description = description;
        this.defaultValue = defaultValue;
        this.enumValues = enumValues;
    }
}
