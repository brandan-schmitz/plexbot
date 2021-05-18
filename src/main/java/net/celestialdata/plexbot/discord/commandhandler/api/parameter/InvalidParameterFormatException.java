package net.celestialdata.plexbot.discord.commandhandler.api.parameter;

/**
 * An invalid parameter format exception that is thrown by {@link ParameterConverter}s
 * if the format of the parameter is invalid, for example some text for a number-parsing converter.
 * The message should be written in a way so that it can be directly presented to the end user.
 */
public class InvalidParameterFormatException extends ParameterParseException {
    /**
     * The serial version UID of this class.
     */
    private static final long serialVersionUID = 1;

    /**
     * Constructs a new invalid parameter format exception with the given message.
     * The message should be written in a way so that it can be directly presented to the end user.
     *
     * @param message the detail message
     */
    @SuppressWarnings({"unused", "CdiInjectionPointsInspection"})
    public InvalidParameterFormatException(String message) {
        super(message);
    }

    /**
     * Constructs a new invalid parameter format exception with the given message and cause.
     * The message should be written in a way so that it can be directly presented to the end user.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    @SuppressWarnings("unused")
    public InvalidParameterFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
