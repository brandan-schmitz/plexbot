package net.celestialdata.plexbot;

import com.google.common.util.concurrent.AtomicDouble;
import net.celestialdata.plexbot.client.BotClient;
import net.celestialdata.plexbot.commandhandler.CommandHandler;
import net.celestialdata.plexbot.commandhandler.JavacordHandler;
import net.celestialdata.plexbot.commands.*;
import net.celestialdata.plexbot.configuration.BotConfig;
import net.celestialdata.plexbot.database.DbOperations;
import net.celestialdata.plexbot.database.HibernateUtil;
import net.celestialdata.plexbot.database.builders.UserBuilder;
import net.celestialdata.plexbot.database.models.Episode;
import net.celestialdata.plexbot.database.models.Movie;
import net.celestialdata.plexbot.database.models.Season;
import net.celestialdata.plexbot.database.models.Show;
import net.celestialdata.plexbot.managers.BotStatusManager;
import net.celestialdata.plexbot.managers.SyncthingMonitor;
import net.celestialdata.plexbot.managers.resolution.ResolutionChecker;
import net.celestialdata.plexbot.managers.waitlist.WaitlistChecker;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.util.logging.FallbackLoggerConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static DiscordApi botApi;
    private static CommandHandler commandHandler;
    private static String version;

    private static void registerBotCommands() {
        commandHandler = new JavacordHandler(botApi);

        commandHandler.registerCommand(new PingCommand());
        commandHandler.registerCommand(new HelpCommand());
        commandHandler.registerCommand(new PurgeCommand());
        commandHandler.registerCommand(new ImportCommand());
        commandHandler.registerCommand(new OldRequestCommand());
        commandHandler.registerCommand(new RequestMovieCommand());
        commandHandler.registerCommand(new RequestEpisodeCommand());
    }

    // Builds the bots DiscordApi and connects the bot to Discord
    private static void startBot() {
        botApi = new DiscordApiBuilder()
                .setToken(BotConfig.getInstance().token())
                .setAllIntents()
                .setWaitForUsersOnStartup(true)
                .setWaitForServersOnStartup(true)
                .login()
                .join();
    }

    public static DiscordApi getBotApi() {
        return botApi;
    }

    public static CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public static String getVersion() {
        return version;
    }

    /**
     * Generates a Bot Invite URL
     *
     * @return Returns a string containing the bot invite URL
     */
    private static String getInviteUrl() {
        return botApi.createBotInvite(new PermissionsBuilder()
                .setState(PermissionType.CHANGE_NICKNAME, PermissionState.ALLOWED)
                .setState(PermissionType.SEND_MESSAGES, PermissionState.ALLOWED)
                .setState(PermissionType.MANAGE_MESSAGES, PermissionState.ALLOWED)
                .setState(PermissionType.EMBED_LINKS, PermissionState.ALLOWED)
                .setState(PermissionType.USE_EXTERNAL_EMOJIS, PermissionState.ALLOWED)
                .setState(PermissionType.ADD_REACTIONS, PermissionState.ALLOWED)
                .build());
    }

    public static void main(String[] args) {
        // Disable debug logging
        FallbackLoggerConfiguration.setDebug(false);
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "ERROR");

        // Set the default version and date variables
        version = "0.0.0";
        String date = "00/00/0000";

        // Load the application information
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("pom.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);

            version = properties.getProperty("application-version");
            date = properties.getProperty("version-date");
        } catch (IOException ignored) {
        }

        // Display the application banner
        System.out.format("\n" +
                        "           _____  _      ________   ______   ____ _______                \n" +
                        "          |  __ \\| |    |  ____\\ \\ / /  _ \\ / __ \\__   __|          \n" +
                        "          | |__) | |    | |__   \\ V /| |_) | |  | | | |                 \n" +
                        "          |  ___/| |    |  __|   > < |  _ <| |  | | | |                  \n" +
                        "          | |    | |____| |____ / . \\| |_) | |__| | | |                 \n" +
                        "          |_|    |______|______/_/ \\_\\____/ \\____/  |_|               \n" +
                        "                                                                         \n" +
                        "                   Version %s - %s                            \n" +
                        "                                                                         \n" +
                        "  This software is distributed under the GNU GENERAL PUBLIC v3 license   \n" +
                        "      and is available for anyone to use and modify as long as the       \n" +
                        "           proper attributes are given in the modified code.             \n" +
                        "                                                                         \n",
                version, date);

        // Initialize the bot configuration.
        System.out.print("Loading bot configuration file...");
        BotConfig.getInstance();
        System.out.println("Done");

        // Initialize the database connections
        System.out.print("Connecting to the database...");
        HibernateUtil.getSessionFactory();
        System.out.println("Done");

        // Verify database contents against the filesystem
        System.out.print("Verifying database integrity");
        List<Movie> movies = DbOperations.movieOps.getAllMovies();
        List<Show> shows = DbOperations.showOps.getAllItems();
        List<Season> seasons = DbOperations.seasonOps.getAllItems();
        List<Episode> episodes = DbOperations.episodeOps.getAllItems();

        AtomicInteger progress = new AtomicInteger(0);
        int totalVerifications = movies.size() + shows.size() + seasons.size() + episodes.size();
        int stepSize = totalVerifications / 3;
        AtomicInteger nextMark = new AtomicInteger(stepSize);

        // Verify movies
        movies.forEach(movie -> {
            if (Files.notExists(Path.of(BotConfig.getInstance().movieFolder() + "/" + movie.getFolderName() + "/" + movie.getFilename()))) {
                System.out.println("  ERROR\nData inconsistency found: Movie \"" + movie.getTitle() + " (" + movie.getYear() + ") {imdb-" + movie.getId() +
                        "}\" could not be found on the filesystem, however it is listed in the database.");
                System.exit(1);
            }
            if (progress.get() == nextMark.get()) {
                System.out.print(".");
                nextMark.getAndAdd(stepSize);
            }
            progress.getAndIncrement();
        });

        // Verify TV shows
        shows.forEach(show -> {
            if (!Files.isDirectory(Path.of(BotConfig.getInstance().tvFolder() + "/" + show.getFolderName()))) {
                System.out.println("  ERROR\nData inconsistency found: The TV show folder \"" + show.getFolderName() + "\" could not be found on the " +
                        "filesystem, however it is listed in the database.");
                System.exit(1);
            }
            if (progress.get() == nextMark.get()) {
                System.out.print(".");
                nextMark.getAndAdd(stepSize);
            }
            progress.getAndIncrement();
        });

        // Verify TV seasons
        seasons.forEach(season -> {
            if (!Files.isDirectory(Path.of(BotConfig.getInstance().tvFolder() + "/" + season.getShow().getFolderName() + "/" + season.getFoldername()))) {
                System.out.println("  ERROR\nData inconsistency found: The season folder \"" + season.getFoldername() + "\" for the TV show \"" + season.getShow().getName()
                        + "\" could not be found on the filesystem, however it is listed in the database.");
                System.exit(1);
            }
            if (progress.get() == nextMark.get()) {
                System.out.print(".");
                nextMark.getAndAdd(stepSize);
            }
            progress.getAndIncrement();
        });

        // Verify TV episodes
        episodes.forEach(episode -> {
            if (Files.notExists(Path.of(BotConfig.getInstance().tvFolder() + "/" + episode.getShow().getFolderName() + "/" + episode.getSeason().getFoldername()
                    + "/" + episode.getFilename()))) {
                System.out.println("  ERROR\nData inconsistency found: The file \"" + episode.getFilename() + "\" for episode " + episode.getNumber() +
                        ", season " + episode.getSeason().getNumber() + " of the TV show \"" + episode.getShow().getName() + "\" could not be found on the filesystem, however it " +
                        "is listed in the database.");
                System.exit(1);
            }
            if (progress.get() == nextMark.get()) {
                System.out.print(".");
                nextMark.getAndAdd(stepSize);
            }
            progress.getAndIncrement();
        });
        System.out.println("Done");

        // Start the bot
        System.out.print("Connecting to the Discord API...");
        startBot();
        System.out.println("Done");

        // Register guild join and leaver listeners
        System.out.print("Registering event listeners...");
        botApi.addListener((ServerMemberJoinListener) event ->
                DbOperations.saveObject(new UserBuilder()
                        .withId(event.getUser().getId())
                        .withDiscriminatedName(event.getUser().getDiscriminatedName())
                        .build()
                )
        );
        System.out.println("Done");

        // Update the database with to add or remove users that may have joined or left while the bot was offline
        System.out.print("Scanning for new Users...");
        List<net.celestialdata.plexbot.database.models.User> dbUsers = DbOperations.userOps.getAllUsers();
        for (Server s : Main.getBotApi().getServers()) {
            for (org.javacord.api.entity.user.User u : s.getMembers()) {
                if (!u.isBot()) {
                    DbOperations.saveObject(new UserBuilder()
                            .withId(u.getId())
                            .withDiscriminatedName(u.getDiscriminatedName())
                            .build()
                    );
                    dbUsers.removeIf(o -> o.getId() == u.getId());
                }
            }
        }
        for (net.celestialdata.plexbot.database.models.User u : dbUsers) {
            DbOperations.deleteItem(net.celestialdata.plexbot.database.models.User.class, u.getId());
        }
        System.out.println("Done");

        System.out.print("Configuring API resources...");
        BotClient.getInstance();
        try {
            BotClient.getInstance().refreshPlexServers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done");

        System.out.print("Configuring Bot Work Pool...");
        BotWorkPool.getInstance();
        System.out.println("Done");

        // Configure shutdown hooks
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Ensure clean disconnect from Discord API
            botApi.disconnect();

            // Ensure all bot work is stopped cleanly
            BotWorkPool.getInstance().executor.shutdown();

            // Disconnect from the database
            HibernateUtil.shutdown();
        }));

        System.out.print("Configuring Bot Status Manager...");
        BotStatusManager.getInstance();
        System.out.println("Done");

        // Start the waitlist manager
        System.out.print("Configuring Waitlist Manager...");
        scheduledExecutorService.scheduleAtFixedRate(() ->
                BotWorkPool.getInstance().submitProcess(new WaitlistChecker()), 0, 1, TimeUnit.HOURS);
        System.out.println("Done");

        // Start the resolution manager
        System.out.print("Configuring Resolution Manager...");
        scheduledExecutorService.scheduleAtFixedRate(() ->
                BotWorkPool.getInstance().submitProcess(new ResolutionChecker()), 0, 2, TimeUnit.HOURS);
        System.out.println("Done");

        // Start the SyncThing monitor if enabled
        if (BotConfig.getInstance().syncthingEnabled()) {
            System.out.print("Configuring SyncThing Monitor...");
            SyncthingMonitor syncthingMonitor = new SyncthingMonitor();
            scheduledExecutorService.scheduleAtFixedRate(() ->
                    BotWorkPool.getInstance().submitProcess(syncthingMonitor), 0, 30, TimeUnit.SECONDS);
            System.out.println("Done");
        }

        // Register the bot commands
        System.out.print("Registering Bot Commands...");
        registerBotCommands();
        System.out.println("Done");

        // Display start-up complete message and invite link
        System.out.println("\nBot Initialization complete. Please share the following link to invite it to join guilds:\n" + getInviteUrl() + "\n");
    }
}