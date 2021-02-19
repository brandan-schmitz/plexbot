package net.celestialdata.plexbot.client;

import net.celestialdata.plexbot.BotConfig;
import net.celestialdata.plexbot.client.api.OmdbApi;
import net.celestialdata.plexbot.client.api.PlexApi;
import net.celestialdata.plexbot.client.api.RdbApi;
import net.celestialdata.plexbot.client.api.YtsApi;

import java.util.Objects;

public class BotClient {
    private static BotClient single_instance = null;
    public final OmdbApi omdbApi;
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

        // Set the API base paths
        rdbApiClient.setBasePath("https://api.real-debrid.com/rest/1.0");
        omdbApiClient.setBasePath("http://www.omdbapi.com");
        plexApiClient.setBasePath("https://plex.tv");
        ytsApiClient.setBasePath(BotConfig.getInstance().currentYtsDomain() + "/api/v2");

        // Set API authentication values
        rdbApiClient.setRdbBearerToken(BotConfig.getInstance().realDebridKey());
        omdbApiClient.setOmdbApiKey(BotConfig.getInstance().omdbApiKey());
        plexApiClient.setPlexLogin(BotConfig.getInstance().plexUsername(), BotConfig.getInstance().plexPassword());
        plexApiClient.setPlexClientIdentifier(BotConfig.getInstance().clientIdentifier());

        // Set the plex AuthToken for accessing the plex server
        try {
            plexApiClient.setPlexAuthToken(Objects.requireNonNull(new PlexApi(plexApiClient).signIn().getUser()).getAuthToken());
        } catch (Exception e) {
            System.out.println("\n\nERROR: Unable to login to Plex API. Please check your credentials " +
                    "in the config.yaml file and try again.\nError:");
            e.printStackTrace();
            System.exit(1);
        }

        // Create the individual api clients
        rdbApi = new RdbApi(rdbApiClient);
        omdbApi = new OmdbApi(omdbApiClient);
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