package net.celestialdata.plexbot.database.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Shows")
public class Show implements BaseModel {

    @Id
    @Column(name = "show_imdb")
    private String imdbCode;

    @Column(name = "show_name")
    private String name;

    @Column(name = "show_foldername")
    private String foldername;

    public Show() {}

    public Show(String imdbCode, String name, String foldername) {
        this.imdbCode = imdbCode;
        this.name = name;
        this.foldername = foldername;
    }

    public String getImdbCode() {
        return imdbCode;
    }

    public void setImdbCode(String imdbCode) {
        this.imdbCode = imdbCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFolderName() {
        return foldername;
    }

    public void setFolderName(String folderName) {
        this.foldername = folderName;
    }
}
