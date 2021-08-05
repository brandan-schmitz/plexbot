package net.celestialdata.plexbot.discord.commands;

import io.quarkus.arc.log.LoggerName;
import net.celestialdata.plexbot.clients.services.TmdbService;
import net.celestialdata.plexbot.clients.services.TvdbService;
import net.celestialdata.plexbot.db.daos.*;
import net.celestialdata.plexbot.db.entities.*;
import net.celestialdata.plexbot.discord.MessageFormatter;
import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.enumerators.FileType;
import net.celestialdata.plexbot.utilities.BotProcess;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.util.event.ListenerManager;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@ApplicationScoped
public class MigrateCommand extends BotProcess implements Command<Message> {
    private final DecimalFormat decimalFormatter = new DecimalFormat("#0.00");
    private Message replyMessage;
    private ListenerManager<ButtonClickListener> cancelListener;
    private LocalDateTime lastUpdated = LocalDateTime.now().minus(5, ChronoUnit.SECONDS);
    private int total = 0;
    private int progress = 1;
    private boolean canceled = false;

    @LoggerName("net.celestialdata.plexbot.discord.commands.MigrateCommand")
    Logger logger;

    @ConfigProperty(name = "FolderSettings.tvFolder")
    String tvFolder;

    @Inject
    @RestClient
    TvdbService tvdbService;

    @Inject
    @RestClient
    TmdbService tmdbService;

    @Inject
    DiscordApi discordApi;

    @Inject
    MessageFormatter messageFormatter;

    @Inject
    FileUtilities fileUtilities;

    @Inject
    ShowDao showDao;

    @Inject
    EpisodeDao episodeDao;

    @Override
    public void handleFailure(Throwable error) {
        logger.error(error);
    }

    @Transactional
    public List<EpisodeOld> listAllOldEpisodes() {
        return EpisodeOld.listAll();
    }

    @Transactional
    public List<ShowOld> listOldShows() {
        return ShowOld.listAll();
    }

    public void updateProgress() {
        var percentage = (((double) (progress == 1 ? 0 : progress) / total) * 100);

        // Update the bot status process string
        updateProcessString("Migration Processor - " + decimalFormatter.format(percentage) + "%");

        if (lastUpdated.plus(3, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
            replyMessage.edit(new EmbedBuilder()
                    .setTitle("Migration Progress")
                    .setDescription("You requested that a data migration occur. Below is the progress of that migration.\n" +
                            "```Migrating " + progress + " of " + total + " items```")
                    .setColor(Color.BLUE)
                    .setFooter("Progress updated: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(ZonedDateTime.now()) + " CST")
            ).join();
            lastUpdated = LocalDateTime.now();
        }
    }

    public void sendErrorMessage(String failedItem, Exception exception) {
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Migration Error")
                        .setDescription("An unknown error occurred while migrating the following item:\n```" + failedItem + "```")
                        .addField("Error Message:", "```" + ExceptionUtils.getMessage(exception) + "```")
                        .setColor(Color.RED))
                .send(replyMessage.getChannel())
                .join();
    }

    public void sendErrorMessage(String failedItem, String message) {
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Migration Error")
                        .setDescription("An unknown error occurred while migrating the following item:\n```" + failedItem + "```")
                        .addField("Error Message:", "```" + message + "```")
                        .setColor(Color.RED))
                .send(replyMessage.getChannel())
                .join();
    }

    public void onCancel() {
        replyMessage.edit(new EmbedBuilder()
                .setTitle("Migration Canceled")
                .setDescription("The migration has been canceled. You can resume the migration by running the migrate command again.")
                .setColor(Color.BLACK)
        );
        cancelListener.remove();
        endProcess();
    }

    private void resetToNewMessage(EmbedBuilder embedBuilder, long messageToReplyTo) {
        var channel = replyMessage.getChannel();
        discordApi.getMessageById(replyMessage.getId(), channel).join().delete().join();
        new MessageBuilder()
                .setEmbed(embedBuilder)
                .replyTo(messageToReplyTo)
                .send(channel)
                .join();
    }

    public void cleanDirectory(File directory) throws IOException {
        // Fetch a list of files and directories remaining
        var filesList = new ArrayList<File>();
        Files.list(directory.toPath())
                .forEach(path -> filesList.add(path.toFile()));

        // Ensure that files the directory is cleaned out
        for (File file : filesList) {
            if (file.isDirectory() && FileUtils.isEmptyDirectory(file)) {
                FileUtils.deleteDirectory(file);
            } else if (file.isHidden()) {
                FileUtils.delete(file);
            }
        }
    }

    @Override
    public void execute(Message incomingMessage, String prefix, String usedAlias, String parameterString) {
        configureProcess("Migration Processor - initializing", replyMessage);

        replyMessage = new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Migration Started")
                        .setDescription("You requested that the migration process be started.")
                        .setColor(Color.BLUE))
                .addComponents(ActionRow.of(Button.danger("cancel-" + incomingMessage.getId(), "Cancel")))
                .replyTo(incomingMessage)
                .send(incomingMessage.getChannel())
                .join();

        cancelListener = replyMessage.getChannel().addButtonClickListener(clickEvent -> {
            if (clickEvent.getButtonInteraction().getCustomId().equals("cancel-" + incomingMessage.getId())) {
                clickEvent.getInteraction().asMessageComponentInteraction().orElseThrow()
                        .createOriginalMessageUpdater()
                        .removeAllComponents()
                        .removeAllEmbeds()
                        .addEmbed(new EmbedBuilder()
                                .setTitle("Stopping Migration")
                                .setDescription("The bot will finish migrating the current item then stop the migration " +
                                        "process. This will ensure that no media files are corrupted by an improper stopping " +
                                        "of the file transfer that is occurring during the migration process.")
                                .setColor(Color.YELLOW))
                        .update();
                canceled = true;
            }
        });

