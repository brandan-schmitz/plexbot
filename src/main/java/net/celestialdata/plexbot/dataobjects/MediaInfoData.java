package net.celestialdata.plexbot.dataobjects;

public class MediaInfoData {
    public String codec;
    public int duration;
    public int height;
    public int width;

    public boolean isOptimized() {
        if (this.codec == null) {
            throw new NullPointerException("Unable to determine media codec type.");
        } else {
            return this.codec.contains("265");
        }
    }

    public int resolution() {
        var resolution = 0;

        if (height > 0 && height <= 240) {
            resolution = 240;
        } else if (height > 240 && height <= 360) {
            resolution = 360;
        } else if (height > 360 && height <= 480) {
            resolution = 480;
        } else if (height > 480 && height <= 720) {
            resolution = 720;
        } else if (height > 720 && height <= 1080) {
            resolution = 1080;
        } else if (height > 1080) {
            resolution = 2160;
        }

        return resolution;
    }
}