package net.celestialdata.plexbot;

import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.StartupEvent;
import net.celestialdata.plexbot.db.daos.DownloadQueueItemDao;
import net.celestialdata.plexbot.db.daos.UserDao;
import net.celestialdata.plexbot.db.entities.DownloadQueueItem;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Paths;

@Singleton
public class LifecycleController {
    private final Logger logger = Logger.getLogger(LifecycleController.class);

    @ConfigProperty(name = "SickgearSettings.enabled")
    boolean sickgearEnabled;

    @ConfigProperty(name = "BotSettings.adminUsername")
    String username;

    @ConfigProperty(name = "BotSettings.adminPassword")
    String password;

    @Inject
    DownloadQueueItemDao downloadQueueItemDao;

    @Inject
    UserDao userDao;

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
                            "Please edit the created configuration file located at " + Paths.get("config/application.yaml").toAbsolutePath() +
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

        // Display a link to invite the bot to a server with
        logger.info("Invite the bot to servers with this link: " + inviteLink.get());

        // Verify that previously downloading files are changed back to the queued status so that a download can be re-attempted
        // This will only run if the SickGear integration is enabled as that is the only time this download queue is used.
        if (sickgearEnabled) {
            var queueItems = downloadQueueItemDao.listDownloading();
            for (DownloadQueueItem downloadQueueItem : queueItems) {
                downloadQueueItemDao.updateStatus(downloadQueueItem.id, "queued");
            }
        }
    }

    @Transactional
    public void updateAdminUser(@Observes StartupEvent event) {
        logger.info("Adding/updating administrator credentials");
        userDao.createOrUpdate(username, password, "admin");
    }
}