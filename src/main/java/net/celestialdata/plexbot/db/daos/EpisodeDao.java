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

@SuppressWarnings({"unused"})
@ApplicationScoped
public class EpisodeDao {

    @ConfigProperty(name = "FolderSettings.tvFolder")
    String tvFolder;

    @Inject
    FileUtilities fileUtilities;

    public List<Episode> listALl() {
        return Episode.listAll();
    }

    public Episode get(int id) {
        return Episode.findById(id);
    }

    public Episode getByTmdbId(long tmdbId) {
        return Episode.find("tmdbId", tmdbId).firstResult();
    }

    public Episode getByTvdbId(long tvdbId) {
        return Episode.find("tvdbId", tvdbId).firstResult();
    }

    public Episode getByFilename(String filename) {
        return Episode.find("filename", filename).firstResult();
    }

    public boolean exists(int id) {
        return Episode.count("id", id) == 1;
    }

    public boolean existsByTmdbId(long tmdbId) {
        return Episode.count("tmdbId", tmdbId) == 1;
    }

    public boolean existsByTvdbId(long tvdbId) {
        return Episode.count("tvdbId", tvdbId) == 1;
    }

    public Episode createOrUpdate(TmdbEpisode episodeData, long tvdbId, String filename, Show show) {
        var episodeFileData = fileUtilities.getMediaInfo(tvFolder + show.foldername + "/Season " + episodeData.seasonNum + "/" + filename);
        var fileType = FileType.determineFiletype(filename);

        Episode entity = new Episode();
        entity.tmdbId = episodeData.tmdbId;
        entity.tvdbId = tvdbId;
        entity.title = episodeData.name;
        entity.date = episodeData.date;
        entity.number = episodeData.number;
        entity.season = episodeData.seasonNum;
        entity.show = show;
        entity.filename = filename;
        entity.filetype = fileType.getTypeString();
        entity.height = episodeFileData.height;
        entity.width = episodeFileData.width;
        entity.duration = episodeFileData.duration;
        entity.codec = episodeFileData.codec;
        entity.resolution = episodeFileData.resolution();
        entity.isOptimized = episodeFileData.isOptimized();

        return createOrUpdate(entity);
    }

    @SuppressWarnings("DuplicatedCode")
    @Transactional
    public Episode createOrUpdate(Episode episode) {
        if (episode.id != null && exists(episode.id)) {
            Episode entity = Episode.findById(episode.id);
            entity.tmdbId = episode.tmdbId;
            entity.tvdbId = episode.tvdbId;
            entity.title = episode.title;
            entity.date = episode.date;
            entity.number = episode.number;
            entity.season = episode.season;
            entity.show = episode.show;
            entity.filename = episode.filename;
            entity.filetype = episode.filetype;
            entity.height = episode.height;
            entity.width = episode.width;
            entity.duration = episode.duration;
            entity.codec = episode.codec;
            entity.resolution = episode.resolution;
            entity.isOptimized = episode.isOptimized;
            return entity;
        } else if (episode.tmdbId != null && existsByTmdbId(episode.tmdbId)) {
            Episode entity = getByTmdbId(episode.tmdbId);
            entity.tvdbId = episode.tvdbId;
            entity.title = episode.title;
            entity.date = episode.date;
            entity.number = episode.number;
            entity.season = episode.season;
            entity.show = episode.show;
            entity.filename = episode.filename;
            entity.filetype = episode.filetype;
            entity.height = episode.height;
            entity.width = episode.width;
            entity.duration = episode.duration;
            entity.codec = episode.codec;
            entity.resolution = episode.resolution;
            entity.isOptimized = episode.isOptimized;
            return entity;
        } else if (episode.tvdbId != null && existsByTvdbId(episode.tvdbId)) {
            Episode entity = getByTvdbId(episode.tvdbId);
            entity.tmdbId = episode.tmdbId;
            entity.title = episode.title;
            entity.date = episode.date;
            entity.number = episode.number;
            entity.season = episode.season;
            entity.show = episode.show;
            entity.filename = episode.filename;
            entity.filetype = episode.filetype;
            entity.height = episode.height;
            entity.width = episode.width;
            entity.duration = episode.duration;
            entity.codec = episode.codec;
            entity.resolution = episode.resolution;
            entity.isOptimized = episode.isOptimized;
            return entity;
        } else {
            episode.persist();
            return episode;
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