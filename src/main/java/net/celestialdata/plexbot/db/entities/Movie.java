package net.celestialdata.plexbot.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Movies")
public class Movie extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "movie_id", nullable = false)
    public Integer id;

    @Column(name = "movie_tmdb_id", nullable = false, unique = true)
    public Long tmdbId;

    @Column(name = "movie_imdb_id")
    public String imdbId;

    @Column(name = "movie_title", nullable = false)
    public String title;

    @Column(name = "movie_year", nullable = false)
    public String year;

    @Column(name = "movie_resolution", nullable = false)
    public Integer resolution;

    @Column(name = "movie_height")
    public Integer height;

    @Column(name = "movie_width")
    public Integer width;

    @Column(name = "movie_duration")
    public Integer duration;

    @Column(name = "movie_codec")
    public String codec;

    @Column(name = "movie_filename")
    public String filename;

    @Column(name = "movie_filetype")
    public String filetype;

    @Column(name = "movie_foldername")
    public String folderName;

    @Column(name = "movie_optimized")
    public Boolean isOptimized;
}