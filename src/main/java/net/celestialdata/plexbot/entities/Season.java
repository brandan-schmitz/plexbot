package net.celestialdata.plexbot.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "Seasons")
public class Season extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "season_id", nullable = false)
    public int id;

    @Column(name = "season_number", nullable = false)
    public int number;

    @Column(name = "season_foldername", nullable = false)
    public String foldername;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "season_show", referencedColumnName = "show_id", nullable = false)
    public Show show;
}