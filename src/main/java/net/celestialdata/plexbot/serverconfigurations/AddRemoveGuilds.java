package net.celestialdata.plexbot.serverconfigurations;

import net.celestialdata.plexbot.database.DatabaseDataManager;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.Collection;

public class AddRemoveGuilds {

    /**
     * Add a guild and its members to the database
     * @param guild The guild to add to the database
     */
    public static void addGuild(Server guild) {
        // Add the servers current users to the DB
        Collection<User> users = guild.getMembers();
        for (User u : users) {
            if (!u.isBot()) {
                DatabaseDataManager.addUser(u);
            }
        }

        // Add the server from the database
        DatabaseDataManager.addServer(guild);
    }

    public static void removeGuild(Server server) {
        // Get the ID of the server being removed
        long serverID = server.getId();

        // Remove the server from the database
        DatabaseDataManager.removeServer(serverID);
    }

    static void removeGuild(long serverID) {
        // Remove the server from the database
        DatabaseDataManager.removeServer(serverID);
    }
}