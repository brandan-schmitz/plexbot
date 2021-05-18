package net.celestialdata.plexbot.discord.commands;

import com.vdurmont.emoji.EmojiParser;
import io.quarkus.arc.log.LoggerName;
import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("unused")
@ApplicationScoped
public class PingCommand implements Command<Message> {

    @LoggerName("net.celestialdata.plexbot.discord.commands.PingCommand")
    Logger logger;

    @Override
    public void execute(Message incomingMessage, String prefix, String usedAlias, String parameterString) {
        // Get the time when the command was received
        Instant receivedTime = Instant.now();

        // Get the time the command was sent
        Instant sentTime = incomingMessage.getCreationTimestamp();

        // Calculate the difference between the command time and now
        long timeDiff = ChronoUnit.MILLIS.between(receivedTime, sentTime);

        incomingMessage.reply(new EmbedBuilder()
                .setTitle(EmojiParser.parseToUnicode(":tools:") + "  Plexbot Ping")
                .setDescription(timeDiff + " milliseconds")
                .setColor(Color.BLUE))
                .exceptionally(ExceptionLogger.get()
                ).exceptionally(ExceptionLogger.get());
    }
}