package net.celestialdata.plexbot.client;

import net.celestialdata.plexbot.client.api.OmDbApi;
import net.celestialdata.plexbot.client.api.RdbApi;
import net.celestialdata.plexbot.client.api.YtsApi;
import net.celestialdata.plexbot.config.ConfigProvider;

public class BotClient {
    private static BotClient single_instance = null;
    public final OmDbApi omdbApi;
    public final YtsApi ytsApi;
    public final RdbApi rdbApi;

    private BotClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setApiKey(ConfigProvider.API_KEYS.omdbApiKey());
        apiClient.setAccessToken(ConfigProvider.API_KEYS.realDebridKey());

        omdbApi = new OmDbApi(apiClient);
        ytsApi = new YtsApi(apiClient);
        rdbApi = new RdbApi(apiClient);
    }

    public static BotClient getInstance() {
        if (single_instance == null) {
            single_instance = new BotClient();
        }

        return single_instance;
    }
}