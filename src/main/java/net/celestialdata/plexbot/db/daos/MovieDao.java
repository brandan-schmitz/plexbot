package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.clients.models.tmdb.TmdbMovie;
import net.celestialdata.plexbot.db.entities.Movie;
import net.celestialdata.plexbot.enumerators.FileType;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@ApplicationScoped
public class MovieDao {

    @ConfigProperty(name = "FolderSettings.movieFolder")
    String movieFolder;

    @Inject
    FileUtilities fileUtilities;

    @Transactional
    public List<Movie> listALl() {
        return Movie.listAll();
    }

    @Transactional
    public Movie get(int id) {
        return Movie.findById(id);
    }

    @Transactional
    public Movie getByTmdbId(long tmdbId) {
        return Movie.find("tmdbId", tmdbId).firstResult();
    }

    @Transactional
    public Movie getByImdbId(String imdbId) {
        return Movie.find("imdbId", imdbId).firstResult();
    }

    @Transactional
    public Movie getByFilename(String filename) {
        return Movie.find("filename", filename).firstResult();
    }

    @Transactional
    public boolean exists(int id) {
        return Movie.count("id", id) == 1;
    }

    @Transactional
    public boolean existsByTmdbId(long tmdbId) {
        return Movie.count("tmdbId", tmdbId) == 1;
    }

    @Transactional
    public boolean existsByImdbId(String imdbId) {
        return Movie.count("imdbId", imdbId) == 1;
    }

    @SuppressWarnings("DuplicatedCode")
    @Transactional
    public Movie createOrUpdate(TmdbMovie movieData, String filename) {
        var movieFileData = fileUtilities.getMediaInfo(movieFolder + fileUtilities.generatePathname(movieData) + "/" + filename);
        var fileType = FileType.determineFiletype(filename);

        if (existsByTmdbId(movieData.tmdbId)) {
            Movie entity = Movie.find("tmdbId", movieData.tmdbId).firstResult();
            entity.imdbId = movieData.imdbId;
            entity.title = movieData.title;
            entity.year = movieData.getYear();
            entity.filename = filename;
            entity.filetype = fileType.getTypeString();
            entity.folderName = fileUtilities.generatePathname(movieData);
            entity.resolution = movieFileData.resolution();
            entity.height = movieFileData.height;
            entity.width = movieFileData.width;
            entity.duration = movieFileData.duration;
            entity.codec = movieFileData.codec;
            entity.isOptimized = movieFileData.isOptimized();
            return entity;
        } else {
            Movie entity = new Movie();
            entity.tmdbId = movieData.tmdbId;
            entity.imdbId = movieData.imdbId;
            entity.title = movieData.title;
            entity.year = movieData.getYear();
            entity.filename = filename;
            entity.filetype = fileType.getTypeString();
            entity.folderName = fileUtilities.generatePathname(movieData);
            entity.resolution = movieFileData.resolution();
            entity.height = movieFileData.height;
            entity.width = movieFileData.width;
            entity.duration = movieFileData.duration;
            entity.codec = movieFileData.codec;
            entity.isOptimized = movieFileData.isOptimized();
            entity.persist();
            return entity;
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