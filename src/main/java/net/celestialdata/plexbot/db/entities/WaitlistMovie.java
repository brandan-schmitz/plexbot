package net.celestialdata.plexbot.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Waitlist_Movies")
public class WaitlistMovie extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "movie_id", nullable = false)
    public Integer id;

    @Column(name = "movie_tmdb_id", nullable = false)
    public Long tmdbId;

    @Column(name = "movie_imdb_id")
    public String imdbId;

    @Column(name = "movie_title", nullable = false)
    public String title;

    @Column(name = "movie_year", nullable = false)
    public String year;

    @Column(name = "movie_requested_by", nullable = false)
    public Long requestedBy;

    @Column(name = "movie_message_id", nullable = false)
    public Long messageId;
}