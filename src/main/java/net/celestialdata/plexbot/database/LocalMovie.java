package net.celestialdata.plexbot.database;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@SuppressWarnings("unused")
@Entity
@Table(name = "Movies")
public class LocalMovie extends PanacheEntity {

    @Column(name = "imdb_code", unique = true, nullable = false)
    public String imdbCode;

    @Column(nullable = false)
    public String title;

    @Column(name = "release_year", nullable = false)
    public String releaseYear;

    public int resolution;

    public String filename;

}