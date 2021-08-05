package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.db.entities.EncodingWorkItem;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused"})
@ApplicationScoped
public class EncodingWorkItemDao {

    @Transactional
    public List<EncodingWorkItem> listALl() {
        return EncodingWorkItem.listAll();
    }

    @Transactional
    public EncodingWorkItem get(int id) {
        return EncodingWorkItem.findById(id);
    }

    @Transactional
    public EncodingWorkItem getByMediaId(long mediaId) {
        return EncodingWorkItem.find("mediaId", mediaId).firstResult();
    }

    @Transactional
    public boolean exists(int id) {
        return EncodingWorkItem.count("id", id) == 1;
    }

    @Transactional
    public boolean existsByMediaId(long mediaId) {
        return EncodingWorkItem.count("mediaId", mediaId) == 1;
    }

    @Transactional
    public EncodingWorkItem create(String progress, String workerAgentName, String mediaType, long mediaId) {
        if (existsByMediaId(mediaId)) {
            return getByMediaId(mediaId);
        } else {
            EncodingWorkItem entity = new EncodingWorkItem();
            entity.progress = progress;
            entity.workerAgentName = workerAgentName;
            entity.mediaType = mediaType;
            entity.mediaId = mediaId;
            entity.persist();

            return entity;
        }
    }

    @Transactional
    public EncodingWorkItem create(EncodingWorkItem entity) {
        if (existsByMediaId(entity.mediaId)) {
            return update(getByMediaId(entity.mediaId).id, entity);
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
        entity.mediaId = updatedItem.mediaId;
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