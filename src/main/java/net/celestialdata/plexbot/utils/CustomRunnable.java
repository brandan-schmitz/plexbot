package net.celestialdata.plexbot.utils;

import net.celestialdata.plexbot.managers.BotStatusManager;

public interface CustomRunnable extends Runnable {
    String taskName();

    default void endTask() {
        BotStatusManager.getInstance().removeProcess(taskName());
    }

    default void endTask(Throwable error) {
        BotStatusManager.getInstance().removeProcess(taskName());
        // TODO: Configure logging of errors to discord error log channel
    }
}