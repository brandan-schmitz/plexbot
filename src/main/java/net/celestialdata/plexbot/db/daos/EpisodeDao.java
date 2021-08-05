package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.clients.models.tmdb.TmdbEpisode;
import net.celestialdata.plexbot.db.entities.Episode;
import net.celestialdata.plexbot.db.entities.Show;
import net.celestialdata.plexbot.enumerators.FileType;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@ApplicationScoped
public class EpisodeDao {

    @ConfigProperty(name = "FolderSettings.tvFolder")
    String tvFolder;

    @Inject
    FileUtilities fileUtilities;

    @Transactional
    public List<Episode> listALl() {
        return Episode.listAll();
    }

    @Transactional
    public Episode get(int id) {
        return Episode.findById(id);
    }

    @Transactional
    public Episode getByTmdbId(long tmdbId) {
        return Episode.find("tmdbId", tmdbId).firstResult();
    }

    @Transactional
    public Episode getByTvdbId(long tvdbId) {
        return Episode.find("tvdbId", tvdbId).firstResult();
    }

    @Transactional
    public Episode getByFilename(String filename) {
        return Episode.find("filename", filename).firstResult();
    }

    @Transactional
    public boolean exists(int id) {
        return Episode.count("id", id) == 1;
    }

    @Transactional
    public boolean existsByTmdbId(long tmdbId) {
        return Episode.count("tmdbId", tmdbId) == 1;
    }

    @Transactional
    public boolean existsByTvdbId(long tvdbId) {
        return Episode.count("tvdbId", tvdbId) == 1;
    }

    @SuppressWarnings("DuplicatedCode")
    @Transactional
    public Episode createOrUpdate(TmdbEpisode episodeData, long tvdbId, String filename, int show) {
        Show showData = Show.findById(show);
        var episodeFileData = fileUtilities.getMediaInfo(tvFolder + showData.foldername + "/Season " + episodeData.seasonNum + "/" + filename);
        var fileType = FileType.determineFiletype(filename);

        if (existsByTmdbId(episodeData.tmdbId)) {
            Episode entity = Episode.find("tmdbId", episodeData.tmdbId).firstResult();
            entity.tvdbId = tvdbId;
            entity.title = episodeData.name;
            entity.date = episodeData.date;
            entity.number = episodeData.number;
            entity.season = episodeData.seasonNum;
            entity.show = showData;
            entity.filename = filename;
            entity.filetype = fileType.getTypeString();
            entity.height = episodeFileData.height;
            entity.width = episodeFileData.width;
            entity.duration = episodeFileData.duration;
            entity.codec = episodeFileData.codec;
            entity.resolution = episodeFileData.resolution();
            entity.isOptimized = episodeFileData.isOptimized();
            return entity;
        } else {
            Episode entity = new Episode();
            entity.tmdbId = episodeData.tmdbId;
            entity.tvdbId = tvdbId;
            entity.title = episodeData.name;
            entity.date = episodeData.date;
            entity.number = episodeData.number;
            entity.season = episodeData.seasonNum;
            entity.show = showData;
            entity.filename = filename;
            entity.filetype = fileType.getTypeString();
            entity.height = episodeFileData.height;
            entity.width = episodeFileData.width;
            entity.duration = episodeFileData.duration;
            entity.codec = episodeFileData.codec;
            entity.resolution = episodeFileData.resolution();
            entity.isOptimized = episodeFileData.isOptimized();
            entity.persist();
            return entity;
        }
    }

    @Transactional
    public void delete(int id) {
        Episode entity = Episode.findById(id);
        entity.delete();
    }

    @Transactional
    public void deleteByTmdbId(long tmdbId) {
        Episode entity = Episode.find("tmdbId", tmdbId).firstResult();
        entity.delete();
    }

    @Transactional
    public void deleteByTvdbId(long tvdbId) {
        Episode entity = Episode.find("tvdbId", tvdbId).firstResult();
        entity.delete();
    }
}