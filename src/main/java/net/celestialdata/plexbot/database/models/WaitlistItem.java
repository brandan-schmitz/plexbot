package net.celestialdata.plexbot.database.models;

import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.config.ConfigProvider;
import org.hibernate.annotations.Proxy;
import org.javacord.api.util.logging.ExceptionLogger;

import javax.persistence.*;

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
        Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.waitlistChannelId()).ifPresent(textChannel ->
                textChannel.getMessageById(this.messageId).join().delete().exceptionally(ExceptionLogger.get()));
    }
}