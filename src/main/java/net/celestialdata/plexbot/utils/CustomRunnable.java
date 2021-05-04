package net.celestialdata.plexbot.utils;

import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.configuration.BotConfig;
import net.celestialdata.plexbot.managers.BotStatusManager;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;

public interface CustomRunnable extends Runnable {
    String taskName();

    default boolean cancelOnFull() {
        return false;
    }

    default boolean cancelOnDuplicate() {
        return false;
    }

    default void endTask() {
        BotStatusManager.getInstance().removeProcess(taskName());
    }

    default void endTask(Throwable error) {
        BotStatusManager.getInstance().removeProcess(taskName());
        reportError(error);
    }

    default void reportError(Throwable error) {
        new MessageBuilder()
                .append("An error has occurred while performing the following task:", MessageDecoration.BOLD)
                .appendCode("", taskName())
                .appendCode("java", ExceptionUtils.getMessage(error))
                .appendCode("java", ExceptionUtils.getStackTrace(error))
                .send(Main.getBotApi().getUserById(BotConfig.getInstance().adminUserId()).join());
    }

    default void reportError(Throwable error, String identifier) {
        new MessageBuilder()
                .append("An error has occurred while performing the following task:", MessageDecoration.BOLD)
                .appendCode("", taskName() + " - " + identifier)
                .appendCode("java", ExceptionUtils.getMessage(error))
                .appendCode("java", ExceptionUtils.getStackTrace(error))
                .send(Main.getBotApi().getUserById(BotConfig.getInstance().adminUserId()).join());
    }
}