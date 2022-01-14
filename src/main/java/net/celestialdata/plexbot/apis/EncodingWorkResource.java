package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.db.daos.EncodingWorkItemDao;
import net.celestialdata.plexbot.db.entities.EncodingWorkItem;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Tag(name = "Encoding Work", description = "Endpoints available for the encoding workers get information about items being encoded")
@Path("/api/v1/encoding/work")
@RolesAllowed({"admin", "encoder"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EncodingWorkResource {

    @Inject
    EncodingWorkItemDao encodingWorkItemDao;

    @GET
    public List<EncodingWorkItem> get() {
        return encodingWorkItemDao.listALl();
    }

    @GET
    @Path("/{id}")
    public EncodingWorkItem get(@PathParam("id") int id) {
        if (encodingWorkItemDao.exists(id)) {
            return encodingWorkItemDao.get(id);
        } else {
            throw new NotFoundException();
        }
    }

    @POST
    public int create(EncodingWorkItem workItem) {
        return encodingWorkItemDao.create(workItem).id;
    }

    @PUT
    @Path("/{id}")
    public EncodingWorkItem update(@PathParam("id") int id, @QueryParam("progress") String progress) {
        if (encodingWorkItemDao.exists(id)) {
            return encodingWorkItemDao.update(id, progress);
        } else {
            throw new NotFoundException();
        }
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") int id) {
        if (encodingWorkItemDao.exists(id)) {
            encodingWorkItemDao.delete(id);
        } else {
            throw new NotFoundException();
        }
    }
}