package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.entities.EncodingWorkItem;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Tag(name = "Encoding Work", description = "Endpoints available for the encoding workers get information about items being encoded")
@Path("/encoding/work")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EncodingWorkResource {

    @GET
    public List<EncodingWorkItem> get() {
        return EncodingWorkItem.listAll();
    }

    @GET
    @Path("/{id}")
    public EncodingWorkItem get(@PathParam("id") int id) {
        return EncodingWorkItem.findById(id);
    }

    @POST
    @Transactional
    public int create(EncodingWorkItem workItem) {
        workItem.persist();
        return workItem.id;
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public EncodingWorkItem update(@PathParam("id") int id, EncodingWorkItem workItem) {
        EncodingWorkItem entity = EncodingWorkItem.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }

        entity.progress = workItem.progress;
        entity.workerAgentName = workItem.workerAgentName;
        entity.type = workItem.type;
        entity.mediaId = workItem.mediaId;

        return entity;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void delete(@PathParam("id") int id) {
        EncodingWorkItem entity = EncodingWorkItem.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }
        entity.delete();
    }
}