package net.celestialdata.plexbot.serverconfigurations;

import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.database.DatabaseDataManager;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class UpdateGuildsUsersDB {

    public static void runScan() {
        // Get the servers the bot is in
        Collection<Server> servers = Main.getBotApi().getServers();

        ArrayList<Long> serverIDs = new ArrayList<>();
        for (Server s : servers) {
            serverIDs.add(s.getId());
        }

        // Get all the servers IDs in the DB
        ArrayList<Long> dbServerIDs = DatabaseDataManager.getAllServersID();

        // Get all the servers in the DB
        Collection<Server> dbServers = new ArrayList<>();
        for (Long id : dbServerIDs) {
            Main.getBotApi().getServerById(id).ifPresent(dbServers::add);
        }

        // Get all the user IDs from the database
        ArrayList<Long> dbUsers = DatabaseDataManager.getAllUsersID();

        // Get all the Users in the servers
        Collection<User> users = new ArrayList<>();
        for (Server s : servers) {
            for (User u : s.getMembers()) {
                if (!u.isBot()) {
                    try {
                        users.add(Main.getBotApi().getUserById(u.getId()).get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Remove duplicate users from the list
        Set<User> userConversion = new HashSet<>(users);
        users.clear();
        users.addAll(userConversion);

        // Check if a user is missing from the DB or needs to be updated
        for (User u : users) {
            boolean add = true;

            if (dbUsers.contains(u.getId())) {
                add = !u.getDiscriminatedName().equals(DatabaseDataManager.getUserDiscriminatedName(u.getId()));
            }

            if (add) {
                DatabaseDataManager.addUser(u);
            }
        }

        // Check for servers missing from the database
        for (Server s : servers) {
            if (!dbServers.contains(s)) {
                AddRemoveGuilds.addGuild(s);
            }
        }

        // Check if a server removed the bot from it
        for (Long id : dbServerIDs) {
            boolean remove = true;

            if (serverIDs.contains(id)) {
                remove = false;
            }

            if (remove) {
                AddRemoveGuilds.removeGuild(id);
            }
        }
    }
}
