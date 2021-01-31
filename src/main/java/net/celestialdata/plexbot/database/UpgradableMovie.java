package net.celestialdata.plexbot.database;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.*;

@Entity
@Table(name = "Upgrades")
@SuppressWarnings("unused")
public class UpgradableMovie extends PanacheEntity {

    @OneToOne(optional = false, orphanRemoval = true, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "existing_movie", nullable = false, unique = true)
    public LocalMovie movie;

    @Column(name = "new_resolution", nullable = false)
    public int newResolution;

    @Column(name = "message_id", unique = true, nullable = false)
    public long upgradeMessageId;

    public static LocalMovie getByImdb(String imdbCode) {
        return find("imdbCode", imdbCode).firstResult();
    }

}