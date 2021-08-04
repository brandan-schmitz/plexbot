package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.dataobjects.ParsedSubtitleFilename;
import net.celestialdata.plexbot.db.entities.Episode;
import net.celestialdata.plexbot.db.entities.EpisodeSubtitle;
import net.celestialdata.plexbot.db.entities.Movie;
import net.celestialdata.plexbot.db.entities.MovieSubtitle;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused"})
@ApplicationScoped
public class MovieSubtitleDao {

    public List<MovieSubtitle> listALl() {
        return MovieSubtitle.listAll();
    }

    public MovieSubtitle get(int id) {
        return MovieSubtitle.findById(id);
    }

    public MovieSubtitle getByFilename(String filename) {
        return MovieSubtitle.find("filename", filename).firstResult();
    }

    public List<MovieSubtitle> getByMovie(Movie movie) {
        return MovieSubtitle.list("movie", movie);
    }

    public boolean exists(int id) {
        return MovieSubtitle.count("id", id) == 1;
    }

    public boolean existsByFilename(String filename) {
        return MovieSubtitle.count("filename", filename) == 1;
    }

    public MovieSubtitle create(Movie movie, ParsedSubtitleFilename parsedSubtitleFilename, String finalFilename) {
        return create(movie, parsedSubtitleFilename.language, finalFilename, parsedSubtitleFilename.fileType.getTypeString(),
                parsedSubtitleFilename.isForced, parsedSubtitleFilename.isSDH, parsedSubtitleFilename.isCC);
    }

    @Transactional
    public MovieSubtitle create(Movie movie, String language, String filename, String filetype, boolean isForced, boolean isSdh, boolean isCc) {
        if (existsByFilename(filename)) {
            return getByFilename(filename);
        } else {
            MovieSubtitle entity = new MovieSubtitle();
            entity.language = language;
            entity.movie = movie;
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
    public MovieSubtitle update(int id, MovieSubtitle subtitle) {
        MovieSubtitle entity = MovieSubtitle.findById(id);

        entity.language = subtitle.language;
        entity.movie = subtitle.movie;
        entity.filename = subtitle.filename;
        entity.filetype = subtitle.filetype;
        entity.isForced = subtitle.isForced;
        entity.isSDH = subtitle.isSDH;
        entity.isCC = subtitle.isCC;

        return entity;
    }

    @Transactional
    public void delete(int id) {
        MovieSubtitle entity = MovieSubtitle.findById(id);
        entity.delete();
    }

    @Transactional
    public void deleteByFilename(String filename) {
        MovieSubtitle entity = MovieSubtitle.find("filename", filename).firstResult();
        entity.delete();
    }
}