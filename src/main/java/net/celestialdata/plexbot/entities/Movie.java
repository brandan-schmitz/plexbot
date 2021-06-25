package net.celestialdata.plexbot.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Movies")
public class Movie extends PanacheEntityBase {

    @Id
    @Column(name = "movie_id", nullable = false)
    public String id;

    @Column(name = "movie_title", nullable = false)
    public String title;

    @Column(name = "movie_year", nullable = false)
    public String year;

    @Column(name = "movie_resolution", nullable = false)
    public int resolution;

    @Column(name = "movie_height")
    public int height;

    @Column(name = "movie_width")
    public int width;

    @Column(name = "movie_duration")
    public int duration;

    @Column(name = "movie_codec")
    public String codec;

    @Column(name = "movie_filename")
    public String filename;

    @Column(name = "movie_filetype")
    public String filetype;

    @Column(name = "movie_foldername")
    public String folderName;

    @Column(name = "movie_optimized")
    public boolean isOptimized;
}