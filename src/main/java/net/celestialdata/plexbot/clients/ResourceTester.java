package net.celestialdata.plexbot.clients;

import net.celestialdata.plexbot.clients.models.omdb.OmdbResult;
import net.celestialdata.plexbot.clients.models.omdb.OmdbSearchResponse;
import net.celestialdata.plexbot.clients.models.omdb.OmdbSearchType;
import net.celestialdata.plexbot.clients.models.rdb.RdbMagnetLink;
import net.celestialdata.plexbot.clients.models.rdb.RdbTorrent;
import net.celestialdata.plexbot.clients.models.rdb.RdbUnrestrictedLink;
import net.celestialdata.plexbot.clients.models.rdb.RdbUser;
import net.celestialdata.plexbot.clients.services.OmdbService;
import net.celestialdata.plexbot.clients.services.RdbService;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.*;

@Tag(name = "Test", description = "Endpoints available to test the API functions.")
@Path("/test")
public class ResourceTester {

    @Inject
    @RestClient
    OmdbService omdbService;

    @Inject
    @RestClient
    RdbService rdbService;

    @GET
    @Path("/imdb/search")
    @Produces("application/json")
    public OmdbSearchResponse search(@QueryParam("query") String query, @QueryParam("type") OmdbSearchType searchType) {
        return omdbService.search(query, searchType, ConfigProvider.getConfig().getValue("ApiKeys.omdbApiKey", String.class));
    }

    @GET
    @Path("/imdb/lookup/{id}")
    @Produces("application/json")
    public OmdbResult getById(@PathParam("id") String imdbID) {
        return omdbService.getById(imdbID, ConfigProvider.getConfig().getValue("ApiKeys.omdbApiKey", String.class));
    }

    @GET
    @Path("/rdb/user")
    @Produces("application/json")
    public RdbUser getUser() {
        return rdbService.getUser();
    }

    @POST
    @Path("/rdb/torrents/addMagnet")
    @Produces("application/json")
    @Consumes("application/x-www-form-urlencoded")
    public RdbMagnetLink addMagnet(@FormParam("magnet") String magnet) {

        System.out.println("Test");
        System.out.println(magnet);

        var returnValue = rdbService.addMagnet(magnet);

        System.out.println(returnValue.toString());

        return returnValue;
    }

    @GET
    @Path("/rdb/torrents/info/{id}")
    @Produces("application/json")
    public RdbTorrent getTorrentInfo(@PathParam("id") String id) {
        return rdbService.getTorrentInfo(id);
    }

    @POST
    @Path("/rdb/torrents/selectFiles{id}")
    @Produces("application/json")
    @Consumes("application/x-www-form-urlencoded")
    public void selectFiles(@PathParam("id") String id, @FormParam("files") String fileList) {
        rdbService.selectFiles(id, fileList);
    }

    @DELETE
    @Path("/rdb/torrents/delete/{id}")
    @Produces("application/json")
    public void deleteTorrent(@PathParam("id") String id) {
        rdbService.deleteTorrent(id);
    }

    @POST
    @Path("/rdb/unrestrict/link")
    @Produces("application/json")
    @Consumes("application/x-www-form-urlencoded")
    public RdbUnrestrictedLink unrestrictLink(@FormParam("link") String link) {
        return rdbService.unrestrictLink(link);
    }
}