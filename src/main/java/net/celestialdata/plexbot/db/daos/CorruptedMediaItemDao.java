package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.db.entities.CorruptedMediaItem;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.awt.*;
import java.io.File;
import java.util.List;

@SuppressWarnings({"unused"})
@ApplicationScoped
public class CorruptedMediaItemDao {

    @ConfigProperty(name = "ChannelSettings.corruptedNotificationChannel")
    String corruptedNotificationChannel;

    @Inject
    DiscordApi discordApi;

    public List<CorruptedMediaItem> listALl() {
        return CorruptedMediaItem.listAll();
    }

    public CorruptedMediaItem get(int id) {
        return CorruptedMediaItem.findById(id);
    }

    public CorruptedMediaItem getByMessageId(long messageId) {
        return CorruptedMediaItem.find("messageId", messageId).firstResult();
    }

    public CorruptedMediaItem getByAbsolutePath(String absolutePath) {
        return CorruptedMediaItem.find("absolutePath", absolutePath).firstResult();
    }

    public boolean existsByAbsolutePath(String absolutePath) {
        return CorruptedMediaItem.count("absolutePath", absolutePath) == 1;
    }

    @Transactional
    public CorruptedMediaItem create(String mediaType, File mediaFile) {
        if (existsByAbsolutePath(mediaFile.getAbsolutePath())) {
            return getByAbsolutePath(mediaFile.getAbsolutePath());
        } else {
            CorruptedMediaItem entity = new CorruptedMediaItem();

            var corruptedMessage = new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Corrupted Media File Detected:")
                            .setDescription("While analyzing the following file, it was determined that the file is corrupted.  " +
                                    "This file should be replaced with a non-corrupted version.")
                            .addInlineField("Media Type:", "```" + mediaType + "```")
                            .addField("Media Filename:", "```" + mediaFile.getName() + "```")
                            .setColor(Color.RED))
                    .addComponents(ActionRow.of(
                            Button.create("recheck-corrupted-file", ButtonStyle.DANGER, "Check Again")
                    ))
                    .send(discordApi.getTextChannelById(corruptedNotificationChannel).orElseThrow())
                    .exceptionally(ExceptionLogger.get()).join();

            entity.type = mediaType;
            entity.absolutePath = mediaFile.getAbsolutePath();
            entity.messageId = corruptedMessage.getId();

            entity.persist();

            return entity;
        }
    }

    @Transactional
    public void delete(int id) {
        CorruptedMediaItem entity = CorruptedMediaItem.findById(id);

        discordApi.getTextChannelById(corruptedNotificationChannel).ifPresent(textChannel ->
                textChannel.getMessageById(entity.messageId).join().delete());

        entity.delete();
    }

    @Transactional
    public void deleteByMessageId(long messageId) {
        CorruptedMediaItem entity = CorruptedMediaItem.find("messageId", messageId).firstResult();

        discordApi.getTextChannelById(corruptedNotificationChannel).ifPresent(textChannel ->
                textChannel.getMessageById(entity.messageId).join().delete());

        entity.delete();
    }

    @Transactional
    public void deleteByAbsolutePath(String absolutePath) {
        CorruptedMediaItem entity = CorruptedMediaItem.find("absolutePath", absolutePath).firstResult();

        discordApi.getTextChannelById(corruptedNotificationChannel).ifPresent(textChannel ->
                textChannel.getMessageById(entity.messageId).join().delete());

        entity.delete();
    }
}