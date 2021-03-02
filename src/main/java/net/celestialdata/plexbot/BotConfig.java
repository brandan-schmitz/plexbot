package net.celestialdata.plexbot;


import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * Used to access the configuration stored in the Trawler config.yaml
 * file and use its values.
 *
 * @author Brandan Schmitz
 */
public class BotConfig {
    // Stores the active instance of BotConfig
    private static BotConfig instance = null;

    // Stores the configuration file of the active TrawlerConfiguration
    private static YAMLConfiguration config = null;

    /**
     * Sole constructor of BotConfig.
     */
    protected BotConfig() {
        // Set the filename to load
        File yamlFile = new File("config.yaml");

        // Attempt to load the configuration file. If missing, display an error message and exit
        try {
            config = new Configurations().fileBased(YAMLConfiguration.class, yamlFile);
        } catch (ConfigurationException e) {
            try {
                // Copy the sample file over to the new file
                Files.copy(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("config.sample")),
                        Paths.get(new File("").getAbsolutePath() + "/config.yaml"));

                System.out.println("\n" +
                        "ERROR: The bots configuration file does not exist! If this is the first time running\n" +
                        "       the bot then is normal. Please check the directory where you installed the bot and\n" +
                        "       modify the config.yaml file that was just generated. Once you have modified this file\n" +
                        "       you may start the bot again.\n");
            } catch (Exception ignored) {
                System.out.println("\n" +
                        "ERROR: The bots configuration file does not exist and I was unable to create one! \n" +
                        "       Please check the Plexbot source repo for a sample config.yaml file to use.\n");
            }

            System.exit(1);
        }
    }

    /**
     * Returns a thread-safe, singleton instance of BotConfig. This
     * can be used to access the config.yaml file that configures Plexbot.
     * <p>
     * When this is called, if an existing Config has already been instantiated, then this will
     * simply return the existing instance. Otherwise it will create a new instance and then return
     * that new instance.
     * <p>
     *
     * @return an instance of the bot application configuration
     * @see BotConfig
     */
    public synchronized static BotConfig getInstance() {
        // If there is not an existing instance, create a new one
        if (instance == null) {
            instance = new BotConfig();
        }

        // Return an instance of TrawlerConfiguration
        return instance;
    }

    /**
     * Return the Discord bot token used to authenticate the bot with the Discord API.
     * @return discord bot token
     */
    public String token() {
        return config.getString("BotSettings.token", "");
    }

    /**
     * Return the name of the bot that will be displayed in Discord.
     * @return bot name
     */
    public String botName() {
        return config.getString("BotSettings.botName", "Plexbot");
    }

    /**
     * Return the prefix the bot will use to listen for commands.
     * @return bot prefix
     */
    public String botPrefix() {
        return config.getString("BotSettings.botPrefix", "!");
    }

    /**
     * Return the ID of the user that will be the bot administrator. This is where error messages
     * are sent for download errors and other issues.
     * @return admin user id
     */
    public long adminUserId() {
        return config.getLong("BotSettings.adminUserId");
    }

    /**
     * Return the default URL to use for a image if there is no image returned in the queries to IMDB
     * @return default image url
     */
    public String noPosterImageUrl() {
        return config.getString("BotSettings.noPosterImageUrl",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/No_image_available.svg/1024px-No_image_available.svg.png");
    }

    /**
     * Return the current domain to access YTS through.
     * @return yts domain
     */
    public String currentYtsDomain() {
        return config.getString("BotSettings.currentYtsDomain", "https://yts.mx");
    }

    /**
     * Return the ID of the channel used for messages about movies that can be upgraded to a better resolution.
     * @return channel id
     */
    public long upgradableMoviesChannelId() {
        return config.getLong("ChannelSettings.upgradableMoviesChannelId");
    }

    /**
     * Return the ID of the channel used for notifications about movies that have been
     * upgraded to a better resolution by the bot.
     * @return channel id
     */
    public long upgradedMoviesChannelId() {
        return config.getLong("ChannelSettings.upgradedMoviesChannelId");
    }

    /**
     * Return the ID of the channel where the bot will publish and updates its status message.
     * @return channel id
     */
    public long botStatusChannelId() {
        return config.getLong("ChannelSettings.botStatusChannelId");
    }

    /**
     * Return the ID of the channel where notifications about movies that have been added to the library are created.
     * @return channel id
     */
    public long newMoviesChannelId() {
        return config.getLong("ChannelSettings.newMoviesChannelId");
    }

    /**
     * Return the ID of the channel where notifications about tv episodes that have been added to the library are created.
     * @return channel id
     */
    public long newEpisodesChannelId() {
        return config.getLong("ChannelSettings.newEpisodesChannelId");
    }

    /**
     * Return the ID of the channel where notifications about movies that are in the waitinglist are located.
     * @return channel id
     */
    public long waitlistChannelId() {
        return config.getLong("ChannelSettings.waitlistChannelId");
    }

    /**
     * Return the folder that movies are stored in.
     * @return folder path
     */
    public String movieFolder() {
        var value = config.getString("FolderSettings.movieFolder", "");

        if (!value.endsWith("/")) {
            value = value + "/";
        }

        return value;
    }

    /**
     * Return the folder that TV Shows are stored in.
     * @return folder path
     */
    public String tvFolder() {
        var value = config.getString("FolderSettings.tvFolder", "");

        if (!value.endsWith("/")) {
            value = value + "/";
        }

        return value;
    }

    /**
     * Return the folder that files to be imported are stored in.
     * @return folder path
     */
    public String importFolder() {
        var value = config.getString("FolderSettings.importFolder", "");

        if (!value.endsWith("/")) {
            value = value + "/";
        }

        return value;
    }

    /**
     * Return the folder that is used for temporary file actions by the bot.
     * @return folder path
     */
    public String tempFolder() {
        var value = config.getString("FolderSettings.tempFolder", "");

        if (!value.endsWith("/")) {
            value = value + "/";
        }

        return value;
    }

    /**
     * Return if the bot should check to see if the mount file (mount.pb) is located in the
     * movies, tv, and import folders.
     * @return should mounts be checked
     */
    public boolean checkMount() {
        return config.getBoolean("FolderSettings.checkMount");
    }

    /**
     * Return the list of users authorized to use the import command.
     * @return user list
     */
    public List<Long> authorizedImportUsers() {
        return config.getList(Long.class, "ImportSettings.authorizedUsers");
    }

    /**
     * Return the list of files to ignore during the import command.
     * @return file list
     */
    public List<String> ignoredImportFiles() {
        return config.getList(String.class, "ImportSettings.ignoredFiles");
    }

    /**
     * Return the IP or hostname of the database server.
     * @return database connection address
     */
    public String dbConnectionAddress() {
        return config.getString("DatabaseSettings.dbConnectionAddress", "127.0.0.1");
    }

    /**
     * Return the port to connect to the database one.
     * @return database port
     */
    public int dbPort() {
        return config.getInt("DatabaseSettings.dbPort", 3306);
    }

    /**
     * Return the name of the database to use.
     * @return database name
     */
    public String dbName() {
        return config.getString("DatabaseSettings.dbName", "Plexbot");
    }

    /**
     * Return the username to connect to the database with.
     * @return database username
     */
    public String dbUsername() {
        return config.getString("DatabaseSettings.dbUsername", "Plexbot");
    }

    /**
     * Return the password to connect to the database with.
     * @return database password
     */
    public String dbPassword() {
        return config.getString("DatabaseSettings.dbPassword", "Plexbot");
    }

    /**
     * Return the IP address or hostname of the plex server the bot is managing media for.
     * @return plex connection address
     */
    public String plexConnectionAddress() {
        return config.getString("PlexServerSettings.plexConnectionAddress", "127.0.0.1");
    }

    /**
     * Return the port the plex server is running on.
     * @return plex port
     */
    public int plexPort() {
        return config.getInt("PlexServerSettings.plexPort", 32400);
    }

    /**
     * Return the username for the plex server. This should be an administrator user.
     * @return plex username
     */
    public String plexUsername() {
        return config.getString("PlexServerSettings.plexUsername", "");
    }

    /**
     * Return the password for the plex server. This should be an administrator user.
     * @return plex password
     */
    public String plexPassword() {
        return config.getString("PlexServerSettings.plexPassword", "");
    }

    /**
     * Return the client identifier used to identify individual devices on a plex account. This needs to be a unique
     * UUID. This can be generated by with one of the following commands:
     *    Windows (Powershell): [guid]::NewGuid()
     *    macOS and Linux: uuidgen
     * @return client identifier
     */
    public String clientIdentifier() {
        return config.getString("PlexServerSettings.clientIdentifier", "");
    }

    /**
     * Return the API key for the OMDB API.
     * @return api key
     */
    public String omdbApiKey() {
        return config.getString("ApiKeys.omdbApiKey", "");
    }

    /**
     * Return the API key for the Real-Debrid API.
     * @return api key
     */
    public String realDebridKey() {
        return config.getString("ApiKeys.realDebridKey", "");
    }
}