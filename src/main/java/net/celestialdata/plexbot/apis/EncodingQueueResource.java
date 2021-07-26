package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.entities.EncodingQueueItem;
import net.celestialdata.plexbot.entities.EntityUtilities;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Tag(name = "Encoding Queue", description = "Endpoints available for the encoding workers get information about items in the encoding queue")
@Path("/encoding/queue")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EncodingQueueResource {

    @Inject
    EntityUtilities entityUtilities;

    @Inject
    FileUtilities fileUtilities;

    @GET
    @Path("/next")
    @Transactional
    public EncodingQueueItem next() {
        EncodingQueueItem item;
        boolean isOptimized;

        do {
            // Fetch the next item from the DB queue
            item = (EncodingQueueItem) EncodingQueueItem.listAll().get(0);

            // Fetch the media information and ensure it is not already optimized
            if (item.type.equals("movie")) {
                isOptimized = fileUtilities.getMediaInfo(entityUtilities.getMovie(item.mediaId)).isOptimized();
            } else {
                isOptimized = fileUtilities.getMediaInfo(entityUtilities.getEpisode(item.mediaId)).isOptimized();
            }

            // If the file has been optimized, remove it from the queue
            if (isOptimized) {
                item.delete();
            }
        } while (isOptimized);

        return item;
    }

    @GET
    @Path("/{id}")
    public EncodingQueueItem get(@PathParam("id") int id) {
        return EncodingQueueItem.findById(id);
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void delete(@PathParam("id") int id) {
        EncodingQueueItem entity = EncodingQueueItem.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }
        entity.delete();
    }
}