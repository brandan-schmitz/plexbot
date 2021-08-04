package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.db.entities.Movie;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused"})
@ApplicationScoped
public class MovieDao {

    public List<Movie> listALl() {
        return Movie.listAll();
    }

    public Movie get(int id) {
        return Movie.findById(id);
    }

    public Movie getByTmdbId(long tmdbId) {
        return Movie.find("tmdbId", tmdbId).firstResult();
    }

    public Movie getByImdbId(String imdbId) {
        return Movie.find("imdbId", imdbId).firstResult();
    }

    public Movie getByFilename(String filename) {
        return Movie.find("filename", filename).firstResult();
    }

    public boolean exists(int id) {
        return Movie.count("id", id) == 1;
    }

    public boolean existsByTmdbId(long tmdbId) {
        return Movie.count("tmdbId", tmdbId) == 1;
    }

    public boolean existsByImdbId(String imdbId) {
        return Movie.count("imdbId", imdbId) == 1;
    }

    @SuppressWarnings("DuplicatedCode")
    @Transactional
    public Movie createOrUpdate(Movie movie) {
        if (movie.id != null && exists(movie.id)) {
            Movie entity = Movie.findById(movie.id);
            entity.tmdbId = movie.tmdbId;
            entity.imdbId = movie.imdbId;
            entity.title = movie.title;
            entity.year = movie.year;
            entity.resolution = movie.resolution;
            entity.height = movie.height;
            entity.width = movie.width;
            entity.duration = movie.duration;
            entity.codec = movie.codec;
            entity.filename = movie.filename;
            entity.filetype = movie.filetype;
            entity.folderName = movie.folderName;
            entity.isOptimized = movie.isOptimized;
            return entity;
        } else if (movie.tmdbId != null && existsByTmdbId(movie.tmdbId)) {
            Movie entity = getByTmdbId(movie.tmdbId);
            entity.imdbId = movie.imdbId;
            entity.title = movie.title;
            entity.year = movie.year;
            entity.resolution = movie.resolution;
            entity.height = movie.height;
            entity.width = movie.width;
            entity.duration = movie.duration;
            entity.codec = movie.codec;
            entity.filename = movie.filename;
            entity.filetype = movie.filetype;
            entity.folderName = movie.folderName;
            entity.isOptimized = movie.isOptimized;
            return entity;
        } else if (movie.imdbId != null && existsByImdbId(movie.imdbId)) {
            Movie entity = getByImdbId(movie.imdbId);
            entity.tmdbId = movie.tmdbId;
            entity.title = movie.title;
            entity.year = movie.year;
            entity.resolution = movie.resolution;
            entity.height = movie.height;
            entity.width = movie.width;
            entity.duration = movie.duration;
            entity.codec = movie.codec;
            entity.filename = movie.filename;
            entity.filetype = movie.filetype;
            entity.folderName = movie.folderName;
            entity.isOptimized = movie.isOptimized;
            return entity;
        } else {
            movie.persist();
            return movie;
        }
    }

    @Transactional
    public void delete(int id) {
        Movie entity = Movie.findById(id);
        entity.delete();
    }

    @Transactional
    public void deleteByTmdbId(long tmdbId) {
        Movie entity = Movie.find("tmdbId", tmdbId).firstResult();
        entity.delete();
    }

    @Transactional
    public void deleteByImdbId(String imdbId) {
        Movie entity = Movie.find("imdbId", imdbId).firstResult();
        entity.delete();
    }
}