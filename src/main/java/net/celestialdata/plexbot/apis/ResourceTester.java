package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.clients.models.plex.PlexUser;
import net.celestialdata.plexbot.clients.models.rdb.RdbMagnetLink;
import net.celestialdata.plexbot.clients.models.rdb.RdbTorrent;
import net.celestialdata.plexbot.clients.models.rdb.RdbUnrestrictedLink;
import net.celestialdata.plexbot.clients.models.rdb.RdbUser;
import net.celestialdata.plexbot.clients.models.syncthing.SyncthingCompletionResponse;
import net.celestialdata.plexbot.clients.models.tmdb.*;
import net.celestialdata.plexbot.clients.models.yts.YtsMovie;
import net.celestialdata.plexbot.clients.services.*;
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
    TmdbService tmdbService;

    @Inject
    @RestClient
    RdbService rdbService;

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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("tmdb/tv/{tv_id}")
    public TmdbShow getShow(@PathParam("tv_id") long showId) {
        return tmdbService.getShow(showId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tmdb/tv/{tv_id}/external_ids")
    public TmdbExternalIds getShowExternalIds(@PathParam("tv_id") long showId) {
        return tmdbService.getShowExternalIds(showId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tmdb/tv/{tv_id}/season/{season_number}/episode/{episode_number}")
    public TmdbEpisode getEpisode(@PathParam("tv_id") long showId, @PathParam("season_number") int seasonNumber, @PathParam("episode_number") int episodeNumber) {
        return tmdbService.getEpisode(showId, seasonNumber, episodeNumber);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tmdb/tv/{tv_id}/season/{season_number}/episode/{episode_number}/external_ids")
    public TmdbExternalIds getEpisodeExternalIds(@PathParam("tv_id") long showId, @PathParam("season_number") int seasonNumber, @PathParam("episode_number") int episodeNumber) {
        return tmdbService.getEpisodeExternalIds(showId, seasonNumber, episodeNumber);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tmdb/movie/{movie_id}")
    public TmdbMovie getMovie(@PathParam("movie_id") long movieId) {
        return tmdbService.getMovie(movieId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tmdb/movie/{movie_id}/external_ids")
    public TmdbExternalIds getMovieExternalIds(@PathParam("movie_id") long movieId) {
        return tmdbService.getMovieExternalIds(movieId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tmdb/find/{external_id}")
    public TmdbFindResults findByExternalId(@PathParam("external_id") String externalId, @QueryParam("external_source") TmdbSourceIdType sourceIdType) {
        return tmdbService.findByExternalId(externalId, sourceIdType.getValue());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tmdb/search/movie")
    public TmdbMovieSearchResults searchForMovie(@QueryParam("query") String searchQuery) {
        return tmdbService.searchForMovie(searchQuery);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tmdb/search/movie")
    public TmdbMovieSearchResults searchForMovie(@QueryParam("query") String searchQuery, @QueryParam("year") String year) {
        return tmdbService.searchForMovie(searchQuery, year);
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