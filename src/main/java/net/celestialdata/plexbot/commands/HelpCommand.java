package net.celestialdata.plexbot.commands;


import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.commandhandler.Command;
import net.celestialdata.plexbot.commandhandler.CommandExecutor;
import net.celestialdata.plexbot.commandhandler.CommandHandler;
import net.celestialdata.plexbot.configuration.BotConfig;
import net.celestialdata.plexbot.utils.BotColors;
import net.celestialdata.plexbot.utils.BotEmojis;
import net.celestialdata.plexbot.utils.PagedEmbed;
import net.celestialdata.plexbot.utils.StringBuilderPlus;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

/**
 * A help command for the bot
 *
 * @author Celestialdeath99
 */
public class HelpCommand implements CommandExecutor {

    /**
     * A help command for the bot
     *
     * @param message Message that contains the command
     * @param args    The argument that specifies a specific command to get help with
     */
    @Command(aliases = "help", description = "Gives information about the bots commands", async = true, category = "general", usage = "# help\n//Lists available commands\n# help [command]\n//Gives information about a command")
    public void onHelpCommand(Message message, String[] args) {
        final String serverPrefix = BotConfig.getInstance().botPrefix();

        // If it is just the regular help command
        if (args.length == 0) {

            // Create initial embed object
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(BotEmojis.QUESTION + "  Command Help")
                    .setDescription("\u200b")
                    .setColor(BotColors.INFO);

            // Create pagedEmbed object
            PagedEmbed pagedEmbed = new PagedEmbed(message.getChannel(), embed, serverPrefix);

            // Create StringBuilderPlus objects to handle the details for commands
            StringBuilderPlus generalCommands = new StringBuilderPlus();
            StringBuilderPlus managementCommands = new StringBuilderPlus();
            StringBuilderPlus requestCommands = new StringBuilderPlus();

            // Add start of code blocks to command sections
            generalCommands.appendLine("```");
            managementCommands.appendLine("```");
            requestCommands.appendLine("```");

            // Get information for each command and build the strings
            for (CommandHandler.SimpleCommand simpleCommand : Main.getCommandHandler().getCommands())
                if (simpleCommand.getCommandAnnotation().showInHelpPage()) {
                    if (simpleCommand.getCommandAnnotation().category().equalsIgnoreCase("general")) {
                        // Get how many aliases to the command there are
                        int numAliases = simpleCommand.getCommandAnnotation().aliases().length;

                        // For each alias, add it to the general commands list
                        for (int i = 0; i < numAliases; i++) {
                            generalCommands.appendLine(simpleCommand.getCommandAnnotation().aliases()[i]);
                        }
                    } else if (simpleCommand.getCommandAnnotation().category().equalsIgnoreCase("request")) {
                        // Get how many aliases to the command there are
                        int numAliases = simpleCommand.getCommandAnnotation().aliases().length;

                        // For each alias, add it to the general commands list
                        for (int i = 0; i < numAliases; i++) {
                            requestCommands.appendLine(simpleCommand.getCommandAnnotation().aliases()[i]);
                        }
                    }
                }


            // Add end of code blocks to command sections
            generalCommands.appendLine("```");
            requestCommands.appendLine("```");

            // Add the commands sections to the pagedCommands object
            pagedEmbed.addField("General Commands:", generalCommands.toString());
            pagedEmbed.addField("Request Commands:", requestCommands.toString());

            // Build the help message
            pagedEmbed.build().exceptionally(ExceptionLogger.get());
        }

        // Display the message about a command if it was specified
        else if (args.length == 1) {
            // Used to trigger unknown command reaction
            boolean foundCommand = false;

            // Check if the argument specified matches a command and build a help message if it does
            for (CommandHandler.SimpleCommand simpleCommand : Main.getCommandHandler().getCommands()) {

                // Create objects used for checking all the aliases of a command
                int numAliases = simpleCommand.getCommandAnnotation().aliases().length;

                // Cycle through all command aliases and look for a match
                for (int i = 0; i < numAliases; i++) {
                    if (simpleCommand.getCommandAnnotation().aliases()[i].equals(args[0])) {
                        // Do not trigger and unknown command reaction
                        foundCommand = true;

                        // StringBuilderPlus object to handle all command aliases
                        StringBuilderPlus aliases = new StringBuilderPlus();

                        // Add all command aliases to aliases object
                        for (int a = 0; a < simpleCommand.getCommandAnnotation().aliases().length; a++) {
                            aliases.appendLine(serverPrefix + simpleCommand.getCommandAnnotation().aliases()[a]);
                        }

                        // Build and send message
                        message.getChannel().sendMessage(new EmbedBuilder()
                                .setTitle(BotEmojis.QUESTION + " " + BotConfig.getInstance().botName() + " Help: " + simpleCommand.getCommandAnnotation().aliases()[0].toUpperCase() + " " + BotEmojis.QUESTION)
                                .setDescription("\u200b")
                                .addField(BotEmojis.KEYBOARD + " Accessible Through:", "```" + aliases.toString() + "```\u200b")
                                .addField(BotEmojis.NOTEPAD + " Description:", "```" + simpleCommand.getCommandAnnotation().description() + "```\u200b")
                                .addField(BotEmojis.GEAR + " Usages:", "```MD\n" + simpleCommand.getCommandAnnotation().usage() + "```")
                                .setFooter("<> indicates an argument, [] indicates an optional argument. Do not use <> or [] in a command.")
                                .setColor(BotColors.INFO)
                        ).exceptionally(ExceptionLogger.get());
                    }
                }
            }

            // If the argument specified is not a command respond with a question mark
            if (!foundCommand) {
                message.addReaction(BotEmojis.QUESTION).exceptionally(ExceptionLogger.get());
            }
        }
    }
}