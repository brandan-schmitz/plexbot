package net.celestialdata.plexbot.database.models;

import javax.persistence.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "Episodes")
public class Episode implements BaseModel {

    @Id
    @Column(name = "episode_imdb")
    private String imdbCode;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "episode_show", referencedColumnName = "show_imdb", nullable = false)
    private Show show;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "episode_season", referencedColumnName = "season_id")
    private Season season;

    @Column(name = "episode_number")
    private int number;

    @Column(name = "episode_year")
    private String year;

    @Column(name = "episode_width")
    private int width;

    @Column(name = "episode_height")
    private int height;

    @Column(name = "episode_filetype")
    private String filetype;

    @Column(name = "episode_filename")
    private String filename;

    public Episode() {}

    public Episode(String imdbCode, Show show, Season season, int number, String year, int width, int height, String filetype, String filename) {
        this.imdbCode = imdbCode;
        this.show = show;
        this.season = season;
        this.number = number;
        this.year = year;
        this.width = width;
        this.height = height;
        this.filetype = filetype;
        this.filename = filename;
    }

    public String getImdbCode() {
        return imdbCode;
    }

    public void setImdbCode(String imdbCode) {
        this.imdbCode = imdbCode;
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

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
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
