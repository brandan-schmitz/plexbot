package net.celestialdata.plexbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import net.celestialdata.plexbot.client.BotClient;
import net.celestialdata.plexbot.commandhandler.CommandHandler;
import net.celestialdata.plexbot.commandhandler.JavacordHandler;
import net.celestialdata.plexbot.commands.*;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.database.DataSource;
import net.celestialdata.plexbot.database.DatabaseDataManager;
import net.celestialdata.plexbot.managers.BotStatusManager;
import net.celestialdata.plexbot.managers.resolution.ResolutionChecker;
import net.celestialdata.plexbot.managers.waitlist.WaitlistChecker;
import net.celestialdata.plexbot.serverconfigurations.AddRemoveGuilds;
import net.celestialdata.plexbot.serverconfigurations.UpdateGuildsUsersDB;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.listener.server.ServerJoinListener;
import org.javacord.api.listener.server.ServerLeaveListener;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.util.logging.FallbackLoggerConfiguration;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static DiscordApi botApi;
    private static CommandHandler commandHandler;

    private static void registerBotCommands() {
        commandHandler = new JavacordHandler(botApi);

        commandHandler.registerCommand(new PingCommand());
        commandHandler.registerCommand(new NewPrefixCommand());
        commandHandler.registerCommand(new HelpCommand());
        commandHandler.registerCommand(new PurgeCommand());
        commandHandler.registerCommand(new RequestMovieCommand());
    }

    // Builds the bots DiscordApi and connects the bot to Discord
    private static void startBot() {
        botApi = new DiscordApiBuilder()
                .setToken(ConfigProvider.BOT_SETTINGS.token())
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

    private static void initUnirest() {
        Unirest.setObjectMapper(new ObjectMapper() {
            private final com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static void main(String[] args) {
        // Disable debug logging
        FallbackLoggerConfiguration.setDebug(false);

        // Initialize the database connections
        System.out.print("Connecting to the database...");
        DataSource.init();
        System.out.println("Done");

        // Start the bot
        System.out.print("Connecting to the Discord API...");
        startBot();
        System.out.println("Done");

        // Register guild join and leaver listeners
        System.out.print("Registering event listeners...");
        botApi.addListener((ServerJoinListener) event -> AddRemoveGuilds.addGuild(event.getServer()));
        botApi.addListener((ServerLeaveListener) event -> AddRemoveGuilds.removeGuild(event.getServer()));
        botApi.addListener((ServerMemberJoinListener) event -> DatabaseDataManager.addUser(event.getUser()));
        System.out.println("Done");

        // Update the database with any new guilds or users that may have been added while the bot was offline
        System.out.print("Scanning for new Guilds and Users...");
        UpdateGuildsUsersDB.runScan();
        System.out.println("Done");

        System.out.print("Configuring API resources...");
        initUnirest();
        BotClient.getInstance();
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

        // Register the bot commands
        System.out.print("Registering Bot Commands...");
        registerBotCommands();
        System.out.println("Done");

        // Display start-up complete message and invite link
        System.out.println("\nBot Initialization complete. Please share the following link to invite it to join guilds:\n" + getInviteUrl() + "\n");
    }
}