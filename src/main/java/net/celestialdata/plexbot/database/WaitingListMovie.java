package net.celestialdata.plexbot.database;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.*;

@Entity
@Table(name = "MovieWaitingList")
@SuppressWarnings("unused")
public class WaitingListMovie extends PanacheEntity {

    @Column(name = "imdb_code", unique = true, nullable = false)
    public String imdbCode;

    @Column(nullable = false)
    public String title;

    @Column(name = "release_year", nullable = false)
    public String releaseYear;

    @Column(name = "message_id", nullable = false)
    public Long messageId;

    @ManyToOne(optional = false, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "requested_by", nullable = false)
    public BotUser requestedBy;

}