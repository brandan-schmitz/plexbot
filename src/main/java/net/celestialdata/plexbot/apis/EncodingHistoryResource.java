package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.db.entities.EncodingHistoryItem;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Tag(name = "Encoding History", description = "Endpoints available for the encoding workers save the history or result of an encoding job once done.")
@Path("/api/v1/encoding/history")
@RolesAllowed({"admin", "encoder"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EncodingHistoryResource {

    @POST
    @Transactional
    public int create(EncodingHistoryItem historyItem) {
        EncodingHistoryItem item = new EncodingHistoryItem();
        item.mediaId = historyItem.mediaId;
        item.mediaType = historyItem.mediaType;
        item.encodingAgent = historyItem.encodingAgent;
        item.status = historyItem.status;
        item.persist();

        return item.id;
    }
}