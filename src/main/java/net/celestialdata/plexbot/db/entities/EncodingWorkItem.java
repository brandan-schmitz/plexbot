package net.celestialdata.plexbot.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Encoding_Work")
public class EncodingWorkItem extends PanacheEntityBase {

    @Id
    @Column(name = "item_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Integer id;

    @Column(name = "item_progress", nullable = false)
    public String progress;

    @Column(name = "item_agent_name", nullable = false)
    public String workerAgentName;

    @Column(name = "item_type", nullable = false)
    public String mediaType;

    @Column(name = "item_media_id", nullable = false, unique = true)
    public Long mediaId;
}