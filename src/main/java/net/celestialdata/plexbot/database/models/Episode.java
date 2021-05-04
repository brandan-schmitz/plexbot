package net.celestialdata.plexbot.database.models;

import javax.persistence.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "Episodes")
public class Episode implements BaseModel {

    @Id
    @Column(name = "episode_id")
    private String tvdbId;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "episode_show", referencedColumnName = "show_id", nullable = false)
    private Show show;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "episode_season", referencedColumnName = "season_id")
    private Season season;

    @Column(name = "episode_title")
    private String title;

    @Column(name = "episode_number")
    private int number;

    @Column(name = "episode_date")
    private String date;

    @Column(name = "episode_width")
    private int width;

    @Column(name = "episode_height")
    private int height;

    @Column(name = "episode_resolution")
    private int resolution;

    @Column(name = "episode_filetype")
    private String filetype;

    @Column(name = "episode_filename")
    private String filename;

    public Episode() {
    }

    public Episode(String tvdbId, Show show, Season season, String title, int number, String date, int width, int height, int resolution, String filetype, String filename) {
        this.tvdbId = tvdbId;
        this.show = show;
        this.season = season;
        this.title = title;
        this.number = number;
        this.date = date;
        this.width = width;
        this.height = height;
        this.resolution = resolution;
        this.filetype = filetype;
        this.filename = filename;
    }

    public String getTvdbId() {
        return tvdbId;
    }

    public void setTvdbId(String tvdbId) {
        this.tvdbId = tvdbId;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
