package net.celestialdata.plexbot.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Episodes")
public class Episode extends PanacheEntityBase {

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Id
    @Column(name = "episode_id", nullable = false)
    public String id;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "episode_title")
    public String title;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "episode_date")
    public String date;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "episode_number")
    public int number;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "episode_season", referencedColumnName = "season_id", nullable = false)
    public Season season;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "episode_show", referencedColumnName = "show_id", nullable = false)
    public Show show;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "episode_filename")
    public String filename;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "episode_filetype")
    public String filetype;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "episode_height")
    public int height;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "episode_width")
    public int width;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "episode_duration")
    public int duration;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "episode_codec")
    public String codec;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "episode_resolution")
    public int resolution;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "episode_optimized")
    public boolean isOptimized;
}