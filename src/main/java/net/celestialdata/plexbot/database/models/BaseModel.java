package net.celestialdata.plexbot.database.models;

public interface BaseModel {
    default void onDelete() {
    }
}
