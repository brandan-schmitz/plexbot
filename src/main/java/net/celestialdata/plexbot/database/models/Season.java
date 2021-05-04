package net.celestialdata.plexbot.database.models;

import javax.persistence.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "Seasons")
public class Season implements BaseModel {

    @Id
    @Column(name = "season_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "season_show", referencedColumnName = "show_id", nullable = false)
    private Show show;

    @Column(name = "season_number")
    private int number;

    @Column(name = "season_foldername")
    private String foldername;

    public Season() {
    }

    public Season(Show show, int number, String foldername) {
        this.show = show;
        this.number = number;
        this.foldername = foldername;
    }

    public int getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public String getFoldername() {
        return foldername;
    }

    public void setFoldername(String foldername) {
        this.foldername = foldername;
    }
}
