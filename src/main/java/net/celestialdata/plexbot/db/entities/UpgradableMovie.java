package net.celestialdata.plexbot.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Upgradable_Movies")
public class UpgradableMovie extends PanacheEntityBase {

    @Id
    @Column(name = "upgrade_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public int id;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "upgrade_movie", referencedColumnName = "movie_id", unique = true)
    public Movie movie;

    @Column(name = "upgrade_new_resolution", nullable = false)
    public int newResolution;

    @Column(name = "upgrade_message_id", nullable = false)
    public Long messageId;
}