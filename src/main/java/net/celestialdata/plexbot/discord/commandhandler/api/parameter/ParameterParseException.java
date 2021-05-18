package net.celestialdata.plexbot.discord.commandhandler.api.parameter;

/**
 * A parameter parse exception that is thrown if an exception happens during calling a {@link ParameterConverter}.
 * The message should be written in a way so that it can be directly presented to the end user.
 */
public class ParameterParseException extends IllegalArgumentException {
    /**
     * The serial version UID of this class.
     */
    private static final long serialVersionUID = 1;

    /**
     * The name of the parameter that was parsed.
     */
    private String parameterName;

    /**
     * The value of the parameter that was parsed.
     */
    private String parameterValue;

    /**
     * Constructs a new parameter parse exception with the given message.
     * The message should be written in a way so that it can be directly presented to the end user.
     *
     * @param message the detail message
     */
    @SuppressWarnings("CdiInjectionPointsInspection")
    public ParameterParseException(String message) {
        super(message);
    }

    /**
     * Constructs a new parameter parse exception with the given message and cause.
     * The message should be written in a way so that it can be directly presented to the end user.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public ParameterParseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new parameter parse exception with the given message, parameter name and parameter value.
     * The message should be written in a way so that it can be directly presented to the end user.
     *
     * @param message        the detail message
     * @param parameterName  the name of the parameter that was parsed
     * @param parameterValue the value of the parameter that was parsed
     */
    @SuppressWarnings("unused")
    public ParameterParseException(String parameterName, String parameterValue, String message) {
        this(message);
        this.parameterName = parameterName;
        this.parameterValue = parameterValue;
    }

    /**
     * Constructs a new parameter parse exception with the given message, cause, parameter name and parameter value.
     * The message should be written in a way so that it can be directly presented to the end user.
     *
     * @param message        the detail message
     * @param cause          the cause
     * @param parameterName  the name of the parameter that was parsed
     * @param parameterValue the value of the parameter that was parsed
     */
    public ParameterParseException(String parameterName, String parameterValue, String message, Throwable cause) {
        this(message, cause);
        this.parameterName = parameterName;
        this.parameterValue = parameterValue;
    }

    /**
     * Returns the name of the parameter that was parsed.
     *
     * @return the name of the parameter that was parsed
     */
    @SuppressWarnings("unused")
    public String getParameterName() {
        return parameterName;
    }

    /**
     * Sets the name of the parameter that was parsed.
     *
     * @param parameterName the name of the parameter that was parsed
     */
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    /**
     * Returns the value of the parameter that was parsed.
     *
     * @return the value of the parameter that was parsed
     */
    @SuppressWarnings("unused")
    public String getParameterValue() {
        return parameterValue;
    }

    /**
     * Sets the value of the parameter that was parsed.
     *
     * @param parameterValue the value of the parameter that was parsed
     */
    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }
}
