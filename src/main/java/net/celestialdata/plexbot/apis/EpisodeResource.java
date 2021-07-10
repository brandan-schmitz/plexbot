package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.entities.Episode;
import net.celestialdata.plexbot.entities.EpisodeSubtitle;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Tag(name = "Episodes", description = "Endpoints available for fetching information about episodes in the database")
@Path("/episodes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EpisodeResource {

    @GET
    @Path("/{id}")
    public Episode get(@PathParam("id") String id) {
        return Episode.findById(id);
    }

    @GET
    @Path("/{id}/subtitles")
    public List<EpisodeSubtitle> getSubtitles(@PathParam("id") String id) {
        Episode episode = Episode.findById(id);
        return EpisodeSubtitle.list("episode", episode);
    }
}