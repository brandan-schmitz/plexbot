package net.celestialdata.plexbot.utils;

import net.celestialdata.plexbot.configuration.BlacklistedCharacter;
import net.celestialdata.plexbot.configuration.BotConfig;
import org.apache.commons.lang3.StringUtils;

/**
 * Provide a way to ensure file and folder names are sanitized for use
 * with the filesystem.
 */
public abstract class FilenameSanitizer {
    public static String sanitize(String input) {
        // Replace any characters that are defined in the config
        for (BlacklistedCharacter character : BotConfig.getInstance().blacklistedCharacters()) {
            input = input.replace(character.getOriginal(), character.getReplacement());
        }

        // Remove any accents and return the sanitized string
        input = StringUtils.stripAccents(input);
        return input;
    }
}