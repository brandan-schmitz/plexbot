package net.celestialdata.plexbot.database.models;

/**
 * Interfaces used to define the database object models. Provides hooks for running custom actions
 * when performing some database actions such as creating/updating, or deleting an object in the database.
 */
public interface BaseModel {
    /**
     * Provide a hook to run specific actions when a object is being deleted from the database. This is
     * executed prior to the object being deleted.
     */
    default void onDelete() {}

    /**
     * Provide a hook to run specific actions when a object is being saved to the database. This action
     * is triggered when both updating or saving a new object to the database. You can check if this is object
     * is being updated or saved by checking if the object already exists in the database.
     */
    default void onSave() {}
}