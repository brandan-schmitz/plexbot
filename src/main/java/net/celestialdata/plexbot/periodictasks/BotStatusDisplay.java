package net.celestialdata.plexbot.periodictasks;

import io.quarkus.arc.log.LoggerName;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import net.celestialdata.plexbot.db.daos.EncodingWorkItemDao;
import net.celestialdata.plexbot.db.daos.EpisodeDao;
import net.celestialdata.plexbot.db.daos.MovieDao;
import net.celestialdata.plexbot.db.entities.EncodingWorkItem;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.awt.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class BotStatusDisplay {
    final private HashMap<String, String> currentProcesses = new HashMap<>();
    Message statusMessage;

    @LoggerName("net.celestialdata.plexbot.periodictasks.BotStatusDisplay")
    Logger logger;

    @ConfigProperty(name = "ChannelSettings.botStatusChannel")
    Long statusChannelId;

    @Inject
    DiscordApi discordApi;

    @Inject
    @Named("botVersion")
    Instance<String> botVersion;

    @Inject
    EncodingWorkItemDao encodingWorkItemDao;

    @Inject
    MovieDao movieDao;

    @Inject
    EpisodeDao episodeDao;

    void initialize(@Observes StartupEvent startupEvent) {
        // Clear the past 100 messages in the channel. If the channel does not exist, throw an error then quit the application.
        discordApi.getTextChannelById(statusChannelId).ifPresentOrElse(channel -> channel.getMessages(100)
                        .thenAccept(messages -> messages.deleteAll().join()).exceptionally(ExceptionLogger.get()),
                () -> {
                    logger.fatal("Invalid channel ID for the bot status channel. You must specify a valid channel ID for the bot to run.");
                    Quarkus.asyncExit(1);
                });

        // Send the updated status message then keep track of it
        statusMessage = new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Bot Status")
                        .setDescription("The bot is currently starting up. This message will be updated once the bot is online.")
                        .setColor(Color.YELLOW)
                ).send(discordApi.getTextChannelById(statusChannelId).orElseThrow()).exceptionally(ExceptionLogger.get())
                .join();
    }

    public void stopManager() {
        statusMessage.edit(new EmbedBuilder()
                .setTitle("Bot Status")
                .setDescription("The bot has been shut down. Please check back later once the bot is back online.")
                .setColor(Color.BLACK)
        ).exceptionally(ExceptionLogger.get());
    }

    private String generateSeasonString(int seasonNumber) {
        if (seasonNumber <= 9) {
            return "s0" + seasonNumber;
        } else return "s" + seasonNumber;
    }

    private String generateEpisodeString(int episodeNumber) {
        if (episodeNumber <= 9) {
            return "e0" + episodeNumber;
        } else return "e" + episodeNumber;
    }

    @Scheduled(every = "3s", delay = 10, delayUnit = TimeUnit.SECONDS)
    void updateStatus() {
        var statusBuilder = new StringBuilder();
        var encodingStatusBuilder = new StringBuilder();
        var counter = 1;

        // Create the string used within the embed that displays running commands and tasks
        if (currentProcesses.isEmpty()) {
            statusBuilder.append("Idle");
        } else {
            for (String processName : currentProcesses.values()) {
                statusBuilder.append(counter).append(") ").append(processName).append("\n");
                counter++;
            }
        }

        // Create the string usd within the embed that displays running media optimizations
        List<EncodingWorkItem> workItems = encodingWorkItemDao.listALl();
        if (workItems.isEmpty()) {
            encodingStatusBuilder.append("Idle");
        } else {
            // Reset the counter back to 1
            counter = 1;

            // Cycle through the work items and build the status string
            for (EncodingWorkItem workItem : workItems) {
                var itemTitle = "";

                // Create the title for this item
                if (workItem.mediaType.equals("episode")) {
                    var episode = episodeDao.getByTvdbId(workItem.mediaId);
                    itemTitle = episode.show.name + " " + generateSeasonString(episode.season) + generateEpisodeString(episode.number);
                } else if (workItem.mediaType.equals("movie")) {
                    var movie = movieDao.getByTmdbId(workItem.mediaId);
                    itemTitle = movie.title + " (" + movie.year + ")";
                }

                // Build the string
                encodingStatusBuilder.append(counter).append(") ").append(itemTitle).append(" - ")
                        .append(workItem.progress).append("\n");

                // Increment the counter
                counter++;
            }
        }

        // Update the status message
        statusMessage.edit(new EmbedBuilder()
                .setTitle("Bot Status")
                .setDescription("The processes that are currently being run by the bot are displayed below.")
                .addField("Tasks:", "```" + statusBuilder + "```")
                .addField("Optimizations:", "```" + encodingStatusBuilder + "```")
                .setFooter("Plexbot v" + botVersion.get() + " - " + DateTimeFormatter.ofLocalizedDateTime(
                        FormatStyle.MEDIUM).format(ZonedDateTime.now()) + " CST"
                )
                .setColor(Color.GREEN)
        ).exceptionally(ExceptionLogger.get());
    }

    /**
     * Submit a process to the status manager for tracking. The status manager will generate a UUID
     * for the process and return that to the process for use in removing or updating the process.
     *
     * @param processString process name/progress if applicable
     * @return process uuid
     */
    public String submitProcess(String processString) {
        var processId = UUID.randomUUID();

        for (Map.Entry<String, String> entry : currentProcesses.entrySet()) {
            while (entry.getKey().equals(processId.toString())) {
                processId = UUID.randomUUID();
            }
        }

        currentProcesses.put(processId.toString(), processString);
        return processId.toString();
    }

    /**
     * Update an existing process in the status manager. This will update the string displayed and can
     * be used to update the current status of a process if it is tracking its progress.
     *
     * @param processId            uuid of the process
     * @param updatedProcessString updated process name/progress if applicable
     */
    public void updateProcess(String processId, String updatedProcessString) {
        currentProcesses.put(processId, updatedProcessString);
    }

    /**
     * Remove an existing process in the status manager.
     *
     * @param processId uuid of the process
     */
    public void removeProcess(String processId) {
        currentProcesses.remove(processId);
    }
}