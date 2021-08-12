package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.db.daos.MovieDao;
import net.celestialdata.plexbot.db.daos.MovieSubtitleDao;
import net.celestialdata.plexbot.db.entities.Movie;
import net.celestialdata.plexbot.db.entities.MovieSubtitle;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Tag(name = "Movies", description = "Endpoints available for fetching information about movies in the database")
@Path("/movies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MovieResource {

    @Inject
    MovieDao movieDao;

    @Inject
    MovieSubtitleDao movieSubtitleDao;

    @GET
    @Path("/{tmdb_id}")
    public Movie get(@PathParam("tmdb_id") long tmdbId) {
        return movieDao.getByTmdbId(tmdbId);
    }

    @GET
    @Path("/{id}/subtitles")
    public List<MovieSubtitle> getSubtitles(@PathParam("id") int id) {
        Movie movie = movieDao.get(id);
        return movieSubtitleDao.getByMovie(movie);
    }
}