package net.celestialdata.plexbot.database.models;

import net.celestialdata.plexbot.database.DbOperations;
import org.hibernate.annotations.Proxy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings("unused")
@Entity
@Table(name = "Users")
@Proxy(lazy = false)
public class User implements BaseModel {
    @Id
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_discriminated_name", nullable = false)
    private String discriminatedName;

    public User() {
    }

    public User(Long id, String discriminatedName) {
        this.id = id;
        this.discriminatedName = discriminatedName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDiscriminatedName() {
        return discriminatedName;
    }

    public void setDiscriminatedName(String discriminatedName) {
        this.discriminatedName = discriminatedName;
    }

    @Override
    public String toString() {
        return "User [id=" + this.id + ", discriminatedName=" + this.discriminatedName + "]";
    }

    @Override
    public void onDelete() {
        for (WaitlistItem item : DbOperations.waitlistItemOps.getAllItemsByUser(this.id)) {
            DbOperations.deleteItem(WaitlistItem.class, item.getId());
        }
    }
}