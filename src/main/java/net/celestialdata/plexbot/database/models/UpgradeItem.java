package net.celestialdata.plexbot.database.models;

import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.config.ConfigProvider;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Proxy;
import org.javacord.api.util.logging.ExceptionLogger;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Upgrades")
@Proxy(lazy = false)
public class UpgradeItem implements BaseModel, Serializable {
    @Id
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "upgrade_movie", referencedColumnName = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "upgrade_resolution", nullable = false)
    private int newResolution;

    @Column(name = "upgrade_message_id")
    private Long messageId;

    public UpgradeItem() {
    }

    public UpgradeItem(Movie movie, int newResolution, Long messageId) {
        this.movie = movie;
        this.newResolution = newResolution;
        this.messageId = messageId;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public int getNewResolution() {
        return newResolution;
    }

    public void setNewResolution(int newResolution) {
        this.newResolution = newResolution;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return "UpgradeItem [movie=" + this.movie + ", newResolution=" + this.newResolution + ", messageId=" + this.messageId + "]";
    }

    @Override
    public void onDelete() {
        Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.upgradableMoviesChannelId()).ifPresent(textChannel ->
                textChannel.getMessageById(this.messageId).join().delete().exceptionally(ExceptionLogger.get()));
    }
}