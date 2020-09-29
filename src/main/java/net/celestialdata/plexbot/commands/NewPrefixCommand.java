package net.celestialdata.plexbot.commands;

import com.vdurmont.emoji.EmojiParser;
import net.celestialdata.plexbot.commandhandler.Command;
import net.celestialdata.plexbot.commandhandler.CommandExecutor;
import net.celestialdata.plexbot.database.DatabaseDataManager;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;

/**
 * A command to configure the prefix the bot uses to listen for commands.
 */
public class NewPrefixCommand implements CommandExecutor {

    @Command(aliases = "prefix", description = "Update the prefix for the bot", category = "general", usage = "# prefix <New Prefix>\n//Sets the new prefix of the bot\nin this server.")
    public void onPrefixCommand(Message message, String[] args) {

        if (args.length == 0) {
            message.getChannel().sendMessage("You must specify a new prefix.");
        }

        if (args.length == 1) {
            String newPrefix = args[0];

            if (newPrefix.matches("[a-zA-Z0-9]+")) {
                message.getChannel().sendMessage("Your prefix cannot be numbers or letters, only symbols!");
            } else if (newPrefix.length() > 1) {
                message.getChannel().sendMessage("Your prefix can only be one symbol, not multiple.");
            } else {
                DatabaseDataManager.updateServerPrefix(message.getServer().map(Server::getId).orElseThrow(), newPrefix);

                message.getChannel().sendMessage(new EmbedBuilder()
                        .setTitle(EmojiParser.parseToUnicode(":tools:") + "  Bot Prefix Updated  " + EmojiParser.parseToUnicode(":tools:"))
                        .setDescription("The prefix for the bot has been updated to: `  " + args[0] + "  `")
                        .setColor(Color.BLUE)).exceptionally(ExceptionLogger.get());
            }

        }
    }
}
