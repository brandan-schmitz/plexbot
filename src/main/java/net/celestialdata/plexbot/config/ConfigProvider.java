package net.celestialdata.plexbot.config;

import net.celestialdata.plexbot.config.interfaces.ApiKeys;
import net.celestialdata.plexbot.config.interfaces.BotSettings;
import net.celestialdata.plexbot.config.interfaces.DatabaseSettings;

/**
 * Binds the values of the configs to their interfaces
 *
 * @author Celestialdeath99
 */
public class ConfigProvider {

    /**
     * Binds the values of BotSettings under Interfaces to BOT_SETTINGS
     */
    public static final BotSettings BOT_SETTINGS = ConfigLoader.configurationProvider().bind("BotSettings", BotSettings.class);

    /**
     * Binds the values of DatabaseSettings under Interfaces to DATABASE_SETTINGS
     */
    public static final DatabaseSettings DATABASE_SETTINGS = ConfigLoader.configurationProvider().bind("DatabaseSettings", DatabaseSettings.class);

    /**
     * Binds the values of ApiKeys under Interfaces to API_KEYS
     */
    public static final ApiKeys API_KEYS = ConfigLoader.configurationProvider().bind("ApiKeys", ApiKeys.class);
}
