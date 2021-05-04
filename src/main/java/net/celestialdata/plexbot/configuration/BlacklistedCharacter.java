package net.celestialdata.plexbot.configuration;

/**
 * Represents a single item in the list of characters/strings that are
 * blacklisted from appearing in a file or folder name.
 */
public class BlacklistedCharacter {
    private final String original;
    private final String replacement;

    /**
     * Construct a new BlacklistedCharacter object
     *
     * @param original      the character that needs to be removed
     * @param replacement   the character to replace the blacklisted character
     */
    public BlacklistedCharacter(String original, String replacement) {
        this.original = original;
        this.replacement = replacement;
    }

    /**
     * Return the blacklisted character
     *
     * @return blacklisted character
     */
    public String getOriginal() {
        return original;
    }

    /**
     * Return the replacement character
     *
     * @return replacement character
     */
    public String getReplacement() {
        return replacement;
    }
}