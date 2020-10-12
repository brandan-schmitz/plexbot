package net.celestialdata.plexbot.commands;

import net.celestialdata.plexbot.BotWorkPool;
import net.celestialdata.plexbot.commandhandler.Command;
import net.celestialdata.plexbot.commandhandler.CommandExecutor;
import net.celestialdata.plexbot.workhandlers.RequestHandler;
import net.celestialdata.plexbot.database.DatabaseDataManager;
import net.celestialdata.plexbot.utils.BotColors;
import net.celestialdata.plexbot.utils.BotEmojis;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.logging.ExceptionLogger;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class RequestMovieCommand implements CommandExecutor {
    private volatile String year = "";
    private volatile String id = "";

    /**
     * A command to request movies be added to the server
     *
     * @param message the title or command of the movie
     * @param args any arguments for the command such as requesting a movie by its ID
     */
    @Command(aliases = {"request", "r"}, description = "Request a movie be added to the server", async = true, category = "request", usage = "# request <movie name>\n//Request a movie.\n# r <movie name>\n//Request a movie.")
    public void onRequestMovieCommand(Message message, String[] args) {
        year = "";
        id = "";

        if (args.length == 0) {
            // Build a message saying a argument is needed
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(BotEmojis.ERROR + "  Command Error:")
                    .setDescription("You must specify the movie you wish to request. For more information please use " +
                            DatabaseDataManager.getServerPrefix(message.getServer().map(Server::getId).orElseThrow()) + "help request")
                    .setColor(BotColors.ERROR);

            // Send the message
            message.getChannel().sendMessage(embed);
        } else {
            StringBuilder movieTitle = new StringBuilder();

            // Combine the arguments into a movie name
            int numArgs = args.length;
            int i = 1;
            for (String a : args) {
                if (a.startsWith("--year=")) {
                    year = a.replace("--year=", "");
                } else if (a.startsWith("--id=")) {
                    id = a.replace("--id=", "");
                } else {
                    movieTitle.append(a);
                    if (numArgs - i > 0) {
                        movieTitle.append(" ");
                        i++;
                    }
                }
            }

            String processName = "Movie Request: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(ZonedDateTime.now());

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
                ).thenAccept(message1 -> BotWorkPool.getInstance().submitProcess(processName, new RequestHandler(
                        processName, movieTitle.toString(), year, id, message1, message.getUserAuthor().map(User::getId).orElseThrow()
                ))).exceptionally(ExceptionLogger.get());
            } else {
                message.getChannel().sendMessage(new EmbedBuilder()
                        .setTitle("Searching IMDB...")
                        .setDescription("I am searching the Internet Movie Database for your movie. This may take a few seconds.")
                        .setColor(BotColors.INFO)
                ).thenAccept(message1 -> BotWorkPool.getInstance().submitProcess(processName, new RequestHandler(
                        processName, movieTitle.toString(), year, id, message1, message.getUserAuthor().map(User::getId).orElseThrow()
                ))).exceptionally(ExceptionLogger.get());
            }
        }
    }
}