package net.celestialdata.plexbot.apis;

import io.quarkus.panache.common.Sort;
import net.celestialdata.plexbot.entities.EncodingQueueItem;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Tag(name = "Encoding Queue", description = "Endpoints available for the encoding workers get information about items in the encoding queue")
@Path("/encoding")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EncodingQueueResource {

    @GET
    @Path("/queue/next")
    public EncodingQueueItem next() {
        return (EncodingQueueItem) EncodingQueueItem.listAll(Sort.by("mediaId").descending()).get(0);
    }

    @GET
    @Path("/queue/{id}")
    public EncodingQueueItem get(@PathParam("id") int id) {
        return EncodingQueueItem.findById(id);
    }

    @DELETE
    @Path("/queue/{id}")
    @Transactional
    public void delete(@PathParam("id") int id) {
        EncodingQueueItem entity = EncodingQueueItem.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }
        entity.delete();
    }
}