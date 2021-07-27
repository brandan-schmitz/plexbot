package net.celestialdata.plexbot.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Corrupted_Media")
public class CorruptedMediaItem extends PanacheEntityBase {

    @Id
    @Column(name = "item_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public int id;

    @Column(name = "item_type", nullable = false)
    public String type;

    @Column(name = "item_path", nullable = false, unique = true)
    public String path;

    @Column(name = "item_message_id", nullable = false)
    public String messageId;
}