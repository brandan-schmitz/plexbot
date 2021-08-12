package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.db.entities.EncodingQueueItem;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused"})
@ApplicationScoped
public class EncodingQueueItemDao {

    @Transactional
    public List<EncodingQueueItem> listALl() {
        return EncodingQueueItem.listAll();
    }

    @Transactional
    public EncodingQueueItem get(int id) {
        return EncodingQueueItem.findById(id);
    }

    @Transactional
    public EncodingQueueItem getByMediaId(long mediaId) {
        return EncodingQueueItem.find("mediaId", mediaId).firstResult();
    }

    @Transactional
    public boolean exists(int id) {
        return EncodingQueueItem.count("id", id) == 1;
    }

    @Transactional
    public boolean existsByMediaId(long mediaId) {
        return EncodingQueueItem.count("mediaId", mediaId) == 1;
    }

    @Transactional
    public EncodingQueueItem create(String mediaType, long mediaId) {
        if (existsByMediaId(mediaId)) {
            return getByMediaId(mediaId);
        } else {
            EncodingQueueItem entity = new EncodingQueueItem();
            entity.mediaType = mediaType;
            entity.mediaId = mediaId;
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