package net.celestialdata.plexbot.clients.services;

import net.celestialdata.plexbot.clients.models.omdb.OmdbResult;
import net.celestialdata.plexbot.clients.models.omdb.OmdbSearchResponse;
import net.celestialdata.plexbot.clients.models.omdb.OmdbSearchType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Singleton
@RegisterRestClient(baseUri = "http://www.omdbapi.com")
public interface OmdbService {

    @GET
    @Path("/")
    @Produces("application/json")
    OmdbSearchResponse search(@QueryParam("s") String searchTerm, @QueryParam("type") OmdbSearchType omdbSearchType, @QueryParam("apiKey") String imdbApiKey);

    @GET
    @Path("/")
    @Produces("application/json")
    OmdbSearchResponse search(@QueryParam("s") String searchTerm, @QueryParam("type") OmdbSearchType omdbSearchType, @QueryParam("y") String year, @QueryParam("apiKey") String imdbApiKey);

    @GET
    @Path("/")
    @Produces("application/json")
    OmdbResult getById(@QueryParam("i") String imdbID, @QueryParam("apiKey") String imdbApiKey);
}