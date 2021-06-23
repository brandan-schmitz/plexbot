package net.celestialdata.plexbot.dataobjects;

import net.celestialdata.plexbot.enumerators.FileType;

public class ParsedSubtitleFilename {
    public String id;

    public FileType fileType;

    public String language;

    public boolean isForced;

    public ParsedSubtitleFilename parseFilename(String unparsedFilename) {
        // Split the filename into parts based on the . character
        String[] filenameParts = unparsedFilename.split("\\.");

        // Parse the necessary information based upon its position in the filename
        if (filenameParts.length == 3) {
            this.id = filenameParts[0];
            this.language = filenameParts[1];
            this.fileType = FileType.determineFiletype(unparsedFilename);
            isForced = false;
        } else if (filenameParts.length == 4 && filenameParts[2].equalsIgnoreCase("forced")) {
            this.id = filenameParts[0];
            this.language = filenameParts[1];
            this.fileType = FileType.determineFiletype(unparsedFilename);
            this.isForced = true;
        }

        // Return the parsed subtitle file name object
        return this;
    }
}