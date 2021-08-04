package net.celestialdata.plexbot.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Episodes")
public class Episode extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "episode_id", nullable = false)
    public Integer id;

    @Column(name = "episode_tmdb_id", nullable = false, unique = true)
    public Long tmdbId;

    @Column(name = "episode_tvdb_id")
    public Long tvdbId;

    @Column(name = "episode_title")
    public String title;

    @Column(name = "episode_date")
    public String date;

    @Column(name = "episode_number")
    public Integer number;

    @Column(name = "episode_season")
    public String season;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "episode_show", referencedColumnName = "show_id", nullable = false)
    public Show show;

    @Column(name = "episode_filename")
    public String filename;

    @Column(name = "episode_filetype")
    public String filetype;

    @Column(name = "episode_height")
    public Integer height;

    @Column(name = "episode_width")
    public Integer width;

    @Column(name = "episode_duration")
    public Integer duration;

    @Column(name = "episode_codec")
    public String codec;

    @Column(name = "episode_resolution")
    public Integer resolution;

    @Column(name = "episode_optimized")
    public Boolean isOptimized;
}