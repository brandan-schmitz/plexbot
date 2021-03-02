package net.celestialdata.plexbot.utils;

import uk.co.caprica.vlcjinfo.MediaInfo;

public class MediaInfoHelper {

    public static int getHeight(String filename) {
        MediaInfo mediaInfo = MediaInfo.mediaInfo(filename);
        return Integer.parseInt(mediaInfo.first("Video").value("Height")
                .replace(" ", "").replace("pixels", ""));
    }

    public static int getWidth(String filename) {
        MediaInfo mediaInfo = MediaInfo.mediaInfo(filename);
        return Integer.parseInt(mediaInfo.first("Video").value("Width")
                .replace(" ", "").replace("pixels", ""));
    }

    public static String getFilesize(String filename) {
        MediaInfo mediaInfo = MediaInfo.mediaInfo(filename);
        return mediaInfo.first("General").value("File size");
    }
}
