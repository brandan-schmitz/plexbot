package net.celestialdata.plexbot.utilities;

import io.quarkus.runtime.ShutdownEvent;
import org.javacord.api.DiscordApi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class ShutdownController {

    @Inject
    DiscordApi discordApi;

    void stop(@Observes ShutdownEvent event) {
        discordApi.disconnect();
    }
}
