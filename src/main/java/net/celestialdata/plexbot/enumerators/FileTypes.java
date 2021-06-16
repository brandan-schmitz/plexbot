package net.celestialdata.plexbot.enumerators;

@SuppressWarnings("CdiInjectionPointsInspection")
public enum FileTypes {
    AVI(".avi"),
    DIVX(".divx"),
    FLV(".flv"),
    M4V(".m4v"),
    MKV(".mkv"),
    MP4(".mp4"),
    MPEG(".mpeg"),
    MPG(".mpg"),
    WMV(".wmv"),
    SRT(".srt"),
    SMI(".smi"),
    SSA(".ssa"),
    ASS(".ass"),
    VTT(".vtt");

    FileTypes(String extension) {
        this.extension = extension;
    }

    private final String extension;

    public String getExtension() {
        return this.extension;
    }

    public boolean isVideo() {
        return this == AVI || this == DIVX || this == FLV || this == M4V || this == MKV || this == MP4 || this == MPEG || this == MPG || this == WMV;
    }

    public boolean isSubtitle() {
        return this == SRT || this == SMI || this == SSA || this == ASS || this == VTT;
    }

}