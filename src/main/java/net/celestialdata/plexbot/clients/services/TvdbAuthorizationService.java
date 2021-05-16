package net.celestialdata.plexbot.clients.services;

import net.celestialdata.plexbot.clients.authorizations.TvdbAuthorizationHeaderFactory;
import net.celestialdata.plexbot.clients.models.tvdb.TvdbLoginRequestBody;
import net.celestialdata.plexbot.clients.models.tvdb.responses.TvdbAuthResponse;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Singleton
@RegisterRestClient(baseUri = "https://api4.thetvdb.com/v4")
@RegisterClientHeaders(TvdbAuthorizationHeaderFactory.class)
public interface TvdbAuthorizationService {

    @POST
    @Path("/login")
    TvdbAuthResponse login(TvdbLoginRequestBody loginBody);
}