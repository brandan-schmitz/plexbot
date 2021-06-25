package net.celestialdata.plexbot.discord;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Produces;

@Named
@ApplicationScoped
public class InviteLinkProducer {

    @Inject
    DiscordApi discordApi;

    @Produces
    @Named("inviteLink")
    public String produceLink() {
        return discordApi.createBotInvite(new PermissionsBuilder()
                .setState(PermissionType.CHANGE_NICKNAME, PermissionState.ALLOWED)
                .setState(PermissionType.SEND_MESSAGES, PermissionState.ALLOWED)
                .setState(PermissionType.MANAGE_MESSAGES, PermissionState.ALLOWED)
                .setState(PermissionType.EMBED_LINKS, PermissionState.ALLOWED)
                .setState(PermissionType.USE_EXTERNAL_EMOJIS, PermissionState.ALLOWED)
                .setState(PermissionType.ADD_REACTIONS, PermissionState.ALLOWED)
                .build());
    }
}