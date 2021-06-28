package net.celestialdata.plexbot.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Encoding_Queue")
public class EncodingQueueItem extends PanacheEntityBase {

    @Id
    @Column(name = "item_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public int id;

    @Column(name = "item_type", nullable = false)
    public String type;

    @Column(name = "item_media_id", nullable = false, unique = true)
    public String mediaId;
}