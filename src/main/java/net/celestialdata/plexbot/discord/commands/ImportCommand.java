package net.celestialdata.plexbot.discord.commands;

import io.quarkus.arc.log.LoggerName;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.processors.ImportMediaProcessor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;

@SuppressWarnings("unused")
@ApplicationScoped
public class ImportCommand implements Command<Message> {

    @LoggerName("net.celestialdata.plexbot.discord.commands.ImportCommand")
    Logger logger;

    @ConfigProperty(name = "ImportSettings.authorizedUsers")
    List<String> authorizedUsers;

    @Inject
    Instance<ImportMediaProcessor> importMediaProcessor;

    @Inject
    MessageFormatter messageFormatter;

    @Override
    public void handleFailure(Throwable error) {
        logger.error(error);
    }

    @Override
    public void execute(Message incomingMessage, String prefix, String usedAlias, String parameterString) {
        if (authorizedUsers == null || authorizedUsers.isEmpty() || authorizedUsers.contains(String.valueOf(incomingMessage.getAuthor().getId()))) {
            // Configure the default parameter options
            var skipSync = false;
            var overwrite = false;
            var optimized = false;
            var includeOptimized = false;

            // Parse the provided parameters
            var params = parameterString.split("\\s+");

            // Check if any of the parameters were used
            for (String param : params) {
                if (param.equalsIgnoreCase("--skip-sync")) {
                    skipSync = true;
                } else if (param.equalsIgnoreCase("--overwrite")) {
                    overwrite = true;
                } else if (param.equalsIgnoreCase("--optimized")) {
                    optimized = true;
                } else if (param.equalsIgnoreCase("--include-optimized")) {
                    includeOptimized = true;
                }
            }

            // Reply to the command message to acquire the reply message for later usage
            var replyMessage = new MessageBuilder()
                    .setEmbed(messageFormatter.importProgressMessage("Initializing Import Process"))
                    .addComponents(ActionRow.of(Button.danger("cancel-" + incomingMessage.getId(), "Cancel")))
                    .replyTo(incomingMessage)
                    .send(incomingMessage.getChannel())
                    .join();

            // Run the import processor
            importMediaProcessor.get().processImport(replyMessage, incomingMessage.getId(), skipSync, overwrite, optimized, includeOptimized);
        } else {
            incomingMessage.reply(messageFormatter.errorMessage("You are not authorized to use this command. If you believe this is a mistake, please contact Brandan."));
        }
    }
}
