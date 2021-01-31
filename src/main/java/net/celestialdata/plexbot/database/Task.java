package net.celestialdata.plexbot.database;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "Tasks")
@SuppressWarnings("unused")
public class Task extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    public String progress;

    @ManyToOne()
    @JoinColumn(name = "started_by")
    public BotUser startedBy;

    public Boolean finished;

    @Column(name = "started_at", nullable = false)
    public Instant startedAt;

    @Column(name = "finished_at")
    public Instant finishedAt;

    public Task() {
        startedAt = Instant.now();
    }

    public Task(Instant startedAt) {
        this.startedAt = startedAt;
    }

}