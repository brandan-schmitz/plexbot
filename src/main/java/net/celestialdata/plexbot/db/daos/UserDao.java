package net.celestialdata.plexbot.db.daos;

import io.quarkus.elytron.security.common.BcryptUtil;
import net.celestialdata.plexbot.db.entities.User;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused"})
@ApplicationScoped
public class UserDao {

    @Transactional
    public List<User> listALl() {
        return User.listAll();
    }

    @Transactional
    public User get(int id) {
        return User.findById(id);
    }

    @Transactional
    public User getByUsername(String username) {
        return User.find("username", username).firstResult();
    }

    @Transactional
    public boolean exists(int id) {
        return User.count("id", id) == 1;
    }

    @Transactional
    public boolean existsByUsername(String username) {
        return User.count("username", username) == 1;
    }

    @Transactional
    public User createOrUpdate(String username, String password, String role) {
        if (existsByUsername(username)) {
            User entity = User.find("username", username).firstResult();
            entity.password = BcryptUtil.bcryptHash(password);
            entity.role = role;
            return entity;
        } else {
            User entity = new User();
            entity.username = username;
            entity.password = BcryptUtil.bcryptHash(password);
            entity.role = role;
            entity.persist();
            return entity;
        }
    }

    @Transactional
    public void delete(int id) {
        User entity = User.findById(id);
        entity.delete();
    }

    @Transactional
    public void deleteByUsername(String username) {
        User entity = User.find("username", username).firstResult();
        entity.delete();
    }
}