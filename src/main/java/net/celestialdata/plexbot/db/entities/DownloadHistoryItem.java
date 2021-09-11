package net.celestialdata.plexbot.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import net.celestialdata.plexbot.clients.models.sg.enums.SgQuality;

import javax.persistence.*;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Download_History")
public class DownloadHistoryItem extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "history_id", nullable = false)
    public Integer id;

    @Column(name = "history_resource", unique = true)
    public String resource;

    @Column(name = "history_filename", unique = true)
    public String filename;

    @Column(name = "history_filetype", nullable = false)
    public String filetype;

    @Column(name = "history_show", nullable = false)
    public long showId;

    @Column(name = "history_season", nullable = false)
    public Integer seasonNumber;

    @Column(name = "history_episode", nullable = false)
    public Integer episodeNumber;

    @Column(name = "history_quality", nullable = false)
    public SgQuality quality;

    @Column(name = "history_status", nullable = false)
    public String status;
}