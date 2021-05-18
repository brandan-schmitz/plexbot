package net.celestialdata.plexbot.discord;

import io.quarkus.runtime.StartupEvent;
import me.koply.kcommando.KCommando;
import me.koply.kcommando.integration.impl.javacord.JavacordIntegration;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.DiscordApi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class CommandHandler {

    @Inject
    DiscordApi discordApi;

    @ConfigProperty(name = "BotSettings.prefix")
    String prefix;

    @ConfigProperty(name = "BotSettings.ownerID")
    String ownerID;

    void register(@Observes StartupEvent event) {
        new KCommando<>(new JavacordIntegration(discordApi))
                .setPackage("net.celestialdata.plexbot.discord.commands")
                .setReadBotMessages(true)
                .setOwners(ownerID)
                .setPrefix(prefix)
                .build();
    }
}
