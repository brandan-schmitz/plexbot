package net.celestialdata.plexbot.utilities;

import io.quarkus.arc.log.LoggerName;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.javacord.api.DiscordApi;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

@ApplicationScoped
public class LifecycleController {

    @LoggerName("net.celestialdata.plexbot.utilities.LifecycleController")
    Logger logger;

    @Inject
    DiscordApi discordApi;

    @Inject
    @Named("inviteLink")
    Instance<String> inviteLink;

    void start(@Observes StartupEvent event) {
        logger.info("Invite the bot to servers with this link: " + inviteLink.get());
    }

    void stop(@Observes ShutdownEvent event) {
        discordApi.disconnect();
    }
}