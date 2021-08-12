package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.dataobjects.ParsedSubtitleFilename;
import net.celestialdata.plexbot.db.entities.Movie;
import net.celestialdata.plexbot.db.entities.MovieSubtitle;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@ApplicationScoped
public class MovieSubtitleDao {

    @Transactional
    public List<MovieSubtitle> listALl() {
        return MovieSubtitle.listAll();
    }

    @Transactional
    public MovieSubtitle get(int id) {
        return MovieSubtitle.findById(id);
    }

    @Transactional
    public MovieSubtitle getByFilename(String filename) {
        return MovieSubtitle.find("filename", filename).firstResult();
    }

    @Transactional
    public List<MovieSubtitle> getByMovie(Movie movie) {
        return MovieSubtitle.list("movie", movie);
    }

    @Transactional
    public boolean exists(int id) {
        return MovieSubtitle.count("id", id) == 1;
    }

    @Transactional
    public boolean existsByFilename(String filename) {
        return MovieSubtitle.count("filename", filename) == 1;
    }

    @SuppressWarnings("DuplicatedCode")
    @Transactional
    public MovieSubtitle createOrUpdate(int linkedMovie, ParsedSubtitleFilename parsedSubtitleFilename, String finalFilename) {
        Movie movie = Movie.findById(linkedMovie);
        if (existsByFilename(finalFilename)) {
            MovieSubtitle entity = MovieSubtitle.find("filename", finalFilename).firstResult();
            entity.movie = movie;
            entity.language = parsedSubtitleFilename.language;
            entity.filetype = parsedSubtitleFilename.fileType.getTypeString();
            entity.isForced = parsedSubtitleFilename.isForced;
            entity.isSDH = parsedSubtitleFilename.isSDH;
            entity.isCC = parsedSubtitleFilename.isCC;
            return entity;
        } else {
            MovieSubtitle entity = new MovieSubtitle();
            entity.movie = movie;
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
        MovieSubtitle entity = MovieSubtitle.findById(id);
        entity.delete();
    }

    @Transactional
    public void deleteByFilename(String filename) {
        MovieSubtitle entity = MovieSubtitle.find("filename", filename).firstResult();
        entity.delete();
    }
}