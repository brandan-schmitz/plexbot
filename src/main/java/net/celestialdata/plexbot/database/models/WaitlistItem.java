package net.celestialdata.plexbot.database.models;

import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.client.ApiException;
import net.celestialdata.plexbot.client.BotClient;
import net.celestialdata.plexbot.client.model.OmdbItem;
import net.celestialdata.plexbot.configuration.BotConfig;
import net.celestialdata.plexbot.utils.BotColors;
import org.hibernate.annotations.Proxy;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.concurrent.CompletionException;

@SuppressWarnings("unused")
@Entity
@Table(name = "Waitinglist")
@Proxy(lazy = false)
public class WaitlistItem implements BaseModel {
    @Id
    @Column(name = "item_id")
    private String id;

    @Column(name = "item_title", nullable = false)
    private String title;

    @Column(name = "item_year", nullable = false)
    private String year;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "item_requested_by", referencedColumnName = "user_id", nullable = false)
    private User requestedBy;

    @Column(name = "item_message_id", nullable = false)
    private Long messageId;

    public WaitlistItem() {
    }

    public WaitlistItem(String id, String title, String year, User requestedBy, Long messageId) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.requestedBy = requestedBy;
        this.messageId = messageId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public User getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return "WaitlistItem [id=" + this.id + ", title=" + this.title + ", year=" + this.year +
                ", requestedBy=" + this.requestedBy + ", messageId=" + this.messageId + "]";
    }

    @Override
    public void onDelete() {
        Main.getBotApi().getTextChannelById(BotConfig.getInstance().waitlistChannelId()).ifPresent(textChannel ->
                textChannel.getMessageById(this.messageId).join().delete().exceptionally(ExceptionLogger.get()));
    }

    @PrePersist
    protected void onCreate() {
        OmdbItem movieInfo;
        try {
            movieInfo = BotClient.getInstance().omdbApi.getById(this.id);
            Main.getBotApi().getTextChannelById(BotConfig.getInstance().waitlistChannelId()).ifPresent(textChannel ->
                    textChannel.sendMessage(new EmbedBuilder()
                            .setTitle(this.title)
                            .setDescription("**IMDB Code:** " + movieInfo.getImdbID() + "\n" +
                                    "**Year:** " + this.year + "\n" +
                                    "**Director(s):** " + movieInfo.getDirector() + "\n" +
                                    "**Plot:** " + movieInfo.getPlot())
                            .setImage(movieInfo.getPoster())
                            .setColor(BotColors.INFO)
                            .setFooter("Last Checked: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                                    .format(ZonedDateTime.now()) + " CST"))
                            .thenAccept(sentMessage -> this.messageId = sentMessage.getId())
                            .join()
            );
        } catch (ApiException | CompletionException ignored) {
        }
    }
}