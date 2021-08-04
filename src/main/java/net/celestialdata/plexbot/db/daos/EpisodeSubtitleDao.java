package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.db.entities.Episode;
import net.celestialdata.plexbot.db.entities.EpisodeSubtitle;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused"})
@ApplicationScoped
public class EpisodeSubtitleDao {

    public List<EpisodeSubtitle> listALl() {
        return EpisodeSubtitle.listAll();
    }

    public EpisodeSubtitle get(int id) {
        return EpisodeSubtitle.findById(id);
    }

    public EpisodeSubtitle getByFilename(String filename) {
        return EpisodeSubtitle.find("filename", filename).firstResult();
    }

    public List<EpisodeSubtitle> getByEpisode(Episode episode) {
        return EpisodeSubtitle.list("episode", episode);
    }

    public boolean exists(int id) {
        return EpisodeSubtitle.count("id", id) == 1;
    }

    public boolean existsByFilename(String filename) {
        return EpisodeSubtitle.count("filename", filename) == 1;
    }

    @Transactional
    public EpisodeSubtitle create(Episode episode, String language, String filename, String filetype, boolean isForced, boolean isSdh, boolean isCc) {
        if (existsByFilename(filename)) {
            return getByFilename(filename);
        } else {
            EpisodeSubtitle entity = new EpisodeSubtitle();
            entity.language = language;
            entity.episode = episode;
            entity.filename = filename;
            entity.filetype = filetype;
            entity.isForced = isForced;
            entity.isSDH = isSdh;
            entity.isCC = isCc;
            entity.persist();

            return entity;
        }
    }

    @Transactional
    public EpisodeSubtitle update(int id, EpisodeSubtitle subtitle) {
        EpisodeSubtitle entity = EpisodeSubtitle.findById(id);

        entity.language = subtitle.language;
        entity.episode = subtitle.episode;
        entity.filename = subtitle.filename;
        entity.filetype = subtitle.filetype;
        entity.isForced = subtitle.isForced;
        entity.isSDH = subtitle.isSDH;
        entity.isCC = subtitle.isCC;

        return entity;
    }

    @Transactional
    public void delete(int id) {
        EpisodeSubtitle entity = EpisodeSubtitle.findById(id);
        entity.delete();
    }

    @Transactional
    public void deleteByFilename(String filename) {
        EpisodeSubtitle entity = EpisodeSubtitle.find("filename", filename).firstResult();
        entity.delete();
    }
}