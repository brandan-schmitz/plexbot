package net.celestialdata.plexbot.clients.services;

import net.celestialdata.plexbot.clients.authorizations.TvdbAuthorizationHeaderFactory;
import net.celestialdata.plexbot.clients.models.tvdb.TvdbLoginRequestBody;
import net.celestialdata.plexbot.clients.models.tvdb.responses.TvdbAuthResponse;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Singleton
@RegisterRestClient(baseUri = "https://api4.thetvdb.com/v4")
@RegisterClientHeaders(TvdbAuthorizationHeaderFactory.class)
public interface TvdbAuthorizationService {

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    TvdbAuthResponse login(TvdbLoginRequestBody loginBody);
}