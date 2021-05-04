package net.celestialdata.plexbot.database.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings("unused")
@Entity
@Table(name = "Shows")
public class Show implements BaseModel {

    @Id
    @Column(name = "show_id")
    private String tvdbId;

    @Column(name = "show_name")
    private String name;

    @Column(name = "show_foldername")
    private String foldername;

    public Show() {
    }

    public Show(String tvdbId, String name, String foldername) {
        this.tvdbId = tvdbId;
        this.name = name;
        this.foldername = foldername;
    }

    public String getTvdbId() {
        return tvdbId;
    }

    public void setTvdbId(String tvdbId) {
        this.tvdbId = tvdbId;
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
