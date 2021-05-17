package net.celestialdata.plexbot.clients.services;

import net.celestialdata.plexbot.clients.authorizations.PlexAuthorizationHeaderFactory;
import net.celestialdata.plexbot.clients.models.plex.PlexUser;
import net.celestialdata.plexbot.utilities.UnwrappingObjectMapper;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Singleton
@RegisterRestClient(baseUri = "https://plex.tv")
@RegisterClientHeaders(PlexAuthorizationHeaderFactory.class)
@RegisterProvider(UnwrappingObjectMapper.class)
public interface PlexAuthorizationService {

    @POST
    @Retry
    @Path("/users/sign_in.json")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    PlexUser login();
}