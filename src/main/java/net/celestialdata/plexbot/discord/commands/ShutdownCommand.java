package net.celestialdata.plexbot.discord.commands;

import io.quarkus.arc.log.LoggerName;
import io.quarkus.runtime.Quarkus;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.awt.*;

@SuppressWarnings("unused")
@ApplicationScoped
public class ShutdownCommand implements Command<Message> {

    @LoggerName("net.celestialdata.plexbot.discord.commands.ShutdownCommand")
    Logger logger;

    @ConfigProperty(name = "BotSettings.ownerID")
    Long ownerId;

    @Inject
    MessageFormatter messageFormatter;

    @Override
    public void handleFailure(Throwable error) {
        logger.error(error);
    }

    @Override
    public void execute(Message incomingMessage, String prefix, String usedAlias, String parameterString) {
        if (incomingMessage.getAuthor().getId() == ownerId) {
            incomingMessage.reply(new EmbedBuilder()
                    .setTitle("Shutting Down")
                    .setDescription("The bot will now begin its shutdown process. This may take several minutes depending " +
                            "on what is currently being executed.")
                    .setColor(Color.GREEN)
            ).exceptionally(ExceptionLogger.get()).exceptionally(ExceptionLogger.get());

            Quarkus.asyncExit(0);
        } else {
            incomingMessage.reply(messageFormatter.errorMessage("You are not authorized to use this command. If you believe this is a mistake, please contact Brandan."));
        }
    }
}