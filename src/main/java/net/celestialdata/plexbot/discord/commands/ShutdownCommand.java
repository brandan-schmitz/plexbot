package net.celestialdata.plexbot.discord.commands;

import io.quarkus.arc.log.LoggerName;
import io.quarkus.runtime.Quarkus;
import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.awt.*;

@SuppressWarnings("unused")
@ApplicationScoped
public class ShutdownCommand implements Command<Message> {

    @LoggerName("net.celestialdata.plexbot.discord.commands.ShutdownCommand")
    Logger logger;

    @Override
    public void execute(Message incomingMessage, String prefix, String usedAlias, String parameterString) {
        incomingMessage.reply(new EmbedBuilder()
                .setTitle("Shutting Down")
                .setDescription("The bot will now begin its shutdown process. This may take several minutes depending " +
                        "on what is currently being executed.")
                .setColor(Color.GREEN)
        ).exceptionally(ExceptionLogger.get()).exceptionally(ExceptionLogger.get());

        Quarkus.asyncExit(0);
    }
}