        // Gather media lists
        List<EpisodeOld> oldEpisodes = listAllOldEpisodes();
        List<ShowOld> oldShows = listOldShows();

        // Count total number of items that need to be migrated
        progress = 1;
        total = oldEpisodes.size() + oldShows.size();

        // Update the progress to show it has started
        updateProgress();

        // Process episodes
        for (EpisodeOld episode : oldEpisodes) {
            try {
                migrateOldEpisode(episode);
            } catch (Exception e) {
                logger.error("Unable to process episode " + episode.filename, e);
                sendErrorMessage("Episode " + episode.filename, e);
                progress++;
            }

            // Stop if the process was canceled
            if (canceled) {
                onCancel();
                endProcess();
                return;
            }

            // Update the progress
            updateProgress();
        }

        // Stop if the process was canceled
        if (canceled) {
            onCancel();
            endProcess();
            return;
        }

        // Process shows
        for (ShowOld show : oldShows) {
            try {
                migrateOldShow(show);
            } catch (Exception e) {
                logger.error("Unable to process show " + show.foldername, e);
                sendErrorMessage("Show " + show.foldername, e);
                progress++;
            }

            // Stop if the process was canceled
            if (canceled) {
                onCancel();
                endProcess();
                return;
            }

            // Update the progress
            updateProgress();
        }

        // Update the message to remove the cancel button, listener and display a message showing the migration is finished
        resetToNewMessage(new EmbedBuilder()
                .setTitle("Migration Completed")
                .setDescription("The migration you requested has been completed. Please verify that no error messages were displayed during this process. " +
                        "Any files that caused an error will need to be manually migrated.")
                .setColor(Color.GREEN), incomingMessage.getId());
        cancelListener.remove();
        endProcess();
    }

    public void migrateOldEpisode(EpisodeOld oldEpisode) {
        // Find episode on tvdb
        var episodeResponse = tvdbService.getEpisode(oldEpisode.id);

        // Verify episode was located
        if (!episodeResponse.status.equalsIgnoreCase("success")) {
            logger.warn("Unable to find matching episode for " + oldEpisode.filename);
            sendErrorMessage("Episode " + oldEpisode.filename, "Unable to find matching episode.");
            progress++;
            return;
        }

        // Fetch information about the episodes show
        var showResponse = tvdbService.getSeries(episodeResponse.episode.seriesId);

        // Verify information was fetched
        if (!showResponse.status.equalsIgnoreCase("success")) {
            logger.warn("Unable to load matching show for " + oldEpisode.filename);
            sendErrorMessage("Episode " + oldEpisode.filename, "Unable to load matching show.");
            progress++;
            return;
        }

        // Create the new show folder if not exists
        if (!fileUtilities.createFolder(showResponse.series)) {
            logger.warn("Failed to create show folder for episode " + oldEpisode.filename);
            sendErrorMessage("Episode " + oldEpisode.filename, "Failed to create show folder.");
            progress++;
            return;
        }

        // Ensure the show is in the database
        var show = showDao.create(showResponse.series.id, showResponse.series.name, fileUtilities.generatePathname(showResponse.series));

        // Create the season folder if not exists
        if (!fileUtilities.createFolder(tvFolder + show.foldername + "/Season " + episodeResponse.episode.seasonNumber)) {
            logger.warn("Failed to create season folder: " + tvFolder + show.foldername + "/Season " + episodeResponse.episode.seasonNumber);
            sendErrorMessage("Episode " + oldEpisode.filename, "Failed to create season folder.");
            progress++;
            return;
        }

        // Move the episode into place
        var filename = fileUtilities.generateEpisodeFilename(episodeResponse.episode, show, FileType.determineFiletype(oldEpisode.filename));
        var sourceFile = tvFolder + oldEpisode.show.foldername + "/Season " + oldEpisode.season + "/" + oldEpisode.filename;
        var destinationFile = tvFolder + show.foldername + "/Season " + episodeResponse.episode.seasonNumber + "/" + filename;
        if (!destinationFile.equals(sourceFile)) {
            if (!fileUtilities.moveMedia(sourceFile, destinationFile, true)) {
                logger.warn("Failed to move episode " + oldEpisode.filename);
                sendErrorMessage("Episode " + oldEpisode.filename, "Failed to move episode file.");
                progress++;
                return;
            }
        }

        // Add the episode to the database
        episodeDao.createOrUpdate(episodeResponse.episode, filename, show.id);

        // Delete the old episode from the database
        deleteOldEpisode(oldEpisode.id);

        // Increment the progress counter
        progress++;
    }

    void migrateOldShow(ShowOld oldShow) throws IOException {
        // Attempt to delete the show folder, this should fail if there is anything aside remaining
        var directory = new File(tvFolder + oldShow.foldername);
        cleanDirectory(directory);

        // Attempt to remove the show folder
        if (FileUtils.isEmptyDirectory(directory)) {
            FileUtils.deleteDirectory(directory);
        } else {
            logger.warn("Unable to delete show " + oldShow.foldername + " as it is not empty.");
            sendErrorMessage("Show " + oldShow.foldername, "Unable to delete non-empty folder.");
            progress++;
            return;
        }

        // Delete the database entry
        deleteOldShow(oldShow.id);

        // Increment the progress counter
        progress++;
    }

    @Transactional
    public void deleteOldEpisode(String id) {
        EpisodeOld entity = EpisodeOld.findById(id);
        entity.delete();
    }

    @Transactional
    public void deleteOldShow(String id) {
        ShowOld entity = ShowOld.findById(id);
        entity.delete();
    }
}