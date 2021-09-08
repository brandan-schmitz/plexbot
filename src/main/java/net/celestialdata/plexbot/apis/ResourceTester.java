package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.clients.models.plex.PlexUser;
import net.celestialdata.plexbot.clients.models.rdb.RdbMagnetLink;
import net.celestialdata.plexbot.clients.models.rdb.RdbTorrent;
import net.celestialdata.plexbot.clients.models.rdb.RdbUnrestrictedLink;
import net.celestialdata.plexbot.clients.models.rdb.RdbUser;
import net.celestialdata.plexbot.clients.models.sg.enums.SgQuality;
import net.celestialdata.plexbot.clients.models.sg.enums.SgStatus;
import net.celestialdata.plexbot.clients.models.sg.responses.SgFetchHistoryResponse;
import net.celestialdata.plexbot.clients.models.sg.responses.SgGetEpisodeResponse;
import net.celestialdata.plexbot.clients.models.sg.responses.SgSimpleResponse;
import net.celestialdata.plexbot.clients.models.syncthing.SyncthingCompletionResponse;
import net.celestialdata.plexbot.clients.models.tmdb.*;
import net.celestialdata.plexbot.clients.models.tvdb.TvdbLoginRequestBody;
import net.celestialdata.plexbot.clients.models.tvdb.objects.*;
import net.celestialdata.plexbot.clients.models.tvdb.responses.TvdbAuthResponse;
import net.celestialdata.plexbot.clients.models.yts.YtsMovie;
import net.celestialdata.plexbot.clients.services.*;
import net.celestialdata.plexbot.clients.utilities.SgServiceWrapper;
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

    @Inject
    SgServiceWrapper sgServiceWrapper;

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
    public TvdbSeries getSeries(@PathParam("id") Long id) {
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


    @POST
    @Path("/sg/shows")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public SgSimpleResponse addShow(@QueryParam("tvdbId") long tvdbId) {
        return sgServiceWrapper.addShow(tvdbId);
    }

    @GET
    @Path("/sg/history")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public SgFetchHistoryResponse fetchHistory() {
        return sgServiceWrapper.fetchHistory();
    }

    @GET
    @Path("/sg/episodes/{show_id}/{season}/{episode}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public SgGetEpisodeResponse getSgEpisode(@PathParam("show_id") long showTvDb, @PathParam("season") int season, @PathParam("episode") int episode) {
        return sgServiceWrapper.getEpisode(showTvDb, season, episode);
    }

    @PUT
    @Path("/sg/episodes/set_status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public SgSimpleResponse setEpisodeStatus(@QueryParam("showTvdbId") long showTvdbId, @QueryParam("season") int season, @QueryParam("episode") int episode,
                                             @QueryParam("status") SgStatus status, @QueryParam("quality") SgQuality quality) {
        return sgServiceWrapper.setEpisodeStatus(showTvdbId, season, episode, status, quality);
    }
}