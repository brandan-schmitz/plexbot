package net.celestialdata.plexbot.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import net.celestialdata.plexbot.clients.models.sg.enums.SgQuality;

import javax.persistence.*;
import java.time.LocalDateTime;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Encoding_History")
public class EncodingHistoryItem extends PanacheEntityBase {

    @Id
    @Column(name = "item_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Integer id;

    @Column(name = "item_type", nullable = false)
    public String mediaType;

    @Column(name = "item_media_id", nullable = false)
    public Long mediaId;

    @Column(name = "item_encoding_agent", nullable = false)
    public String encodingAgent;

    @Column(name = "item_status", nullable = false)
    public String status;
}