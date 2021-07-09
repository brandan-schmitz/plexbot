package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.entities.EncodingWorkItem;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Tag(name = "Encoding Work", description = "Endpoints available for the encoding workers get information about items being encoded")
@Path("/encoding")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EncodingWorkResource {

    @GET
    @Path("/work/{id}")
    public EncodingWorkItem get(@PathParam("id") int id) {
        return EncodingWorkItem.findById(id);
    }

    @POST
    @Path("/work")
    @Transactional
    public Response create(EncodingWorkItem workItem) {
        workItem.persist();
        return Response.created(URI.create("/encoding/queue/" + workItem.id)).build();
    }

    @PUT
    @Path("/work/{id}")
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
    @Path("/work/{id}")
    @Transactional
    public void delete(@PathParam("id") int id) {
        EncodingWorkItem entity = EncodingWorkItem.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }
        entity.delete();
    }
}