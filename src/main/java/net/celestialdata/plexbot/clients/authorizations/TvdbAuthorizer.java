package net.celestialdata.plexbot.clients.authorizations;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import net.celestialdata.plexbot.clients.models.tvdb.TvdbLoginRequestBody;
import net.celestialdata.plexbot.clients.services.TvdbAuthorizationService;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class TvdbAuthorizer {
    @Inject
    @RestClient
    TvdbAuthorizationService tvdbAuthorizationService;
    private String authToken;

    void startup(@Observes StartupEvent event) {
        authorize();
    }

    @Scheduled(every = "12h")
    void authorize() {
        // Fetch the token and pin from the configuration
        TvdbLoginRequestBody loginBody = new TvdbLoginRequestBody();
        loginBody.apiKey = ConfigProvider.getConfig().getValue("ApiKeys.tvdbApiKey", String.class);
        loginBody.pin = ConfigProvider.getConfig().getValue("ApiKeys.tvdbSubscriberPin", String.class);

        // Login to the API and get the auth token
        authToken = tvdbAuthorizationService.login(loginBody).data.token;
    }

    public String getAuthToken() {
        return authToken;
    }
}