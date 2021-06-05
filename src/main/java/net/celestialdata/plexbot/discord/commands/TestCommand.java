package net.celestialdata.plexbot.discord.commands;

import io.quarkus.arc.log.LoggerName;
import io.smallrye.common.annotation.Blocking;
import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.javacord.api.entity.message.Message;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@SuppressWarnings("unused")
@ApplicationScoped
public class TestCommand implements Command<Message> {

    @LoggerName("net.celestialdata.plexbot.discord.commands.TestCommand")
    Logger logger;

    @Inject
    FileUtilities fileUtilities;

    @Override
    @Blocking
    public void execute(Message incomingMessage, String prefix, String usedAlias, String parameterString) {
        var args = parameterString.split("\\s+");

        fileUtilities.downloadFile(args[0], args[1]).subscribe().with(
                item -> logger.info("Download Progress: " + item + "%"),
                failure -> logger.error(failure),
                () -> logger.info("Download Finished")
        );

        logger.info("End of method");
    }
}