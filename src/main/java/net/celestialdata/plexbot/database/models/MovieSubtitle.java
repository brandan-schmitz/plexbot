package net.celestialdata.plexbot.database.models;

import javax.persistence.*;

@Entity
@Table(name = "Movie_Subtitles")
public class MovieSubtitle implements BaseModel {

    @Id
    @Column(name = "subtitle_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "subtitle_movie", referencedColumnName = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "subtitle_language")
    private String languageCode;

    @Column(name = "subtitle_filetype")
    private String filetype;

    @Column(name = "subtitle_filename")
    private String filename;

    public MovieSubtitle() {}

    public MovieSubtitle(Movie movie, String languageCode, String filetype, String filename) {
        this.movie = movie;
        this.languageCode = languageCode;
        this.filetype = filetype;
        this.filename = filename;
    }

    public int getId() {
        return id;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
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
