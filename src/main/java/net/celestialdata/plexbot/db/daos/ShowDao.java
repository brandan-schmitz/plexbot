package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.db.entities.Show;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused"})
@ApplicationScoped
public class ShowDao {

    @Transactional
    public List<Show> listALl() {
        return Show.listAll();
    }

    @Transactional
    public Show get(int id) {
        return Show.findById(id);
    }

    @Transactional
    public Show getByTvdbId(long tvdbId) {
        return Show.find("tvdbId", tvdbId).firstResult();
    }

    @Transactional
    public boolean exists(int id) {
        return Show.count("id", id) == 1;
    }

    @Transactional
    public boolean existsByTvdbId(long tvdbId) {
        return Show.count("tvdbId", tvdbId) == 1;
    }

    @Transactional
    public Show create(long tvdbId, String name, String foldername) {
        if (existsByTvdbId(tvdbId)) {
            return Show.find("tvdbId", tvdbId).firstResult();
        } else {
            Show entity = new Show();
            entity.tvdbId = tvdbId;
            entity.name = name;
            entity.foldername = foldername;
            entity.persist();
            return entity;
        }
    }

    @Transactional
    public Show update(int id, Show updatedItem) {
        Show entity = Show.findById(id);
        entity.name = updatedItem.name;
        entity.foldername = updatedItem.name;
        entity.tvdbId = updatedItem.tvdbId;
        return entity;
    }

    @Transactional
    public void delete(int id) {
        Show entity = Show.findById(id);
        entity.delete();
    }

    @Transactional
    public void deleteByTmdbId(long tmdbId) {
        Show entity = Show.find("tmdbId", tmdbId).firstResult();
        entity.delete();
    }

    @Transactional
    public void deleteByTvdbId(long tvdbId) {
        Show entity = Show.find("tmdbId", tvdbId).firstResult();
        entity.delete();
    }
}