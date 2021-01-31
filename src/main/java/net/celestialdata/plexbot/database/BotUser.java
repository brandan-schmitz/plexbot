package net.celestialdata.plexbot.database;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@SuppressWarnings("unused")
@Entity
@Table(name = "Users")
@NamedQuery(name = "BotUsers.findAll", query = "SELECT c FROM BotUser c ORDER BY c.id")
public class BotUser extends PanacheEntity {

    @Column(name = "discord_id", unique = true, nullable = false)
    public Long discordId;

    @Column(name = "discriminated_name", nullable = false)
    public String discriminatedName;

    public BotUser() {
    }

    public BotUser(Long discordId, String discriminatedName) {
        this.discordId = discordId;
        this.discriminatedName = discriminatedName;
    }

    public static BotUser getByDiscordId(long discordId) {
        return find("discordId", discordId).firstResult();
    }

    public static BotUser getByDiscriminatedName(String discriminatedName) {
        return find("discriminatedName", discriminatedName).firstResult();
    }

}