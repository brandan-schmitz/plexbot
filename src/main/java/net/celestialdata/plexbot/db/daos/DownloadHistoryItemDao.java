package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.clients.models.sg.enums.SgQuality;
import net.celestialdata.plexbot.db.entities.DownloadHistoryItem;
import net.celestialdata.plexbot.db.entities.DownloadQueueItem;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

@SuppressWarnings("UnusedReturnValue")
@ApplicationScoped
public class DownloadHistoryItemDao {

    @Transactional
    public boolean exists(long showTvdbId, int seasonNumber, int episodeNumber, SgQuality quality, String status) {
        return DownloadHistoryItem.count("showId = ?1 and seasonNumber = ?2 and episodeNumber = ?3 and quality = ?4 and status = ?5",
                showTvdbId, seasonNumber, episodeNumber, quality, status) >= 1;
    }

    @Transactional
    public long countFailed(long showTvdbId, int seasonNumber, int episodeNumber, SgQuality quality, String filename) {
        return DownloadHistoryItem.count("showId = ?1 and seasonNumber = ?2 and episodeNumber = ?3 and quality = ?4 and filename = ?5",
                showTvdbId, seasonNumber, episodeNumber, quality, filename);
    }

    @Transactional
    public DownloadHistoryItem create(DownloadQueueItem queueItem, String status) {
        DownloadHistoryItem entity = new DownloadHistoryItem();
        entity.resource = queueItem.resource;
        entity.filename = queueItem.filename;
        entity.filetype = queueItem.filetype;
        entity.showId = queueItem.showId;
        entity.seasonNumber = queueItem.seasonNumber;
        entity.episodeNumber = queueItem.episodeNumber;
        entity.quality = queueItem.quality;
        entity.status = status;
        entity.time = LocalDateTime.now();
        entity.persist();
        return entity;
    }
}