package net.celestialdata.plexbot.commands;

import net.celestialdata.plexbot.commandhandler.Command;
import net.celestialdata.plexbot.commandhandler.CommandExecutor;
import net.celestialdata.plexbot.utils.BotColors;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class RequestEpisodeCommand implements CommandExecutor {

    /**
     * A command to request episodes be added to the server
     *
     * @param message the imdb ID of the episode
     * @param args    any arguments for the command
     */
    @Command(aliases = {"re"}, description = "Request a movie be added to the server", async = true, category = "request", usage = "# request <movie name>\n//Request a movie.\n# r <movie name>\n//Request a movie.")
    public void onRequestEpisodeCommand(Message message, String[] args) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Command not implemented:")
                .setDescription("This command has not been implemented yet. Please try again later after the command has been implemented.")
                .setColor(BotColors.ERROR);

        // Send the message
        message.getChannel().sendMessage(embed);

        /*
        if (args.length == 0) {
            // Build a message saying a argument is needed
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Command Error:")
                    .setDescription("You must specify the episode ID you wish to request. For more information please use " +
                            BotConfig.getInstance().botPrefix() + "help request")
                    .setColor(BotColors.ERROR);

            // Send the message
            message.getChannel().sendMessage(embed);
        } else {
            String id = args[0];

            // Configure the process name
            String processName = "Episode Request: " + message.getIdAsString();

            // Start the worker for handling the movie request.
            // If the bot is already maxed on work, display a message about waiting.
            if (BotWorkPool.getInstance().isPoolFull()) {
                // TODO: If needing to wait, have bot send a PM when their request is ready to be handled
                message.getChannel().sendMessage(new EmbedBuilder()
                        .setTitle("Fuck off, I am busy...")
                        .setDescription("Just kidding!!\n\nSeriously though, I am actually busy. I am currently processing too many other requests, " +
                                "checking for movies in the waiting list, or checking to see if any movies on Plex can be upgraded to a better " +
                                "resolution. As a result, it may be a few minutes before I can get to your request. There are currently "
                                + BotWorkPool.getInstance().getNumTasksInQueue() + " task(s) ahead of yours." +
                                "\n\n**Please wait until this message has been updated.**")
                        .setColor(BotColors.WARNING)
                ).thenAccept(message1 -> BotWorkPool.getInstance().submitProcess(new EpisodeRequestHandler(processName, id, message1, message.getAuthor().getId())))
                        .exceptionally(ExceptionLogger.get());
            } else {
                message.getChannel().sendMessage(new EmbedBuilder()
                        .setTitle("Searching IMDB...")
                        .setDescription("I am searching the Internet Movie Database for your episode. This may take a few seconds.")
                        .setColor(BotColors.INFO)
                ).thenAccept(message1 -> BotWorkPool.getInstance().submitProcess(new EpisodeRequestHandler(processName, id, message1, message.getAuthor().getId())))
                        .exceptionally(ExceptionLogger.get());
            }
        }
         */
    }
}