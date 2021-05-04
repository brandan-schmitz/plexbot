package net.celestialdata.plexbot.client;

import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.client.api.*;
import net.celestialdata.plexbot.client.model.AuthToken;
import net.celestialdata.plexbot.client.model.LoginBody;
import net.celestialdata.plexbot.configuration.BotConfig;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BotClient {
    private static BotClient single_instance = null;
    public final OmdbApi omdbApi;
    public final YtsApi ytsApi;
    public final RdbApi rdbApi;
    public final SyncthingApi syncthingApi;
    private final List<PlexServerApi> plexServerClients;
    public final TvdbApi tvdbApi;

    /**
     * Construct an instance of the BotClient which contains all the necessary API clients
     * to make the bot function.
     */
    private BotClient() {
        // Create the ApiClient
        ApiClient rdbApiClient = new ApiClient();
        ApiClient omdbApiClient = new ApiClient();
        ApiClient ytsApiClient = new ApiClient();
        ApiClient syncthingApiClient = new ApiClient();
        ApiClient tvdbApiClient = new ApiClient();

        // Set the API base paths
        rdbApiClient.setBasePath("https://api.real-debrid.com/rest/1.0");
        omdbApiClient.setBasePath("http://www.omdbapi.com");
        ytsApiClient.setBasePath(BotConfig.getInstance().currentYtsDomain() + "/api/v2");
        syncthingApiClient.setBasePath(BotConfig.getInstance().syncthingAddress() + "/rest");
        tvdbApiClient.setBasePath("https://api4.thetvdb.com/v4");

        // Login to TheTVDB API
        TvdbApi tempApi = new TvdbApi(tvdbApiClient);
        AuthToken tvdbAuthToken = null;
        try {
            tvdbAuthToken = tempApi.login(new LoginBody()
                    .apikey(BotConfig.getInstance().tvdbApiKey())
                    .pin(BotConfig.getInstance().tvdbSubscriberPin())).getData();
        } catch (ApiException e) {
            System.out.println("\n\nERROR: Unable to login to TheTVDB API. Please check your credentials and try again.");
            System.exit(1);
        }

        // Set API authentication values
        rdbApiClient.setRdbBearerToken(BotConfig.getInstance().realDebridKey());
        omdbApiClient.setOmdbApiKey(BotConfig.getInstance().omdbApiKey());
        syncthingApiClient.setSyncthingApiKey(BotConfig.getInstance().syncthingApiKey());
        if (tvdbAuthToken != null) {
            tvdbApiClient.setTvdbToken(tvdbAuthToken.getToken());
        } else {
            System.out.println("\n\nERROR: Unable to login to TheTVDB API. Please check your credentials and try again.");
            System.exit(1);
        }

        // Create the individual api clients
        rdbApi = new RdbApi(rdbApiClient);
        omdbApi = new OmdbApi(omdbApiClient);
        ytsApi = new YtsApi(ytsApiClient);
        syncthingApi = new SyncthingApi(syncthingApiClient);
        tvdbApi = new TvdbApi(tvdbApiClient);

        // Configure the Plex API clients
        plexServerClients = new ArrayList<>();
        BotConfig.getInstance().plexServers().forEach(plexServer -> {
            // Create the ApiClients
            ApiClient plexApiClient = new ApiClient();
            ApiClient plexServerApiClient = new ApiClient();

            // Set the API base paths
            plexApiClient.setBasePath("https://plex.tv");
            plexServerApiClient.setBasePath("http://" + plexServer.getAddress() + ":" + plexServer.getPort());

            // Set the client identifier
            plexApiClient.setPlexClientIdentifier(BotConfig.getInstance().clientIdentifier());
            plexServerApiClient.setPlexClientIdentifier(BotConfig.getInstance().clientIdentifier());

            // Set the product header
            plexApiClient.addDefaultHeader("X-Plex-Product", "Plexbot for Discord");
            plexServerApiClient.addDefaultHeader("X-Plex-Product", "Plexbot for Discord");

            // Set the version header
            plexApiClient.addDefaultHeader("X-Plex-Version", Main.getVersion());
            plexServerApiClient.addDefaultHeader("X-Plex-Version", Main.getVersion());

            // Set the device header
            plexApiClient.addDefaultHeader("X-Plex-Device", System.getProperty("os.name"));
            plexServerApiClient.addDefaultHeader("X-Plex-Device", System.getProperty("os.name"));

            // Set the device name header
            plexApiClient.addDefaultHeader("X-Plex-Device-Name", "Plexbot");
            plexServerApiClient.addDefaultHeader("X-Plex-Device-Name", "Plexbot");

            // Set the platform header
            plexApiClient.addDefaultHeader("X-Plex-Platform", System.getProperty("os.name"));
            plexServerApiClient.addDefaultHeader("X-Plex-Platform", System.getProperty("os.name"));

            // Set the platform version header
            plexApiClient.addDefaultHeader("X-Plex-Platform-Version", System.getProperty("os.version"));
            plexServerApiClient.addDefaultHeader("X-Plex-Platform-Version", System.getProperty("os.version"));

            // Set the login to plex.tv
            plexApiClient.setPlexLogin(plexServer.getUsername(), plexServer.getPassword());

            // Set the plex AuthToken for accessing the plex server
            try {
                plexServerApiClient.setPlexAuthToken(Objects.requireNonNull(new PlexApi(plexApiClient).signIn().getUser()).getAuthToken());
            } catch (Exception ignored) {
                System.out.println("\n\nERROR: Unable to login to Plex API. Please check your credentials defined for the " +
                        "following server in the config.yaml file and try again.\nServer: " + plexServer.getAddress());
                System.exit(1);
            }

            // Add the logged in API client to the list of plex server clients
            plexServerClients.add(new PlexServerApi(plexServerApiClient));
        });
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

    public void refreshPlexServers() {
        plexServerClients.forEach(client -> {
            try {
                client.refreshLibraries();
            } catch (ApiException e) {
                new MessageBuilder()
                        .append("An error has occurred while performing the following task:", MessageDecoration.BOLD)
                        .appendCode("", "Refresh Plex Server: " + client.getApiClient().getBasePath())
                        .appendCode("java", ExceptionUtils.getMessage(e))
                        .appendCode("java", ExceptionUtils.getStackTrace(e))
                        .send(Main.getBotApi().getUserById(BotConfig.getInstance().adminUserId()).join());
            }
        });
    }
}