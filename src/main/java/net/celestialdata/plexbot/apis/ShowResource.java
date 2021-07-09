package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.entities.Show;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Tag(name = "Shows", description = "Endpoints available for fetching information about shows in the database")
@Path("/shows")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShowResource {

    @GET
    @Path("/{id}")
    public Show get(@PathParam("id") String id) {
        return Show.findById(id);
    }
}