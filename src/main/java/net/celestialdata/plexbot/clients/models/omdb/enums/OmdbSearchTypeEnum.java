package net.celestialdata.plexbot.clients.models.omdb.enums;

@SuppressWarnings("unused")
public enum OmdbSearchTypeEnum {
    MOVIE("movie"),
    SERIES("series");

    public final String label;

    @SuppressWarnings("CdiInjectionPointsInspection")
    OmdbSearchTypeEnum(String label) {
        this.label = label;
    }
}