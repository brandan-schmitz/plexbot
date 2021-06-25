package net.celestialdata.plexbot.dataobjects;

import net.celestialdata.plexbot.enumerators.FileType;

public class ParsedMediaFilename {
    public String id;

    public FileType fileType;

    public ParsedMediaFilename parseFilename(String unparsedFilename) throws IllegalArgumentException {
        // Split the filename into parts based on the . character
        String[] filenameParts = unparsedFilename.split("\\.");

        // Ensure the file is properly named
        if (filenameParts.length != 2) {
            throw new IllegalArgumentException("Invalid filename format found for the following file:\n" + unparsedFilename);
        } else if (filenameParts[0].matches("[^t0-9].*")) {
            throw new IllegalArgumentException("Invalid IMDb or TVDB code for the following file:\n" + unparsedFilename);
        } else {
            // Parse the necessary information based upon its position in the filename
            this.id = filenameParts[0];
            this.fileType = FileType.determineFiletype(unparsedFilename);

            // Return the parsed subtitle file name object
            return this;
        }
    }
}