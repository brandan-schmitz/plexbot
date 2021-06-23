package net.celestialdata.plexbot.dataobjects;

import net.celestialdata.plexbot.enumerators.FileType;

public class ParsedMediaFilename {
    public String id;

    public FileType fileType;

    public ParsedMediaFilename parseFilename(String unparsedFilename) {
        // Split the filename into parts based on the . character
        String[] filenameParts = unparsedFilename.split("\\.");

        // Parse the necessary information based upon its position in the filename
        this.id = filenameParts[0];
        this.fileType = FileType.determineFiletype(unparsedFilename);

        // Return the parsed subtitle file name object
        return this;
    }
}