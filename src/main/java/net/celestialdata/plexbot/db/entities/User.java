package net.celestialdata.plexbot.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;

import javax.persistence.*;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@UserDefinition
@Table(name = "Users")
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "user_id", nullable = false)
    public Integer id;

    @Username
    @Column(name = "user_username", nullable = false)
    public String username;

    @Password
    @Column(name = "user_password", nullable = false)
    public String password;

    @Roles
    @Column(name = "user_role", nullable = false)
    public String role;
}