package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.clients.models.sg.enums.SgQuality;
import net.celestialdata.plexbot.clients.models.sg.objects.SgHistoryItem;
import net.celestialdata.plexbot.db.entities.DownloadQueueItem;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
@ApplicationScoped
public class DownloadQueueItemDao {

    @Transactional
    public List<DownloadQueueItem> listDownloading() {
        return DownloadQueueItem.list("status", "downloading");
    }

    @Transactional
    public long getDownloadingCount() {
        return DownloadQueueItem.count("status", "downloading");
    }

    @Transactional
    public boolean exists(long showTvdbId, int seasonNumber, int episodeNumber, SgQuality quality) {
        return DownloadQueueItem.count("showId = ?1 and seasonNumber = ?2 and episodeNumber = ?3 and quality = ?4",
                showTvdbId, seasonNumber, episodeNumber, quality) >= 1;
    }

    @Transactional
    public DownloadQueueItem getNext() {
        if (DownloadQueueItem.count("status", "queued") != 0) {
            return (DownloadQueueItem) DownloadQueueItem.list("status", "queued").get(0);
        } else return null;
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