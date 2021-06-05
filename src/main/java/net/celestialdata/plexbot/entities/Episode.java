package net.celestialdata.plexbot.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "Episodes")
public class Episode extends PanacheEntityBase {

    @Id
    @Column(name = "episode_id", nullable = false)
    public String id;

    @Column(name = "episode_title")
    public String title;

    @Column(name = "episode_date")
    public String date;

    @Column(name = "episode_number")
    public int number;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "episode_season", referencedColumnName = "season_id", nullable = false)
    public Season season;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "episode_show", referencedColumnName = "show_id", nullable = false)
    public Show show;

    @Column(name = "episode_filename")
    public String filename;

    @Column(name = "episode_filetype")
    public String filetype;

    @Column(name = "episode_height")
    public int height;

    @Column(name = "episode_width")
    public int width;

    @Column(name = "episode_duration")
    public int duration;

    @Column(name = "episode_codec")
    public String codec;

    @Column(name = "episode_resolution")
    public int resolution;

    @Column(name = "episode_optimized")
    public boolean isOptimized;
}