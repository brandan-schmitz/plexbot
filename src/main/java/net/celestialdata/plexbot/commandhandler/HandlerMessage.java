package net.celestialdata.plexbot.commandhandler;

/**
 * All messages used in the lib.
 *
 * @author Celestialdeath99
 */
public enum HandlerMessage {

    MISSING_PERMISSIONS("You are not allowed to use this command!");

    private String message;

    HandlerMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the message.
     *
     * @return The message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message. Set it to <code>null</code> if you don't want any output.
     *
     * @param message The message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
