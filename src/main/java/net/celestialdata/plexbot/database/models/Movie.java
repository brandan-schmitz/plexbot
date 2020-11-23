package net.celestialdata.plexbot.database.models;

import net.celestialdata.plexbot.database.DbOperations;
import org.hibernate.annotations.Proxy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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

    public Movie(String id, String title, String year, int resolution, String filename) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.resolution = resolution;
        this.filename = filename;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "Movie [id=" + this.id + ", title=" + this.title + ", year=" + this.year +
                ", resolution=" + this.resolution + ", filename=" + this.filename + "]";
    }

    @Override
    public void onDelete() {
        if (DbOperations.upgradeItemOps.exists(this.id)) {
            DbOperations.deleteItem(UpgradeItem.class, this.id);
        }
    }
}