package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.db.entities.EncodingQueueItem;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused"})
@ApplicationScoped
public class EncodingQueueItemDao {

    public List<EncodingQueueItem> listALl() {
        return EncodingQueueItem.listAll();
    }

    public EncodingQueueItem get(int id) {
        return EncodingQueueItem.findById(id);
    }

    public EncodingQueueItem getByTmdbId(long tmdbId) {
        return EncodingQueueItem.find("tmdbId", tmdbId).firstResult();
    }

    public boolean exists(int id) {
        return EncodingQueueItem.count("id", id) == 1;
    }

    public boolean existsByTmdbId(long tmdbId) {
        return EncodingQueueItem.count("tmdbId", tmdbId) == 1;
    }

    @Transactional
    public EncodingQueueItem create(String mediaType, long tmdbId) {
        if (existsByTmdbId(tmdbId)) {
            return getByTmdbId(tmdbId);
        } else {
            EncodingQueueItem entity = new EncodingQueueItem();
            entity.mediaType = mediaType;
            entity.tmdbId = tmdbId;
            entity.persist();

            return entity;
        }
    }

    @Transactional
    public void delete(int id) {
        EncodingQueueItem entity = EncodingQueueItem.findById(id);
        entity.delete();
    }

    @Transactional
    public void deleteByTmdbId(long tmdbId) {
        EncodingQueueItem entity = EncodingQueueItem.find("tmdbId", tmdbId).firstResult();
        entity.delete();
    }
}