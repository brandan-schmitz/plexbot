package net.celestialdata.plexbot.discord.commands;

import io.quarkus.arc.log.LoggerName;
import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.processors.RequestProcessor;
import org.javacord.api.entity.message.Message;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@ApplicationScoped
public class RequestMovieCommand implements Command<Message> {

    @LoggerName("net.celestialdata.plexbot.discord.commands.RequestMovieCommand")
    Logger logger;

    @Inject
    Instance<RequestProcessor> requestProcessorInstance;

    @Override
    public void handleFailure(Throwable error) {
        logger.error(error);
    }

    @Override
    public List<String> getAliases() {
        List<String> alias = new ArrayList<>();
        alias.add("rm");
        return alias;
    }

    @Override
    public void execute(Message incomingMessage, String prefix, String usedAlias, String parameterString) {
        requestProcessorInstance.get().runMovieRequest(incomingMessage, parameterString);
    }
}