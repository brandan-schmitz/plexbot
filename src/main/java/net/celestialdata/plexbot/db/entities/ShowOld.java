package net.celestialdata.plexbot.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Shows_old")
public class ShowOld extends PanacheEntityBase {

    @Id
    @Column(name = "show_id", nullable = false)
    public String id;

    @Column(name = "show_name", nullable = false)
    public String name;

    @Column(name = "show_foldername", nullable = false)
    public String foldername;
}