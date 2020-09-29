package net.celestialdata.plexbot.commands;

import net.celestialdata.plexbot.commandhandler.Command;
import net.celestialdata.plexbot.commandhandler.CommandExecutor;
import org.javacord.api.entity.message.Message;
import org.javacord.api.util.logging.ExceptionLogger;

public class PurgeCommand implements CommandExecutor {

    /**
     * Command to purge messages from a channel
     *
     * @param message Message that contains the command
     */
    @Command(aliases = "purge", description = "Purge a user defined number of messages from the channel", category = "general", usage = "# purge <#>\n//Purge specified number of messages.")
    public void onPurgeCommand(Message message, String[] args) {
        message.getChannel().getMessages(Integer.parseInt(args[0]) + 1)
                .thenAccept(messages -> messages.deleteAll()
                        .exceptionally(ExceptionLogger.get()))
                .exceptionally(ExceptionLogger.get());
    }
}
