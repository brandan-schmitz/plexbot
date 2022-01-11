package net.celestialdata.plexbot.clients.authorizations;

import io.quarkus.scheduler.Scheduled;
import net.celestialdata.plexbot.clients.services.PlexAuthorizationService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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

    @ConfigProperty(name = "PlexSettings.enabled")
    boolean plexEnabled;

    @Scheduled(every = "12h")
    void authorize() {
        if (plexEnabled) {
            authToken = plexAuthorizationService.login().authToken;
        }
    }

    public String getAuthToken() {
        return authToken;
    }
}