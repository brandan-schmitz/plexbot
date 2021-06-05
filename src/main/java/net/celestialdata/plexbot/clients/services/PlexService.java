package net.celestialdata.plexbot.clients.services;

import net.celestialdata.plexbot.clients.authorizations.PlexAuthorizationHeaderFactory;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@RegisterRestClient(configKey = "PlexSettings.address")
@RegisterClientHeaders(PlexAuthorizationHeaderFactory.class)
public interface PlexService {

    @SuppressWarnings("VoidMethodAnnotatedWithGET")
    @GET
    @Path("/library/sections/all/refresh")
    void refreshLibraries();
}