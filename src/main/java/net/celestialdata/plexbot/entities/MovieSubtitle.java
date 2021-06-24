package net.celestialdata.plexbot.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Movie_Subtitles")
public class MovieSubtitle extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "subtitle_id", nullable = false)
    public int id;

    @Column(name = "subtitle_language", nullable = false)
    public String language;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "subtitle_movie", referencedColumnName = "movie_id")
    public Movie movie;

    @Column(name = "subtitle_filename", nullable = false, unique = true)
    public String filename;

    @Column(name = "subtitle_filetype", nullable = false)
    public String filetype;

    @Column(name = "subtitle_forced", nullable = false)
    public Boolean isForced;

    @Column(name = "subtitle_sdh", nullable = false)
    public Boolean isSDH;

    @Column(name = "subtitle_cc", nullable = false)
    public Boolean isCC;
}