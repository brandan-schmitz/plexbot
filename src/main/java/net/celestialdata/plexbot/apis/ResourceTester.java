package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.clients.models.omdb.OmdbResult;
import net.celestialdata.plexbot.clients.models.omdb.OmdbSearchResponse;
import net.celestialdata.plexbot.clients.models.omdb.enums.OmdbSearchTypeEnum;
import net.celestialdata.plexbot.clients.models.plex.PlexUser;
import net.celestialdata.plexbot.clients.models.rdb.RdbMagnetLink;
import net.celestialdata.plexbot.clients.models.rdb.RdbTorrent;
import net.celestialdata.plexbot.clients.models.rdb.RdbUnrestrictedLink;
import net.celestialdata.plexbot.clients.models.rdb.RdbUser;
import net.celestialdata.plexbot.clients.models.syncthing.SyncthingCompletionResponse;
import net.celestialdata.plexbot.clients.models.tvdb.TvdbLoginRequestBody;
import net.celestialdata.plexbot.clients.models.tvdb.objects.*;
import net.celestialdata.plexbot.clients.models.tvdb.responses.TvdbAuthResponse;
import net.celestialdata.plexbot.clients.models.yts.YtsMovie;
import net.celestialdata.plexbot.clients.services.*;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Tag(name = "Test", description = "Endpoints available to test the API functions.")
@Path("/test")
public class ResourceTester {

    @Inject
    @RestClient
    OmdbService omdbService;

    @Inject
    @RestClient
    RdbService rdbService;

    @Inject
    @RestClient
    TvdbAuthorizationService tvdbAuthorizationService;

    @Inject
    @RestClient
    TvdbService tvdbService;

    @Inject
    @RestClient
    SyncthingService syncthingService;

    @Inject
    @RestClient
    PlexAuthorizationService plexAuthorizationService;

    @Inject
    @RestClient
    PlexService plexService;

    @Inject
    @RestClient
    YtsService ytsService;

    @GET
    @Path("/imdb/search")
    @Produces("application/json")
    public OmdbSearchResponse search(@QueryParam("query") String query, @QueryParam("type") OmdbSearchTypeEnum searchType) {
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
        return rdbService.addMagnet(magnet);
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

    @POST
    @Path("/tvdb/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TvdbAuthResponse login(TvdbLoginRequestBody loginBody) {
        return tvdbAuthorizationService.login(loginBody);
    }

    @GET
    @Produces("application/json")
    @Path("/tvdb/episodes/{id}")
    public TvdbEpisode getEpisode(@PathParam("id") String id) {
        return tvdbService.getEpisode(id).episode;
    }

    @GET
    @Produces("application/json")
    @Path("/tvdb/episodes/{id}/extended")
    public TvdbExtendedEpisode getExtendedEpisode(@PathParam("id") String id) {
        return tvdbService.getExtendedEpisode(id).extendedEpisode;
    }

    @GET
    @Produces("application/json")
    @Path("/tvdb/movies/{id}")
    public TvdbMovie getMovie(@PathParam("id") String id) {
        return tvdbService.getMovie(id).movie;
    }

    @GET
    @Produces("application/json")
    @Path("/tvdb/movies/{id}/extended")
    public TvdbExtendedMovie getExtendedMovie(@PathParam("id") String id) {
        return tvdbService.getExtendedMovie(id).extendedMovie;
    }

    @GET
    @Produces("application/json")
    @Path("/tvdb/seasons/{id}")
    public TvdbSeason getSeason(@PathParam("id") String id) {
        return tvdbService.getSeason(id).season;
    }

    @GET
    @Produces("application/json")
    @Path("/tvdb/seasons/{id}/extended")
    public TvdbExtendedSeason getExtendedSeason(@PathParam("id") String id) {
        return tvdbService.getExtendedSeason(id).extendedSeason;
    }

    @GET
    @Produces("application/json")
    @Path("/tvdb/series/{id}")
    public TvdbSeries getSeries(@PathParam("id") String id) {
        return tvdbService.getSeries(id).series;
    }

    @GET
    @Produces("application/json")
    @Path("/tvdb/series/{id}/extended")
    public TvdbExtendedSeries getExtendedSeries(@PathParam("id") String id) {
        return tvdbService.getExtendedSeries(id).extendedSeries;
    }

    @GET
    @Produces("application/json")
    @Path("/tvdb/series/{id}/episodes/official")
    public List<TvdbEpisode> getSeriesEpisodes(@PathParam("id") String id) {
        return tvdbService.getSeriesEpisodes(id).seriesEpisodes.episodes;
    }

    @GET
    @Path(value = "/syncthing/db/completion")
    @Produces(MediaType.APPLICATION_JSON)
    public SyncthingCompletionResponse getCompletionStatus(@QueryParam(value = "folder") String folderName, @QueryParam(value = "device") String deviceID) {
        return syncthingService.getCompletionStatus(folderName, deviceID);
    }

    @POST
    @Path("/plex/users/sign_in.json")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PlexUser login() {
        return plexAuthorizationService.login();
    }


    @SuppressWarnings("QsUndeclaredPathMimeTypesInspection")
    @POST
    @Path("/plex/library/sections/all/refresh")
    public void refreshLibraries() {
        plexService.refreshLibraries();
    }

    @GET
    @Path(value = "/yts/list_movies.json")
    @Produces(MediaType.APPLICATION_JSON)
    public List<YtsMovie> search(@QueryParam("query_term") String imdbID) {
        return ytsService.search(imdbID).results.movies;
    }
}