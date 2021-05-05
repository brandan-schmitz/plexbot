package net.celestialdata.plexbot.clients.models.omdb;

public enum OmdbSearchType {
    MOVIE("movie"),
    SERIES("series");


    public final String label;

    @SuppressWarnings("CdiInjectionPointsInspection")
    OmdbSearchType(String label) {
        this.label = label;
    }
}