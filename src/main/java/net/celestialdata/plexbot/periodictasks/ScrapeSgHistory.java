package net.celestialdata.plexbot.periodictasks;

import io.quarkus.scheduler.Scheduled;
import net.celestialdata.plexbot.clients.models.sg.objects.SgHistoryItem;
import net.celestialdata.plexbot.clients.utilities.SgServiceWrapper;
import net.celestialdata.plexbot.db.daos.DownloadHistoryItemDao;
import net.celestialdata.plexbot.db.daos.DownloadQueueItemDao;
import net.celestialdata.plexbot.db.entities.DownloadHistoryItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.awt.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ScrapeSgHistory {
    private final Logger logger = Logger.getLogger(ScrapeSgHistory.class);

    @ConfigProperty(name = "BotSettings.ownerID")
    long botOwner;

    @ConfigProperty(name = "SickgearSettings.enabled")
    boolean enabled;
    
    @ConfigProperty(name = "SickgearSettings.torrentFolder")
    String torrentFolder;

    @Inject
    SgServiceWrapper sgServiceWrapper;

    @Inject
    DownloadQueueItemDao downloadQueueItemDao;

    @Inject
    DownloadHistoryItemDao downloadHistoryItemDao;

    @Inject
    DiscordApi discordApi;

    @Scheduled(every = "1m", delay = 10, delayUnit = TimeUnit.SECONDS)
    public void runScrape() {
        // Skip this task if the Sickgear integration is disabled
        if (!enabled) {
            return;
        }

        // Fetch the history
        var historyResponse = sgServiceWrapper.fetchHistory();

        // Ensure that the request was successful
        if (!historyResponse.result.equalsIgnoreCase("success")) {
            logger.error("Unable to scrap SickGear history. Please check your connection settings: " + historyResponse.message);
            return;
        }

        // Cycle through each of the history items and add them to the download queue
        for (SgHistoryItem item : historyResponse.results) {
            try {
                // Check if the DB already contains this specific item or if it was downloaded already
                if (downloadQueueItemDao.existsByResource(item.resource) || downloadHistoryItemDao.existsByResource(item.resource)
                        && downloadHistoryItemDao.getByResource(item.resource).status.equals("downloaded")) {
                    continue;
                }

                // Create a local copy of the resource field for modification
                var resource = item.resource;

                // Remove the .. that occasionally appears at the end of some resource strings
                if (resource.endsWith("..")) {
                    resource = resource.substring(0, (resource.length() - 2));
                }

                // Create the file filter to locate this file based on the resource string
                var filter = new WildcardFileFilter(resource + "*");

                // Search the Sickgear torrent folder for the file
                var files = FileUtils.listFiles(new File(torrentFolder), filter, TrueFileFilter.INSTANCE);

                // Ensure that the file was found
                if (files.isEmpty()) {
                    logger.warn("The resource file for " + item.showName + " s" + item.season + "e" + item.episode + " was not located!!");
                    discordApi.getUserById(botOwner).join().sendMessage(new EmbedBuilder()
                            .setTitle("Missing Resource File")
                            .setDescription("I was unable to located the specified resource file while attempting to scrape the following episode history from SickGear.")
                            .addField("Show:", "```" + item.showName + "```")
                            .addInlineField("Season:", "```" + item.season + "```")
                            .addInlineField("Episode:", "```" + item.episode + "```")
                            .addField("Resource:", "```" + item.resource + "```")
                            .setColor(Color.YELLOW)
                    ).join();
                    continue;
                }

                // Fetch the first file in the collection, this should be the only one and the file we want
                var file = files.iterator().next();

                // Determine the type of file
                var filetype = "torrent";
                if (file.getName().endsWith(".torrent")) {
                    filetype = "torrent";
                } else if (file.getName().endsWith(".magnet")) {
                    filetype = "magnet";
                }

                // Add the file to the download queue
                downloadQueueItemDao.create(item, file.getName(), filetype);
            } catch (Throwable e) {
                logger.error(e);
                discordApi.getUserById(botOwner).join().sendMessage(new EmbedBuilder()
                        .setTitle("Error queuing download")
                        .setDescription("An error was encountered while trying to queue the following episode to be downloaded.")
                        .addField("Show:", "```" + item.showName + "```")
                        .addInlineField("Season:", "```" + item.season + "```")
                        .addInlineField("Episode:", "```" + item.episode + "```")
                        .addField("Resource:", "```" + item.resource + "```")
                        .addField("Error:", "```" + e.getCause() + "```")
                        .setColor(Color.RED)
                ).join();
            }
        }
    }
}