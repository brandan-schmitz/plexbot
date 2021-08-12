package net.celestialdata.plexbot.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Shows")
public class Show extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "show_id", nullable = false)
    public Integer id;

    @Column(name = "show_tvdb_id", nullable = false, unique = true)
    public Long tvdbId;

    @Column(name = "show_name", nullable = false)
    public String name;

    @Column(name = "show_foldername", nullable = false)
    public String foldername;
}