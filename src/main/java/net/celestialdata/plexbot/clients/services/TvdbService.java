package net.celestialdata.plexbot.clients.services;

import net.celestialdata.plexbot.clients.authorizations.TvdbAuthorizationHeaderFactory;
import net.celestialdata.plexbot.clients.models.tvdb.responses.*;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(baseUri = "https://api4.thetvdb.com/v4")
@RegisterClientHeaders(TvdbAuthorizationHeaderFactory.class)
public interface TvdbService {

    @GET
    @Produces("application/json")
    @Path("/episodes/{id}")
    TvdbEpisodeResponse getEpisode(@PathParam("id") String id);

    @GET
    @Produces("application/json")
    @Path("/episodes/{id}/extended")
    TvdbExtendedEpisodeResponse getExtendedEpisode(@PathParam("id") String id);

    @GET
    @Produces("application/json")
    @Path("/movies/{id}")
    TvdbMovieResponse getMovie(@PathParam("id") String id);

    @GET
    @Produces("application/json")
    @Path("/movies/{id}/extended")
    TvdbExtendedMovieResponse getExtendedMovie(@PathParam("id") String id);

    @GET
    @Produces("application/json")
    @Path("/seasons/{id}")
    TvdbSeasonResponse getSeason(@PathParam("id") String id);

    @GET
    @Produces("application/json")
    @Path("/seasons/{id}/extended")
    TvdbExtendedSeasonResponse getExtendedSeason(@PathParam("id") String id);

    @GET
    @Produces("application/json")
    @Path("/series/{id}")
    TvdbSeriesResponse getSeries(@PathParam("id") long id);

    @GET
    @Produces("application/json")
    @Path("/series/{id}/extended")
    TvdbExtendedSeriesResponse getExtendedSeries(@PathParam("id") String id);

    @GET
    @Produces("application/json")
    @Path("/series/{id}/episodes/official")
    TvdbSeriesEpisodesResponse getSeriesEpisodes(@PathParam("id") String id);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search")
    TvdbSearchResponse search(@QueryParam("q") String searchQuery, @QueryParam("type") String mediaType);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search")
    TvdbSearchResponse search(@QueryParam("q") String searchQuery, @QueryParam("type") String mediaType, @QueryParam("year") String year);
}