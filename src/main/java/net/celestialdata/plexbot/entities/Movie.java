package net.celestialdata.plexbot.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings("unused")
@Entity
@Table(name = "Movies")
public class Movie extends PanacheEntityBase {

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
    @Column(name = "movie_resolution", nullable = false)
    public int resolution;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "movie_height")
    public int height;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "movie_width")
    public int width;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "movie_duration")
    public int duration;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "movie_codec")
    public String codec;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "movie_filename")
    public String filename;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "movie_filetype")
    public String filetype;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "movie_foldername")
    public String folderName;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "movie_optimized")
    public boolean isOptimized;
}