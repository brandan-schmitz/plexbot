package net.celestialdata.plexbot.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Waitlist_Movies_old")
public class WaitlistMovieOld extends PanacheEntityBase {

    @Id
    @Column(name = "movie_id", nullable = false)
    public String id;

    @Column(name = "movie_title", nullable = false)
    public String title;

    @Column(name = "movie_year", nullable = false)
    public String year;

    @Column(name = "movie_requested_by", nullable = false)
    public Long requestedBy;

    @Column(name = "movie_message_id", nullable = false)
    public Long messageId;
}