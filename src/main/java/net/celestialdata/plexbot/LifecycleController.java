package net.celestialdata.plexbot;

import io.quarkus.arc.log.LoggerName;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import net.celestialdata.plexbot.periodictasks.BotStatusDisplay;
import org.eclipse.microprofile.config.ConfigProvider;
import org.javacord.api.DiscordApi;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.resource.spi.ConfigProperty;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@ApplicationScoped
public class LifecycleController {

    @LoggerName("net.celestialdata.plexbot.LifecycleController")
    Logger logger;

    @Inject
    DiscordApi discordApi;

    @Inject
    BotStatusDisplay botStatusDisplay;

    @Inject
    @Named("inviteLink")
    Instance<String> inviteLink;

    void start(@Observes StartupEvent event) {
        if (!LaunchMode.current().isDevOrTest() && Files.notExists(Paths.get("config/application.yaml").toAbsolutePath())) {
            var configSample = getClass().getClassLoader().getResourceAsStream("config.sample");
            if (configSample != null) {
                try {
                    if (Files.notExists(Paths.get("config").toAbsolutePath()) || !Files.isDirectory(Paths.get("config").toAbsolutePath())) {
                        Files.createDirectory(Paths.get("config").toAbsolutePath());
                    }

                    Files.copy(configSample, Paths.get("config/application.yaml").toAbsolutePath());
                    logger.fatal("The bot configuration file does not exist, hover a sample configuration file has been provided for you. " +
                            "Please edit the created configuration file located at " + Paths.get("config/application.yaml").toAbsolutePath().toString() +
                            " to use your own settings as the default settings in this sample file WILL NOT WORK.");
                    Quarkus.asyncExit(1);
                } catch (Exception e) {
                    logger.fatal("The bot configuration file does not exist and the bot was unable to create a sample file on your filesystem. " +
                            "Please check the plexbot code repo for a sample configuration file then place it within a directory called \"config\" " +
                            "with the name \"application.yaml\" in the base folder where you installed the application.");
                    Quarkus.asyncExit(1);
                }
            } else {
                logger.fatal("The bot configuration file does not exist and the bot was unable to locate the sample file within the application. " +
                        "Please check the plexbot code repo for a sample configuration file then place it within a directory called \"config\" " +
                        "with the name \"application.yaml\" in the base folder where you installed the application.");
                Quarkus.asyncExit(1);
            }
        }

        // Display an link to invite the bot to a server with
        logger.info("Invite the bot to servers with this link: " + inviteLink.get());
    }

    void stop(@Observes ShutdownEvent event) {
        botStatusDisplay.stopManager();
        discordApi.disconnect();
    }
}