package net.celestialdata.plexbot.commands;

import com.vdurmont.emoji.EmojiParser;
import net.celestialdata.plexbot.BotConfig;
import net.celestialdata.plexbot.commandhandler.Command;
import net.celestialdata.plexbot.commandhandler.CommandExecutor;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class PingCommand implements CommandExecutor {

    /**
     * Command to check the ping response of the bot
     *
     * @param message Message that contains the command
     */
    @Command(aliases = "ping", description = "Get the ping of the bot", category = "general", usage = "# ping\n//Gets the ping of the bot")
    public void onPingCommand(Message message) {
        // Get the current system time
        Instant sysTime = Instant.now();

        // Get the time the message was created
        Instant time = message.getCreationTimestamp();

        // Calculate the difference between when the message was created and the bot received the message
        long timeDiff = ChronoUnit.MILLIS.between(time, sysTime);

        // Build and send the message with the ping result
        message.getChannel().sendMessage(new EmbedBuilder()
                .setTitle(EmojiParser.parseToUnicode(":tools:") + "  " + BotConfig.getInstance().botName() + " Ping  " + EmojiParser.parseToUnicode(":tools:"))
                .addInlineField("Response Time:", timeDiff + " milliseconds")
                .setColor(Color.BLUE))
                .exceptionally(ExceptionLogger.get());
    }
}
