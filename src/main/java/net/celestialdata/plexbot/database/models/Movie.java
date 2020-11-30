package net.celestialdata.plexbot.database.models;

import net.celestialdata.plexbot.database.DbOperations;
import org.hibernate.annotations.Proxy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Object representation of the Movie's column in the database
 */
@SuppressWarnings("unused")
@Entity
@Table(name = "Movies")
@Proxy(lazy = false)
public class Movie implements BaseModel {
    @Id
    @Column(name = "movie_id")
    private String id;

    @Column(name = "movie_title", nullable = false)
    private String title;

    @Column(name = "movie_year", nullable = false)
    private String year;

    @Column(name = "movie_resolution", nullable = false)
    private int resolution;

    @Column(name = "movie_filename")
    private String filename;

    public Movie() {
    }

    /**
     * Create a new movie object directly
     *
     * @param id movie IMDB code
     * @param title movie title
     * @param year movie release year
     * @param resolution movie resolution
     * @param filename filename of the movie file
     */
    public Movie(String id, String title, String year, int resolution, String filename) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.resolution = resolution;
        this.filename = filename;
    }

    /**
     * Return the IMDB code of the movie
     * @return IMDB code
     */
    public String getId() {
        return id;
    }

    /**
     * Set the IMDB code of the movie
     * @param id IMDB code
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Return the title of the movie
     * @return movie title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title of the movie
     * @param title movie title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return the release year of the movie
     * @return movie release year
     */
    public String getYear() {
        return year;
    }

    /**
     * Set the release year of the movie
     * @param year movie release year
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * Return the resolution of the movie
     * @return movie resolution
     */
    public int getResolution() {
        return resolution;
    }

    /**
     * Set the resolution of the movie
     * @param resolution movie resolution
     */
    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    /**
     * Return the filename of the movie as stored on the filesystem
     * @return movie filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Set the filename of the movie as stored on the filesystem
     * @param filename movie filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Return a string representation of the movie
     * @return movie string representation
     */
    @Override
    public String toString() {
        return "Movie [id=" + this.id + ", title=" + this.title + ", year=" + this.year +
                ", resolution=" + this.resolution + ", filename=" + this.filename + "]";
    }

    /**
     * Ensure that any items in the Upgradable movies table is removed if the movie that it is set
     * to upgrade is removed from the database.
     */
    @Override
    public void onDelete() {
        if (DbOperations.upgradeItemOps.exists(this.id)) {
            DbOperations.deleteItem(UpgradeItem.class, this.id);
        }
    }
}