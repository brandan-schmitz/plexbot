package net.celestialdata.plexbot.discord;

import io.quarkus.arc.log.LoggerName;
import io.quarkus.runtime.Quarkus;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
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
        DiscordApi api = new DiscordApiBuilder()
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

        // Display the invite link
        var invite = api.createBotInvite(new PermissionsBuilder()
                .setState(PermissionType.CHANGE_NICKNAME, PermissionState.ALLOWED)
                .setState(PermissionType.SEND_MESSAGES, PermissionState.ALLOWED)
                .setState(PermissionType.MANAGE_MESSAGES, PermissionState.ALLOWED)
                .setState(PermissionType.EMBED_LINKS, PermissionState.ALLOWED)
                .setState(PermissionType.USE_EXTERNAL_EMOJIS, PermissionState.ALLOWED)
                .setState(PermissionType.ADD_REACTIONS, PermissionState.ALLOWED)
                .build());
        logger.info("Invite the bot to servers with this link: " + invite);

        // Return the API to CDI
        return api;
    }

    void disposeDiscordApi(@Disposes DiscordApi discordApi) {
        discordApi.disconnect();
    }
}