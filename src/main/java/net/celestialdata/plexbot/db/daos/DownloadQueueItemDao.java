package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.clients.models.sg.objects.SgHistoryItem;
import net.celestialdata.plexbot.db.entities.DownloadQueueItem;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@ApplicationScoped
public class DownloadQueueItemDao {

    @ConfigProperty(name = "SickgearSettings.torrentFolder")
    String torrentFolder;

    @Inject
    FileUtilities fileUtilities;

    @Transactional
    public List<DownloadQueueItem> listAll() {
        return DownloadQueueItem.listAll();
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
    public boolean exists(int id) {
        return DownloadQueueItem.count("id", id) == 1;
    }

    @Transactional
    public boolean exists(String filename) {
        return DownloadQueueItem.count("filename", filename) == 1;
    }

    @Transactional
    public DownloadQueueItem create(SgHistoryItem historyItem, String filename, String filetype) {
        if (exists(filename)) {
            return DownloadQueueItem.find("filename", filename).firstResult();
        } else {
            DownloadQueueItem entity = new DownloadQueueItem();
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
    }
}