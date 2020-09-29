package net.celestialdata.plexbot.utils;

/**
 * A utility to easily build multi-line strings
 *
 * @author Celestialdeath99
 */
public class StringBuilderPlus {

    // Create a private StingBuilder object
    private StringBuilder sb;

    /**
     * The StringBuilderPlus method
     */
    public StringBuilderPlus() {
        sb = new StringBuilder();
    }

    /**
     * Appends data to the current line of a string
     *
     * @param str The data to be appended to the current line
     */
    public void append(String str) {
        sb.append(str != null ? str : "");
    }

    /**
     * Appends data to a newline in the string
     *
     * @param str The data to be appended to a newline
     */
    public void appendLine(String str) {
        sb.append(str != null ? str : "").append(System.getProperty("line.separator"));
    }

    /**
     * Builds the StringBuilderPlus to a string
     *
     * @return Returns the string with all the data that was provided.
     */
    public String toString() {
        return sb.toString();
    }
}