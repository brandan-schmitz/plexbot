package net.celestialdata.plexbot.utilities;

import io.quarkus.arc.log.LoggerName;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.periodictasks.BotStatusDisplay;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.jboss.logging.Logger;

import javax.inject.Inject;

@SuppressWarnings("unused")
public abstract class BotProcess {
    public String processId;
    public String processString;

    @LoggerName("net.celestialdata.plexbot.utilities.BotProcess")
    Logger logger;

    @Inject
    public BotStatusDisplay botStatusDisplay;

    @Inject
    DiscordApi discordApi;

    @Inject
    MessageFormatter messageFormatter;

    @ConfigProperty(name = "BotSettings.ownerID")
    Long botOwnerId;

    public void configureProcess(String processString) {
        // Configure the UncaughtExceptionHandler
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            logger.error(e);
            endProcess(e);
        });

        // Configure the process string and submit the process to the BotStatusDisplay
        this.processString = processString;
        this.processId = botStatusDisplay.submitProcess(processString);
    }

    public void configureProcess(String processString, Message replyMessage) {
        // Configure the UncaughtExceptionHandler
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            logger.error(e);
            replyMessage.edit(messageFormatter.errorMessage("An unknown error occurred. Brandan has been notified of the error.", e.getMessage()));
            endProcess(e);
        });

        // Configure the process string and submit the process to the BotStatusDisplay
        this.processString = processString;
        this.processId = botStatusDisplay.submitProcess(processString);
    }

    public void updateProcessString(String newProcessString) {
        this.processString = newProcessString;
        botStatusDisplay.updateProcess(processId, processString);
    }

    public void reportError(Throwable error) {
        new MessageBuilder()
                .append("An error has occurred while performing the following task:", MessageDecoration.BOLD)
                .appendCode("", processString)
                .appendCode("java", ExceptionUtils.getMessage(error))
                .appendCode("java", ExceptionUtils.getStackTrace(error))
                .send(discordApi.getUserById(botOwnerId).join());
    }

    public void reportError(Throwable error, String identifier) {
        new MessageBuilder()
                .append("An error has occurred while performing the following task:", MessageDecoration.BOLD)
                .appendCode("", processString + " - " + identifier)
                .appendCode("java", ExceptionUtils.getMessage(error))
                .appendCode("java", ExceptionUtils.getStackTrace(error))
                .send(discordApi.getUserById(botOwnerId).join());
    }

    public void endProcess() {
        botStatusDisplay.removeProcess(processId);
    }

    public void endProcess(Throwable error) {
        botStatusDisplay.removeProcess(processId);
        reportError(error);
    }
}