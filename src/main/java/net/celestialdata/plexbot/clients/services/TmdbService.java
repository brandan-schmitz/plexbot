package net.celestialdata.plexbot.clients.services;

import net.celestialdata.plexbot.clients.authorizations.TmdbAuthorizationHeaderFactory;
import net.celestialdata.plexbot.clients.models.tmdb.*;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Singleton
@RegisterRestClient(baseUri = "https://api.themoviedb.org/3")
@RegisterClientHeaders(TmdbAuthorizationHeaderFactory.class)
public interface TmdbService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tv/{tv_id}")
    TmdbShow getShow(@PathParam("tv_id") long showId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tv/{tv_id}/external_ids")
    TmdbExternalIds getShowExternalIds(@PathParam("tv_id") long showId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tv/{tv_id}/season/{season_number}/episode/{episode_number}")
    TmdbEpisode getEpisode(@PathParam("tv_id") long showId, @PathParam("season_number") int seasonNumber, @PathParam("episode_number") int episodeNumber);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tv/{tv_id}/season/{season_number}/episode/{episode_number}/external_ids")
    TmdbExternalIds getEpisodeExternalIds(@PathParam("tv_id") long showId, @PathParam("season_number") int seasonNumber, @PathParam("episode_number") int episodeNumber);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/movie/{movie_id}")
    TmdbMovie getMovie(@PathParam("movie_id") long movieId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/movie/{movie_id}/external_ids")
    TmdbExternalIds getMovieExternalIds(@PathParam("movie_id") long movieId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/find/{external_id}")
    TmdbFindResults findByExternalId(@PathParam("external_id") String externalId, @QueryParam("external_source") TmdbSourceIdType sourceIdType);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search/movie")
    TmdbMovieSearchResults searchForMovie(@QueryParam("query") String searchQuery);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search/movie")
    TmdbMovieSearchResults searchForMovie(@QueryParam("query") String searchQuery, @QueryParam("year") String year);
}