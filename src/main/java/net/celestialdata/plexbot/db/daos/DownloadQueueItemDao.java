package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.clients.models.sg.objects.SgHistoryItem;
import net.celestialdata.plexbot.db.entities.DownloadQueueItem;
import net.celestialdata.plexbot.utilities.FileUtilities;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@ApplicationScoped
public class DownloadQueueItemDao {

    @Inject
    FileUtilities fileUtilities;

    @Transactional
    public List<DownloadQueueItem> listAll() {
        return DownloadQueueItem.listAll();
    }

    @Transactional
    public List<DownloadQueueItem> listDownloading() {
        return DownloadQueueItem.list("status", "downloading");
    }

    @Transactional
    public DownloadQueueItem get(int id) {
        return DownloadQueueItem.findById(id);
    }

    @Transactional
    public DownloadQueueItem getByFilename(String filename) {
        return DownloadQueueItem.find("filename", filename).firstResult();
    }

    @Transactional
    public DownloadQueueItem getNext() {
        if (DownloadQueueItem.count("status", "queued") != 0) {
            return (DownloadQueueItem) DownloadQueueItem.list("status", "queued").get(0);
        } else return null;
    }

    @Transactional
    public long getDownloadingCount() {
        return DownloadQueueItem.count("status", "downloading");
    }

    @Transactional
    public boolean exists(int id) {
        return DownloadQueueItem.count("id", id) == 1;
    }

    @Transactional
    public boolean exists(String filename) {
        return DownloadQueueItem.count("filename", filename) == 1;
    }

    @Transactional
    public boolean exists(long showTvdbId, int seasonNumber, int episodeNumber) {
        return DownloadQueueItem.count("showId = ?1 and seasonNumber = ?2 and episodeNumber = ?3", showTvdbId, seasonNumber, episodeNumber) == 1;
    }

    @Transactional
    public boolean existsByResource(String resource) {
        return DownloadQueueItem.count("resource", resource) == 1;
    }

    @Transactional
    public DownloadQueueItem create(SgHistoryItem historyItem, String filename, String filetype) {
        DownloadQueueItem entity = new DownloadQueueItem();
        entity.resource = historyItem.resource;
        entity.filename = filename;
        entity.filetype = filetype;
        entity.showId = historyItem.tvdbId;
        entity.seasonNumber = historyItem.season;
        entity.episodeNumber = historyItem.episode;
        entity.quality = historyItem.quality;
        entity.status = "queued";
        entity.persist();
        return entity;
    }

    @Transactional
    public DownloadQueueItem updateStatus(int id, String status) {
        DownloadQueueItem entity = DownloadQueueItem.findById(id);
        entity.status = status;
        return entity;
    }

    @Transactional
    public void delete(DownloadQueueItem queueItem) {
        DownloadQueueItem entity = DownloadQueueItem.findById(queueItem.id);
        entity.delete();
    }
}