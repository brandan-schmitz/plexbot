package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.db.entities.Show;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused"})
@ApplicationScoped
public class ShowDao {

    public List<Show> listALl() {
        return Show.listAll();
    }

    public Show get(int id) {
        return Show.findById(id);
    }

    public Show getByTmdbId(long tmdbId) {
        return Show.find("tmdbId", tmdbId).firstResult();
    }

    public boolean exists(int id) {
        return Show.count("id", id) == 1;
    }

    public boolean existsByTmdbId(long tmdbId) {
        return Show.count("tmdbId", tmdbId) == 1;
    }

    @SuppressWarnings("DuplicatedCode")
    @Transactional
    public Show create(long tmdbId, String name, String foldername) {
        if (existsByTmdbId(tmdbId)) {
            return getByTmdbId(tmdbId);
        } else {
            Show entity = new Show();
            entity.tmdbId = tmdbId;
            entity.name = name;
            entity.foldername = foldername;
            entity.persist();
            return entity;
        }
    }

    @Transactional
    public Show update(int id, Show updatedItem) {
        Show entity = Show.findById(id);
        entity.tmdbId = updatedItem.tmdbId;
        entity.name = updatedItem.name;
        entity.foldername = updatedItem.foldername;
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
        Show entity = Show.find("tvdbId", tvdbId).firstResult();
        entity.delete();
    }
}