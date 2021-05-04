package net.celestialdata.plexbot.commands;

import net.celestialdata.plexbot.commandhandler.Command;
import net.celestialdata.plexbot.commandhandler.CommandExecutor;
import net.celestialdata.plexbot.utils.BotColors;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class OldRequestCommand implements CommandExecutor {

    @Command(aliases = {"request", "r"}, description = "Depreciated, use rm or rt instead.", async = true, category = "request", usage = "# Depreciated, use rm or rt instead.")
    public void onOldRequestCommand(Message message, String[] args) {
        message.getChannel().sendMessage(new EmbedBuilder()
                .setTitle("Depreciated Command")
                .setDescription("This command has been depreciated. Please use one of the following commands instead:")
                .addInlineField("!rm", "Request a movie")
                .addInlineField("!re", "Coming soon!!")
                .setColor(BotColors.ERROR)
        );
    }

}
