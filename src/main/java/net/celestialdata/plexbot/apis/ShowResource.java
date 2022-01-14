package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.db.daos.ShowDao;
import net.celestialdata.plexbot.db.entities.Show;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Tag(name = "Shows", description = "Endpoints available for fetching information about shows in the database")
@Path("/api/v1/shows")
@RolesAllowed({"admin", "encoder", "user"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShowResource {

    @Inject
    ShowDao showDao;

    @GET
    @Path("/{tvdb_id}")
    public Show get(@PathParam("tvdb_id") long tvdbId) {
        return showDao.getByTvdbId(tvdbId);
    }
}