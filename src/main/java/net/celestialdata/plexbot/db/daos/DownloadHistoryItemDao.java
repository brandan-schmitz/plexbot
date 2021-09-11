package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.db.entities.DownloadHistoryItem;
import net.celestialdata.plexbot.db.entities.DownloadQueueItem;
import net.celestialdata.plexbot.utilities.FileUtilities;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@ApplicationScoped
public class DownloadHistoryItemDao {

    @Inject
    FileUtilities fileUtilities;

    @Transactional
    public List<DownloadHistoryItem> listAll() {
        return DownloadHistoryItem.listAll();
    }

    @Transactional
    public DownloadHistoryItem get(int id) {
        return DownloadHistoryItem.findById(id);
    }

    @Transactional
    public DownloadHistoryItem getByFilename(String filename) {
        return DownloadHistoryItem.find("filename", filename).firstResult();
    }

    @Transactional
    public DownloadHistoryItem getByResource(String resource) {
        return DownloadHistoryItem.find("resource", resource).firstResult();
    }

    @Transactional
    public boolean exists(int id) {
        return DownloadHistoryItem.count("id", id) == 1;
    }

    @Transactional
    public boolean existsByFilename(String filename) {
        return DownloadHistoryItem.count("filename", filename) == 1;
    }

    @Transactional
    public boolean existsByResource(String resource) {
        return DownloadHistoryItem.count("resource", resource) == 1;
    }

    @Transactional
    public DownloadHistoryItem create(DownloadQueueItem queueItem, String status) {
        if (existsByResource(queueItem.resource)) {
            return getByResource(queueItem.resource);
        } else {
            DownloadHistoryItem entity = new DownloadHistoryItem();
            entity.resource = queueItem.resource;
            entity.filename = queueItem.filename;
            entity.filetype = queueItem.filetype;
            entity.showId = queueItem.showId;
            entity.seasonNumber = queueItem.seasonNumber;
            entity.episodeNumber = queueItem.episodeNumber;
            entity.quality = queueItem.quality;
            entity.status = status;
            entity.persist();
            return entity;
        }
    }

    @Transactional
    public DownloadHistoryItem updateStatus(int id, String status) {
        DownloadHistoryItem entity = DownloadHistoryItem.findById(id);
        entity.status = status;
        return entity;
    }

    @Transactional
    public void delete(int id) {
        DownloadHistoryItem entity = DownloadHistoryItem.findById(id);
        entity.delete();
    }
}