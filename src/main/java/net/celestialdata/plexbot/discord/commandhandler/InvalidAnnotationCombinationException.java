package net.celestialdata.plexbot.discord.commandhandler;

import net.celestialdata.plexbot.discord.commandhandler.api.annotation.RestrictedTo;
import net.celestialdata.plexbot.discord.commandhandler.api.annotation.RestrictionPolicy;

/**
 * An exception that is thrown if an invalid annotation combination is detected like for example multiple
 * {@link RestrictedTo @RestrictedTo} annotations without a {@link RestrictionPolicy @RestrictionPolicy} annotation.
 */
public class InvalidAnnotationCombinationException extends RuntimeException {
    /**
     * The serial version UID of this class.
     */
    private static final long serialVersionUID = 1;

    /**
     * Constructs a new invalid annotation combination exception with the given message.
     *
     * @param message the detail message
     */
    @SuppressWarnings("CdiInjectionPointsInspection")
    public InvalidAnnotationCombinationException(String message) {
        super(message);
    }
}
