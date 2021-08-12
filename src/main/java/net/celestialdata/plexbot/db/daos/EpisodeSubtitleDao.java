package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.dataobjects.ParsedSubtitleFilename;
import net.celestialdata.plexbot.db.entities.Episode;
import net.celestialdata.plexbot.db.entities.EpisodeSubtitle;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@ApplicationScoped
public class EpisodeSubtitleDao {

    @Transactional
    public List<EpisodeSubtitle> listALl() {
        return EpisodeSubtitle.listAll();
    }

    @Transactional
    public EpisodeSubtitle get(int id) {
        return EpisodeSubtitle.findById(id);
    }

    @Transactional
    public EpisodeSubtitle getByFilename(String filename) {
        return EpisodeSubtitle.find("filename", filename).firstResult();
    }

    @Transactional
    public List<EpisodeSubtitle> getByEpisode(Episode episode) {
        return EpisodeSubtitle.list("episode", episode);
    }

    @Transactional
    public boolean exists(int id) {
        return EpisodeSubtitle.count("id", id) == 1;
    }

    @Transactional
    public boolean existsByFilename(String filename) {
        return EpisodeSubtitle.count("filename", filename) == 1;
    }

    @SuppressWarnings("DuplicatedCode")
    @Transactional
    public EpisodeSubtitle createOrUpdate(int linkedEpisode, ParsedSubtitleFilename parsedSubtitleFilename, String finalFilename) {
        Episode episode = Episode.findById(linkedEpisode);
        if (existsByFilename(finalFilename)) {
            EpisodeSubtitle entity = EpisodeSubtitle.find("filename", finalFilename).firstResult();
            entity.episode = episode;
            entity.language = parsedSubtitleFilename.language;
            entity.filetype = parsedSubtitleFilename.fileType.getTypeString();
            entity.isForced = parsedSubtitleFilename.isForced;
            entity.isSDH = parsedSubtitleFilename.isSDH;
            entity.isCC = parsedSubtitleFilename.isCC;
            return entity;
        } else {
            EpisodeSubtitle entity = new EpisodeSubtitle();
            entity.episode = episode;
            entity.language = parsedSubtitleFilename.language;
            entity.filename = finalFilename;
            entity.filetype = parsedSubtitleFilename.fileType.getTypeString();
            entity.isForced = parsedSubtitleFilename.isForced;
            entity.isSDH = parsedSubtitleFilename.isSDH;
            entity.isCC = parsedSubtitleFilename.isCC;
            entity.persist();
            return entity;
        }
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