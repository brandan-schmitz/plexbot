package net.celestialdata.plexbot.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Upgradable_Movies")
public class UpgradableMovie extends PanacheEntityBase {

    @Id
    @Column(name = "movie_id", nullable = false)
    public String id;

    @Column(name = "movie_title", nullable = false)
    public String title;

    @Column(name = "movie_year", nullable = false)
    public String year;

    @Column(name = "movie_resolution", nullable = false)
    public int resolution;

    @Column(name = "movie_new_resolution", nullable = false)
    public int newResolution;

    @Column(name = "movie_message_id", nullable = false)
    public Long messageId;
}