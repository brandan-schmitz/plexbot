package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.db.entities.EncodingQueueItem;
import net.celestialdata.plexbot.db.entities.EncodingWorkItem;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused"})
@ApplicationScoped
public class EncodingWorkItemDao {

    public List<EncodingWorkItem> listALl() {
        return EncodingWorkItem.listAll();
    }

    public EncodingWorkItem get(int id) {
        return EncodingWorkItem.findById(id);
    }

    public EncodingWorkItem getByTmdbId(long tmdbId) {
        return EncodingWorkItem.find("tmdbId", tmdbId).firstResult();
    }

    public boolean exists(int id) {
        return EncodingWorkItem.count("id", id) == 1;
    }

    public boolean existsByTmdbId(long tmdbId) {
        return EncodingWorkItem.count("tmdbId", tmdbId) == 1;
    }

    @Transactional
    public EncodingWorkItem create(String progress, String workerAgentName, String mediaType, long tmdbId) {
        if (existsByTmdbId(tmdbId)) {
            return getByTmdbId(tmdbId);
        } else {
            EncodingWorkItem entity = new EncodingWorkItem();
            entity.progress = progress;
            entity.workerAgentName = workerAgentName;
            entity.mediaType = mediaType;
            entity.tmdbId = tmdbId;
            entity.persist();

            return entity;
        }
    }

    @Transactional
    public EncodingWorkItem create(EncodingWorkItem entity) {
        if (existsByTmdbId(entity.tmdbId)) {
            return update(getByTmdbId(entity.tmdbId).id, entity);
        } else {
            entity.persist();
            return entity;
        }
    }

    @Transactional
    public EncodingWorkItem update(int id, EncodingWorkItem updatedItem) {
        EncodingWorkItem entity = EncodingWorkItem.findById(id);
        entity.progress = updatedItem.progress;
        entity.workerAgentName = updatedItem.workerAgentName;
        entity.mediaType = updatedItem.mediaType;
        entity.tmdbId = updatedItem.tmdbId;
        return entity;
    }

    @Transactional
    public void delete(int id) {
        EncodingWorkItem entity = EncodingWorkItem.findById(id);
        entity.delete();
    }

    @Transactional
    public void deleteByTmdbId(long tmdbId) {
        EncodingWorkItem entity = EncodingWorkItem.find("tmdbId", tmdbId).firstResult();
        entity.delete();
    }
}