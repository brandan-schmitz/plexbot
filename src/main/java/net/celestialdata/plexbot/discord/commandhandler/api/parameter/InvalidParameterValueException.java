package net.celestialdata.plexbot.discord.commandhandler.api.parameter;

/**
 * An invalid parameter value exception that is thrown by {@link ParameterConverter}s
 * if the value of the parameter is invalid though the format was correct, for example the id of an unknown user.
 * The message should be written in a way so that it can be directly presented to the end user.
 */
public class InvalidParameterValueException extends ParameterParseException {
    /**
     * The serial version UID of this class.
     */
    private static final long serialVersionUID = 1;

    /**
     * Constructs a new invalid parameter value exception with the given message.
     * The message should be written in a way so that it can be directly presented to the end user.
     *
     * @param message the detail message
     */
    @SuppressWarnings({"unused", "CdiInjectionPointsInspection"})
    public InvalidParameterValueException(String message) {
        super(message);
    }

    /**
     * Constructs a new invalid parameter value exception with the given message and cause.
     * The message should be written in a way so that it can be directly presented to the end user.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    @SuppressWarnings("unused")
    public InvalidParameterValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
