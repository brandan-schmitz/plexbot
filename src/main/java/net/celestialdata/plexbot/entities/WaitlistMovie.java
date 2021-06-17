package net.celestialdata.plexbot.entities;

import io.quarkus.arc.Arc;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.eclipse.microprofile.config.ConfigProvider;
import org.javacord.api.DiscordApi;
import org.javacord.api.util.logging.ExceptionLogger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Waitlist_Movies")
public class WaitlistMovie extends PanacheEntityBase {

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Id
    @Column(name = "movie_id", nullable = false)
    public String id;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "movie_title", nullable = false)
    public String title;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "movie_year", nullable = false)
    public String year;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "movie_requested_by", nullable = false)
    public Long requestedBy;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "movie_message_id", nullable = false)
    public Long messageId;

    @Override
    public void delete() {
        var waitlistChannel = ConfigProvider.getConfig().getValue("ChannelSettings.waitlistChannelId", String.class);
        DiscordApi discordApi = Arc.container().instance(DiscordApi.class).get();
        discordApi.getTextChannelById(waitlistChannel)
                .ifPresent(textChannel ->
                        textChannel.getMessageById(this.messageId)
                                .exceptionally(ExceptionLogger.get()).join()
                                .delete().exceptionally(ExceptionLogger.get())
                );

        super.delete();
    }
}