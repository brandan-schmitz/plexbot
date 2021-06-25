package net.celestialdata.plexbot.clients.authorizations;

import io.quarkus.scheduler.Scheduled;
import net.celestialdata.plexbot.clients.services.PlexAuthorizationService;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@SuppressWarnings("unused")
@ApplicationScoped
public class PlexAuthorizer {

    @Inject
    @RestClient
    PlexAuthorizationService plexAuthorizationService;
    private String authToken;

    @Scheduled(every = "12h")
    void authorize() {
        authToken = plexAuthorizationService.login().authToken;
    }

    public String getAuthToken() {
        return authToken;
    }
}