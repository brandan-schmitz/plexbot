package net.celestialdata.plexbot.client;

import net.celestialdata.plexbot.client.api.OmDbApi;
import net.celestialdata.plexbot.client.api.PlexApi;
import net.celestialdata.plexbot.client.api.RdbApi;
import net.celestialdata.plexbot.client.api.YtsApi;
import net.celestialdata.plexbot.config.ConfigProvider;

public class BotClient {
    private static BotClient single_instance = null;
    public final OmDbApi omdbApi;
    public final YtsApi ytsApi;
    public final RdbApi rdbApi;
    @SuppressWarnings("unused")
    public final PlexApi plexApi;

    /**
     * Construct an instance of the BotClient which contains all the necessary API clients
     * to make the bot function.
     */
    private BotClient() {
        // Create the ApiClient
        ApiClient rdbApiClient = new ApiClient();
        ApiClient omdbApiClient = new ApiClient();
        ApiClient plexApiClient = new ApiClient();
        ApiClient ytsApiClient = new ApiClient();

        // Set the Real-Debrid access token and base path
        rdbApiClient.setAccessToken(ConfigProvider.API_KEYS.realDebridKey());
        rdbApiClient.setBasePath("https://api.real-debrid.com/rest/1.0");

        // Set the OmdbAPI Key and base path
        omdbApiClient.setOmdbApiKey(ConfigProvider.API_KEYS.omdbApiKey());
        omdbApiClient.setBasePath("http://www.omdbapi.com");

        // Set the plex.tv account login and base path and client identifier
        plexApiClient.setPlexLogin(ConfigProvider.PLEX_SERVER_SETTINGS.username(),
                ConfigProvider.PLEX_SERVER_SETTINGS.password());
        plexApiClient.setBasePath("https://plex.tv");
        plexApiClient.setPlexClientIdentifier(ConfigProvider.PLEX_SERVER_SETTINGS.clientIdentifier());

        // Set the plex AuthToken for accessing the plex server
        try {
            plexApiClient.setPlexAuthToken(new PlexApi(plexApiClient).signIn().getUser().getAuthToken());
        } catch (Exception e) {
            System.out.println("\n\nERROR: Unable to login to Plex API. Please check your credentials " +
                    "in the config.yaml file and try again.\nError:");
            e.printStackTrace();
            System.exit(1);
        }

        // Set the yts api base path
        ytsApiClient.setBasePath(ConfigProvider.BOT_SETTINGS.currentYtsDomain() + "/api/v2");

        // Create the individual api clients
        rdbApi = new RdbApi(rdbApiClient);
        omdbApi = new OmDbApi(omdbApiClient);
        plexApi = new PlexApi(plexApiClient);
        ytsApi = new YtsApi(ytsApiClient);
    }

    /**
     * Return an instance of the BotClient
     *
     * @return a BotClient instance
     */
    public static BotClient getInstance() {
        if (single_instance == null) {
            single_instance = new BotClient();
        }

        return single_instance;
    }

    /**
     * Reinitialize the BotClient instance. This is useful for if there is an issue with authentication
     * tokens that need to be refreshed.
     */
    public static void refreshClient() {
        single_instance = new BotClient();
    }
}