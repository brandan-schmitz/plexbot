package net.celestialdata.plexbot.utils;

import org.apache.commons.lang3.StringUtils;

public class FilenameSanitizer {

    public static String sanitize(String input) {
        input = input.replace("<", "");
        input = input.replace(">", "");
        input = input.replace(":", "");
        input = input.replace("\"", "");
        input = input.replace("/", "");
        input = input.replace("<\\", "");
        input = input.replace("|", "");
        input = input.replace("?", "");
        input = input.replace("*", "");
        input = input.replace(".", "");
        input = input.replace("·", "-");
        input = input.replace("–", "-");
        input = input.replace("Æ", "Ae");
        input = StringUtils.stripAccents(input);
        return input;
    }
}
