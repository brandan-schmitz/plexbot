package net.celestialdata.plexbot.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "Episode_Subtitles")
public class EpisodeSubtitle extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "subtitle_id", nullable = false)
    public int id;

    @Column(name = "subtitle_language", nullable = false)
    public String language;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "subtitle_episode", referencedColumnName = "episode_id", nullable = false)
    public Episode episode;

    @Column(name = "subtitle_filename", nullable = false, unique = true)
    public String filename;

    @Column(name = "subtitle_filetype", nullable = false)
    public String filetype;

    @Column(name = "subtitle_forced", nullable = false)
    public Boolean isForced;

    @Column(name = "subtitle_sdh", nullable = false)
    public Boolean isSDH;

    @Column(name = "subtitle_cc", nullable = false)
    public Boolean isCC;
}