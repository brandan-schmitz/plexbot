package net.celestialdata.plexbot.clients.services;

import net.celestialdata.plexbot.clients.models.omdb.OmdbResult;
import net.celestialdata.plexbot.clients.models.omdb.OmdbSearchResponse;
import net.celestialdata.plexbot.clients.models.omdb.enums.OmdbSearchTypeEnum;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Singleton
@RegisterRestClient(baseUri = "https://www.omdbapi.com")
public interface OmdbService {

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    OmdbSearchResponse search(@QueryParam("s") String searchTerm, @QueryParam("type") OmdbSearchTypeEnum omdbSearchTypeEnum, @QueryParam("apiKey") String imdbApiKey);

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    OmdbSearchResponse search(@QueryParam("s") String searchTerm, @QueryParam("type") OmdbSearchTypeEnum omdbSearchTypeEnum, @QueryParam("y") String year, @QueryParam("apiKey") String imdbApiKey);

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    OmdbResult getById(@QueryParam("i") String imdbID, @QueryParam("apiKey") String imdbApiKey);
}