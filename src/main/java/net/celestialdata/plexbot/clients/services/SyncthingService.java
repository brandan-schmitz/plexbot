package net.celestialdata.plexbot.clients.services;

import net.celestialdata.plexbot.clients.authorizations.SyncthingAuthorizationHeaderFactory;
import net.celestialdata.plexbot.clients.models.syncthing.SyncthingCompletionResponse;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Singleton
@RegisterRestClient(configKey = "SyncthingSettings.address")
@RegisterClientHeaders(SyncthingAuthorizationHeaderFactory.class)
public interface SyncthingService {

    @GET
    @Path(value = "/rest/db/completion")
    @Produces(MediaType.APPLICATION_JSON)
    SyncthingCompletionResponse getCompletionStatus(@QueryParam(value = "folder") String folderName, @QueryParam(value = "device") String deviceID);
}