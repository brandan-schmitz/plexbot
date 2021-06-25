package net.celestialdata.plexbot.discord;

import io.quarkus.arc.log.LoggerName;
import io.quarkus.runtime.Quarkus;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class JavacordProducer {

    @LoggerName("net.celestialdata.plexbot.discord.JavacordProducer")
    Logger logger;

    @ConfigProperty(name = "BotSettings.token")
    String botToken;

    @Produces
    @ApplicationScoped
    DiscordApi produceDiscordApi() {
        return new DiscordApiBuilder()
                .setToken(botToken)
                .setWaitForServersOnStartup(true)
                .setWaitForUsersOnStartup(true)
                .setAllIntentsExcept(
                        Intent.GUILD_WEBHOOKS,
                        Intent.GUILD_BANS,
                        Intent.GUILD_INVITES,
                        Intent.GUILD_VOICE_STATES,
                        Intent.GUILD_PRESENCES,
                        Intent.GUILD_MESSAGE_TYPING,
                        Intent.DIRECT_MESSAGE_TYPING
                )
                .login()
                .whenComplete((discordApi, throwable) -> {
                    if (throwable != null) {
                        logger.fatal("Exception while logging in to Discord", throwable);
                        Quarkus.asyncExit(1);
                    }
                })
                .join();
    }

    void disposeDiscordApi(@Disposes DiscordApi discordApi) {
        discordApi.disconnect();
    }
}