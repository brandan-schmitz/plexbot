package net.celestialdata.plexbot.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Corrupted_Media")
public class CorruptedMediaItem extends PanacheEntityBase {

    @Id
    @Column(name = "item_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Integer id;

    @Column(name = "item_type", nullable = false)
    public String type;

    @Column(name = "item_absolute_path", nullable = false, unique = true)
    public String absolutePath;

    @Column(name = "item_message_id", nullable = false)
    public Long messageId;
}