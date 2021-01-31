package net.celestialdata.plexbot.client;

import io.quarkus.runtime.Quarkus;
import net.celestialdata.plexbot.client.api.OmDbApi;
import net.celestialdata.plexbot.client.api.PlexApi;
import net.celestialdata.plexbot.client.api.RdbApi;
import net.celestialdata.plexbot.client.api.YtsApi;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import javax.enterprise.context.Dependent;

@SuppressWarnings("unused")
@Dependent
public class BotClient {
    private final Logger LOGGER = Logger.getLogger("Plexbot Logger");
    public OmDbApi omdbApi;
    public YtsApi ytsApi;
    public RdbApi rdbApi;
    public PlexApi plexApi;

    /**
     * Construct an instance of the BotClient which contains all the necessary API clients
     * to make the bot function.
     */
    private BotClient() {
        initialize();
    }

    private void initialize() {
        // Create the ApiClient
        ApiClient rdbApiClient = new ApiClient();
        ApiClient omdbApiClient = new ApiClient();
        ApiClient plexApiClient = new ApiClient();
        ApiClient ytsApiClient = new ApiClient();

        // Set the Real-Debrid access token and base path
        rdbApiClient.setAccessToken(ConfigProvider.getConfig().getValue("apiKeys.rdb", String.class));
        rdbApiClient.setBasePath("https://api.real-debrid.com/rest/1.0");

        // Set the OmdbAPI Key and base path
        omdbApiClient.setOmdbApiKey(ConfigProvider.getConfig().getValue("apiKeys.omdb", String.class));
        omdbApiClient.setBasePath("http://www.omdbapi.com");

        // Set the plex.tv account login and base path and client identifier
        plexApiClient.setPlexLogin(ConfigProvider.getConfig().getValue("plex.username", String.class),
                ConfigProvider.getConfig().getValue("plex.password", String.class));
        plexApiClient.setBasePath("https://plex.tv");
        plexApiClient.setPlexClientIdentifier(ConfigProvider.getConfig().getValue("plex.clientIdentifier", String.class));

        // Set the plex AuthToken for accessing the plex server
        try {
            plexApiClient.setPlexAuthToken(new PlexApi(plexApiClient).signIn().getUser().getAuthToken());
        } catch (Exception e) {
            LOGGER.error(e);
            Quarkus.asyncExit(1);
        }

        // Set the yts api base path
        ytsApiClient.setBasePath(ConfigProvider.getConfig().getValue("bot.ytsUrl", String.class) + "/api/v2");

        // Create the individual api clients
        rdbApi = new RdbApi(rdbApiClient);
        omdbApi = new OmDbApi(omdbApiClient);
        plexApi = new PlexApi(plexApiClient);
        ytsApi = new YtsApi(ytsApiClient);
    }

    /**
     * Reinitialize the BotClient instance. This is useful for if there is an issue with authentication
     * tokens that need to be refreshed.
     */
    public void refreshClient() {
        initialize();
    }
}