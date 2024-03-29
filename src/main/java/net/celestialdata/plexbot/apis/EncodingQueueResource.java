package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.db.daos.EncodingQueueItemDao;
import net.celestialdata.plexbot.db.daos.EpisodeDao;
import net.celestialdata.plexbot.db.daos.MovieDao;
import net.celestialdata.plexbot.db.entities.EncodingQueueItem;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Tag(name = "Encoding Queue", description = "Endpoints available for the encoding workers get information about items in the encoding queue")
@Path("/api/v1/encoding/queue")
@RolesAllowed({"admin", "encoder"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EncodingQueueResource {

    @Inject
    FileUtilities fileUtilities;

    @Inject
    MovieDao movieDao;

    @Inject
    EpisodeDao episodeDao;

    @Inject
    EncodingQueueItemDao encodingQueueItemDao;

    @GET
    @Path("/next")
    public EncodingQueueItem next() {
        EncodingQueueItem item;
        boolean isOptimized;

        do {
            // Fetch the next item from the DB queue
            item = encodingQueueItemDao.getNext();

            // Fetch the media information and ensure it is not already optimized
            if (item.mediaType.equals("movie")) {
                isOptimized = fileUtilities.getMediaInfo(movieDao.getByTmdbId(item.mediaId)).isOptimized();
            } else {
                isOptimized = fileUtilities.getMediaInfo(episodeDao.getByTvdbId(item.mediaId)).isOptimized();
            }

            // If the file has been optimized, remove it from the queue
            if (isOptimized) {
                encodingQueueItemDao.delete(item.id);
            }
        } while (isOptimized);

        return item;
    }

    @GET
    @Path("/{id}")
    public EncodingQueueItem get(@PathParam("id") int id) {
        return encodingQueueItemDao.get(id);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") int id) {
        if (encodingQueueItemDao.exists(id)) {
            encodingQueueItemDao.delete(id);
        } else throw new NotFoundException();
    }
}