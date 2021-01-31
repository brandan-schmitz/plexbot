package net.celestialdata.plexbot;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import net.celestialdata.plexbot.client.BotClient;
import net.celestialdata.plexbot.database.BotUser;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.entity.server.Server;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class LifecycleController {
    private static final Logger LOGGER = Logger.getLogger("Plexbot Logger");

    @Inject
    BotApi botApi;

    @Inject
    BotClient botClient;

    @Inject
    EntityManager entityManager;

    @ConfigProperty(name = "quarkus.log.level")
    String logLevel;

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        // Connect to the discord API and display a join URL
        LOGGER.info("Connecting to the Discord API");
        String inviteUrl = botApi.getInviteUrl();

        // Register server join listener
        LOGGER.info("Registering Discord Event Listeners");
        botApi.api.addListener((ServerMemberJoinListener) event ->
                entityManager.persist(new BotUser(
                        event.getUser().getId(),
                        event.getUser().getDiscriminatedName()
                ))
        );

        // Update the database with to add or remove users that may have joined or left while the bot was offline
        LOGGER.info("Scanning Discord servers for new users");
        int numAdded = 0;
        int numRemoved = 0;
        List<BotUser> dbUsers = entityManager.createNamedQuery("BotUsers.findAll", BotUser.class).getResultList();
        for (Server s : botApi.api.getServers()) {
            for (org.javacord.api.entity.user.User u : s.getMembers()) {
                if (!u.isBot()) {
                    if (dbUsers.stream().noneMatch(o -> o.discordId == u.getId())) {
                        entityManager.persist(new BotUser(u.getId(), u.getDiscriminatedName()));
                        numAdded++;
                    } else {
                        dbUsers.removeIf(o -> o.discordId == u.getId());
                    }
                }
            }
        }
        for (BotUser u : dbUsers) {
            entityManager.remove(u);
            numRemoved++;
        }
        if (numAdded > 0 || numRemoved > 0) {
            LOGGER.info("Added " + numAdded + " users and removed " + numRemoved + " users");
        }

        // Configure the BotClient and ensure things are logged in. Then trigger a refresh of Plex
        // libraries in order to ensure that the Plex login is working and that the Plex media library
        // is up to date with the filesystem.
        LOGGER.info("Logging in to external API's and configuring API clients");
        botClient.refreshClient();
        try {
            botClient.plexApi.refreshLibraries();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (logLevel.equalsIgnoreCase("INFO")) {
            LOGGER.info("Bot Initialization complete. Please share the following link to add it to servers: " + inviteUrl);
        } else
            System.out.println("\nBot Initialization complete. Please share the following link to add it to servers: " + inviteUrl + "\n");
    }

    void onStop(@Observes ShutdownEvent ev) {
        LOGGER.info("The application is stopping...");

        // Disconnect cleanly from the Discord API
        botApi.api.disconnect();
    }
}
