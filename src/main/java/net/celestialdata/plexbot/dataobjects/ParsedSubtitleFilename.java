package net.celestialdata.plexbot.dataobjects;

import net.celestialdata.plexbot.enumerators.FileType;

public class ParsedSubtitleFilename {
    public String id;

    public FileType fileType;

    public String language;

    public boolean isForced = false;

    public boolean isCC = false;

    public boolean isSDH = false;

    public ParsedSubtitleFilename parseFilename(String unparsedFilename) throws IllegalArgumentException {
        // Split the filename into parts based on the . character
        String[] filenameParts = unparsedFilename.split("\\.");

        // Ensure the filename ir properly formatted
        if (filenameParts.length < 3 || filenameParts.length > 5) {
            throw new IllegalArgumentException("Invalid filename format found for the following file:\n" + unparsedFilename +
                    "\n\nPlease check the following article for information on naming your subtitle files: " +
                    "https://support.plex.tv/articles/200471133-adding-local-subtitles-to-your-media" +
                    "\n\nJust remember to replace the name of the movie or show (when doing episodes) with the appropriate id from IMDb or TVDB.");
        } else if (filenameParts[0].matches("[^t0-9].*")) {
            throw new IllegalArgumentException("Invalid IMDb or TVDB code for the following file:\n" + unparsedFilename);
        } else if (filenameParts[1].length() != 3) {
            throw new IllegalArgumentException("Unrecognized language code for the following file:\n" + unparsedFilename +
                    "\n\nPlease make sure to use a valid, 3 letter ISO 639-2/B language code. You can find these codes at the following link:\n\n" +
                    "https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes");
        } else {
            // Parse the necessary information based upon its position in the filename
            if (filenameParts.length == 3) {
                this.id = filenameParts[0];
                this.language = filenameParts[1];
                this.fileType = FileType.determineFiletype(unparsedFilename);
                isForced = false;
                isCC = false;
                isSDH = false;
            } else if (filenameParts.length == 4) {
                parseCommonParts(unparsedFilename, filenameParts);
            } else {
                parseCommonParts(unparsedFilename, filenameParts);

                if (filenameParts[3].equalsIgnoreCase("forced")) {
                    this.isForced = true;
                } else if (filenameParts[3].equalsIgnoreCase("sdh")) {
                    this.isSDH = true;
                } else if (filenameParts[3].equalsIgnoreCase("cc")) {
                    this.isCC = true;
                } else {
                    throw new IllegalArgumentException("Unrecognized flag \"" + filenameParts[2] + "\" in the following subtitle filename:\n" + unparsedFilename +
                            "\n\nValid options are \"forced\", \"sdh\", or \"cc\".");
                }
            }

            // Return the parsed subtitle file name object
            return this;
        }
    }

    private void parseCommonParts(String unparsedFilename, String[] filenameParts) {
        this.id = filenameParts[0];
        this.language = filenameParts[1];
        this.fileType = FileType.determineFiletype(unparsedFilename);

        if (filenameParts[2].equalsIgnoreCase("forced")) {
            this.isForced = true;
        } else if (filenameParts[2].equalsIgnoreCase("sdh")) {
            this.isSDH = true;
        } else if (filenameParts[2].equalsIgnoreCase("cc")) {
            this.isCC = true;
        } else {
            throw new IllegalArgumentException("Unrecognized flag \"" + filenameParts[2] + "\" in the following subtitle filename:\n" + unparsedFilename +
                    "\n\nValid options are \"forced\", \"sdh\", or \"cc\".");
        }
    }
}