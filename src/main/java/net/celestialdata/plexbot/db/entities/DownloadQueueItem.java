package net.celestialdata.plexbot.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import net.celestialdata.plexbot.clients.models.sg.enums.SgQuality;

import javax.persistence.*;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Download_Queue")
public class DownloadQueueItem extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "download_id", nullable = false)
    public Integer id;

    @Column(name = "download_resource", nullable = false)
    public String resource;

    @Column(name = "download_filename", nullable = false)
    public String filename;

    @Column(name = "download_filetype", nullable = false)
    public String filetype;

    @Column(name = "download_show", nullable = false)
    public long showId;

    @Column(name = "download_season", nullable = false)
    public Integer seasonNumber;

    @Column(name = "download_episode", nullable = false)
    public Integer episodeNumber;

    @Column(name = "download_quality", nullable = false)
    public SgQuality quality;

    @Column(name = "download_status", nullable = false)
    public String status;
}