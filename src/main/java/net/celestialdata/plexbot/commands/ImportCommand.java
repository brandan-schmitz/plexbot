package net.celestialdata.plexbot.commands;

import net.celestialdata.plexbot.BotWorkPool;
import net.celestialdata.plexbot.commandhandler.Command;
import net.celestialdata.plexbot.commandhandler.CommandExecutor;
import net.celestialdata.plexbot.managers.ImportManager;
import net.celestialdata.plexbot.utils.BotColors;
import net.celestialdata.plexbot.utils.BotEmojis;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;


public class ImportCommand implements CommandExecutor {

    /**
     * A command to request media be imported to the server
     *
     * @param message the command
     * @param args    any arguments for the command such as enabling the overwriting of media
     */
    @Command(aliases = {"import", "i"}, description = "Import media in the import folder", async = true, category = "request", usage = "# import\n//Import media\n# i\n//Import media")
    public void onRequestMovieCommand(Message message, String[] args) {
        EmbedBuilder embed;

        if (BotWorkPool.getInstance().isPoolFull()) {
            embed = new EmbedBuilder()
                    .setTitle("Fuck off, I am busy...")
                    .setDescription("Just kidding!!\n\nSeriously though, I am actually busy. I am currently processing too many other requests, " +
                            "checking for movies in the waiting list, or checking to see if any movies on Plex can be upgraded to a better " +
                            "resolution. As a result, it may be a few minutes before I can get to your request. There are currently "
                            + BotWorkPool.getInstance().getNumTasksInQueue() + " task(s) ahead of yours." +
                            "\n\n**Please wait until this message has been updated.**")
                    .setColor(BotColors.WARNING);
        } else {
            embed = new EmbedBuilder()
                    .setTitle(BotEmojis.INFO + "  Import Started:")
                    .setDescription("The bot has started the process of importing your media. Please " +
                            "check the bot status channel to see the current progress of the import. Any warnings " +
                            "or errors will be sent to this channel.")
                    .setColor(BotColors.INFO);
        }

        // Send the message
        message.getChannel().sendMessage(embed).thenAccept(sentMessage -> {
            boolean overwrite = false;

            if (args.length > 0) {
                for (String a : args) {
                    if (a.equalsIgnoreCase("--overwrite")) {
                        overwrite = true;
                        break;
                    }
                }
            }

            BotWorkPool.getInstance().submitProcess(new ImportManager(sentMessage, overwrite));
        }).exceptionally(ExceptionLogger.get());
    }
}