package net.celestialdata.plexbot.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Shows")
public class Show extends PanacheEntityBase {

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Id
    @Column(name = "show_id", nullable = false)
    public String id;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "show_name", nullable = false)
    public String name;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @Column(name = "show_foldername", nullable = false)
    public String foldername;
}