package net.celestialdata.plexbot.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings("unused")
@Entity
@Table(name = "Users")
public class User extends PanacheEntityBase {

    @Id
    @Column(name = "user_id", nullable = false)
    public long id;

    @Column(name = "user_discriminated_name", nullable = false)
    public String discriminatedName;
}