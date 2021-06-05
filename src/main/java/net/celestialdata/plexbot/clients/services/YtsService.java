package net.celestialdata.plexbot.clients.services;

import net.celestialdata.plexbot.clients.models.yts.YtsResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Singleton
@RegisterRestClient(baseUri = "https://yts.mx/api/v2")
public interface YtsService {

    @GET
    @Path(value = "/list_movies.json")
    @Produces(MediaType.APPLICATION_JSON)
    YtsResponse search(@QueryParam("query_term") String imdbID);
}