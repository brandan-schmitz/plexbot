package net.celestialdata.plexbot.clients.services;

import net.celestialdata.plexbot.clients.authorizations.RDBAuthorizationHeaderFactory;
import net.celestialdata.plexbot.clients.models.rdb.RdbAddedTorrent;
import net.celestialdata.plexbot.clients.models.rdb.RdbTorrent;
import net.celestialdata.plexbot.clients.models.rdb.RdbUnrestrictedLink;
import net.celestialdata.plexbot.clients.models.rdb.RdbUser;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

@Singleton
@RegisterRestClient(baseUri = "https://api.real-debrid.com/rest/1.0")
@RegisterClientHeaders(RDBAuthorizationHeaderFactory.class)
public interface RdbService {

    @POST
    @Path("/torrents/addMagnet")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    RdbAddedTorrent addMagnet(@FormParam("magnet") String magnet);

    @PUT
    @Path("/torrents/addTorrent")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/x-bittorrent")
    RdbAddedTorrent addTorrent(@RequestBody(description = "Torrent file to upload") InputStream file);

    @POST
    @Path("/torrents/selectFiles/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    void selectFiles(@PathParam("id") String id, @FormParam("files") String fileList);

    @GET
    @Path("/torrents/info/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    RdbTorrent getTorrentInfo(@PathParam("id") String id);

    @DELETE
    @Path("/torrents/delete/{id}")
    void deleteTorrent(@PathParam("id") String id);

    @POST
    @Path("/unrestrict/link")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    RdbUnrestrictedLink unrestrictLink(@FormParam("link") String link);

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    RdbUser getUser();
}