package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.entities.Movie;
import net.celestialdata.plexbot.entities.MovieSubtitle;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Tag(name = "Movies", description = "Endpoints available for fetching information about movies in the database")
@Path("/movies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MovieResource {

    @GET
    @Path("/{id}")
    public Movie get(@PathParam("id") String id) {
        return Movie.findById(id);
    }

    @GET
    @Path("/{id}/subtitles")
    public List<MovieSubtitle> getSubtitles(@PathParam("id") String id) {
        Movie movie = Movie.findById(id);
        return MovieSubtitle.list("movie", movie);
    }
}