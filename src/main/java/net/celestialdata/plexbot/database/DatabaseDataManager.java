package net.celestialdata.plexbot.database;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;

/**
 * Handles the insertions and queries to and from the database
 *
 * @author Celestiadeath99
 */
public class DatabaseDataManager {

    /**
     * Add a Discord server to the database.
     *
     * @param guild the server to add
     * @see Server
     */
    public static void addServer(Server guild) {
        try (Connection connection = DataSource.getConnection()) {
            connection.createStatement().execute("INSERT INTO `Guilds` (`guild_ID`, `guild_name`, `guild_prefix`, `guild_creation_date`, `guild_join_date`, `owner_id`) VALUES ('" + guild.getId() + "', '" + guild.getName() + "', '!', '" + guild.getCreationTimestamp() + "', '" + Instant.now() + "', '" + guild.getOwnerId() + "')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the prefix to a specified server.
     *
     * @param guildID the Discord ID of the server to get the prefix for
     * @return the prefix of the server in string form
     */
    public static String getServerPrefix(long guildID) {
        String prefix = "";
        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT guild_prefix FROM Guilds WHERE guild_ID = '" + guildID + "'");

            while (resultSet.next()) {
                prefix = resultSet.getString("guild_prefix");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return prefix;
    }

    /**
     * Update the command prefix for a particular server.
     *
     * @param guildID the Discord ID of the server to update the prefix for
     * @param newPrefix the new prefix that will be used for the specified server
     */
    public static void updateServerPrefix(long guildID, String newPrefix) {
        try (Connection connection = DataSource.getConnection()) {
            connection.createStatement().execute("UPDATE Guilds SET guild_prefix = '" + newPrefix + "' WHERE guild_ID = " + guildID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the IDs of all the servers in the database.
     *
     * @return the IDs of the servers in the database
     */
    public static ArrayList<Long> getAllServersID() {
        ArrayList<Long> ids = new ArrayList<>();

        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT guild_ID FROM Guilds");

            while (resultSet.next()) {
                ids.add(resultSet.getLong("guild_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    /**
     * Remove the specified server from the database.
     *
     * @param guildID the Discord ID of the server to remove
     */
    public static void removeServer(long guildID) {
        try (Connection connection = DataSource.getConnection()) {
            connection.createStatement().execute("DELETE FROM `Guilds` WHERE `Guilds`.`guild_ID` = " + guildID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a user to the database.
     *
     * @param user The Discord User to add
     * @see User
     */
    public static void addUser(User user) {
        try (Connection connection = DataSource.getConnection()) {
            connection.createStatement().execute("INSERT INTO Users (user_ID, discriminated_name) VALUES ('" + user.getId() + "', '" + user.getDiscriminatedName() +"') ON DUPLICATE KEY UPDATE discriminated_name = '" + user.getDiscriminatedName() + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the username (discriminated name) of a user by their ID.
     *
     * @param userId The ID of the user to fetch the discriminated name for
     * @return The discriminated name of the user
     */
    public static String getUserDiscriminatedName(Long userId) {
        String name = "";

        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT discriminated_name FROM Users WHERE user_id = " + userId);

            while (resultSet.next()) {
                name = resultSet.getString("discriminated_name");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return name;
    }

    /**
     * Get the IDs of all the users in the database.
     *
     * @return the IDs of the users in the database
     */
    public static ArrayList<Long> getAllUsersID() {
        ArrayList<Long> ids = new ArrayList<>();

        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT user_ID FROM Users");

            while (resultSet.next()) {
                ids.add(resultSet.getLong("user_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    /**
     * Add a movie to the database. This is where a movie gets added if
     * it has been downloaded to the server already.
     *
     * @param movieId       the IMDB ID of the movie
     * @param movieTitle    the title of the movie
     * @param movieYear     the year the movie was released
     * @param movieQuality  the numerical value of the movie resolution (320, 480, 720, 1080, 2160)
     * @param movieFilename the filename of the movie
     */
    public static void addMovie(String movieId, String movieTitle, String movieYear, int movieQuality, String movieFilename) {
        movieTitle = movieTitle.replace("'", "");

        try (Connection connection = DataSource.getConnection()) {
            connection.createStatement().execute("INSERT INTO Movies (movie_id, movie_title, movie_year, movie_resolution, movie_filename) VALUES ('" + movieId + "', '" + movieTitle + "', '" + movieYear + "', '" + movieQuality + "', '" + movieFilename + "')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the IDs of all the movies in the database.
     *
     * @return the IDs of the movies int he database
     */
    public static ArrayList<String> getAllMovieIDs() {
        ArrayList<String> ids = new ArrayList<>();

        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT movie_id FROM Movies");

            while (resultSet.next()) {
                ids.add(resultSet.getString("movie_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    /**
     * Check if a movie is list in the database as existing on the server.
     *
     * @param movieId the ID of the movie to check for
     * @return if a movie was in the database
     */
    public static boolean doesMovieExistOnServer(String movieId) {
        boolean exists = false;

        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT movie_id FROM Movies WHERE movie_id = '" + movieId + "'");

            while (resultSet.next()) {
                exists = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exists;
    }

    /**
     * Get the resolution of the specified movie.
     *
     * @param movieId the IMDB ID of the movie
     * @return the resolution of the movie
     */
    public static int getMovieResolution(String movieId) {
        int quality = 0;

        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT movie_resolution FROM Movies WHERE movie_id = '" + movieId + "'");

            while (resultSet.next()) {
                quality = resultSet.getInt("movie_resolution");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return quality;
    }

    /**
     * Get the name of the specified movie.
     *
     * @param movieId the IMDB ID of the movie
     * @return the name of the movie
     */
    public static String getMovieName(String movieId) {
        String name = "";

        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT movie_title FROM Movies WHERE movie_id = '" + movieId + "'");

            while (resultSet.next()) {
                name = resultSet.getString("movie_title");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return name;
    }

    /**
     * Get the current filename of the movie as it is stored in the filesystem.
     *
     * @param movieId the IMDB ID of the movie
     * @return the filename of the movie
     */
    public static String getMovieFilename(String movieId) {
        String filename = "";

        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT movie_filename FROM Movies WHERE movie_id = '" + movieId + "'");

            while (resultSet.next()) {
                filename = resultSet.getString("movie_filename");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return filename;
    }

    /**
     * Update the filename of a movie stored in the database.
     *
     * @param movieId       the IMDB ID of the movie
     * @param movieFilename the new filename of the movie
     */
    public static void updateMovieFilename(String movieId, String movieFilename) {
        try (Connection connection = DataSource.getConnection()) {
            connection.createStatement().execute("UPDATE Movies SET movie_filename = '" + movieFilename + "' WHERE movie_id = '" + movieId + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the resolution of a movie stored in the database.
     *
     * @param movieId      the IMDB ID of the movie
     * @param movieQuality the new resolution
     */
    public static void updateMovieResolution(String movieId, int movieQuality) {
        try (Connection connection = DataSource.getConnection()) {
            connection.createStatement().execute("UPDATE Movies SET movie_resolution = '" + movieQuality + "' WHERE movie_id = '" + movieId + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a movie to the waitlist
     *
     * @param movieId     the IMDB ID of the movie
     * @param movieTitle  the title of the movie
     * @param movieYear   the year the movie was released
     * @param requesterId the ID of the Discord User who requested the movie
     * @param messageId   the ID of the message in the waiting-list notifications channel
     */
    public static void addMovieToWaitlist(String movieId, String movieTitle, String movieYear, long requesterId, long messageId) {
        movieTitle = movieTitle.replace("'", "");

        try (Connection connection = DataSource.getConnection()) {
            connection.createStatement().execute("INSERT INTO Waitlist (movie_id, movie_title, movie_year, requester_id, message_id ) VALUES ('" + movieId + "', '" + movieTitle + "', '" + movieYear + "', '" + requesterId + "', '" + messageId + "')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get if a movie is already in the waitlist or not.
     *
     * @param movieId the IMDB ID of the movie
     * @return if the movie is in the waitlist or not
     */
    public static boolean isMovieInWaitlist(String movieId) {
        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT movie_id FROM Waitlist WHERE movie_id = '" + movieId + "'");

            if (resultSet.next()) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get all the IMDB IDs of the movies in the waitlist.
     *
     * @return the IMDB IDs of the movies in the waitlist
     */
    public static ArrayList<String> getMovieIdsInWaitlist() {
        ArrayList<String> ids = new ArrayList<>();

        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT movie_id FROM Waitlist");

            while (resultSet.next()) {
                String id = resultSet.getString("movie_id");
                ids.add(id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    /**
     * Get the discord message ID for a notification about a movie in the waitlist channel.
     *
     * @param movieId the IMDB ID of the movie to get the message id for
     * @return the ID of the discord message
     */
    public static long getWaitlistMessageId(String movieId) {
        long id = 0;

        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT message_id FROM Waitlist WHERE movie_id = '" + movieId + "'");

            while (resultSet.next()) {
                id = resultSet.getLong("message_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return id;
    }

    /**
     * Get the user who requested a movie that is in the waitlist.
     *
     * @param movieId the IMDB ID of the movie
     * @return the ID of the user
     */
    public static long getWhoRequestedWaitlistItem(String movieId) {
        long id = 0;

        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT requester_id FROM Waitlist WHERE movie_id ='" + movieId +"'");

            while (resultSet.next()) {
                id = resultSet.getLong("requester_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return id;
    }

    /**
     * Remove a movie from the waitlist.
     *
     * @param movieId the IMDB ID of the movie
     */
    public static void removeMovieFromWaitlist(String movieId) {
        try (Connection connection = DataSource.getConnection()) {
            connection.createStatement().execute("DELETE FROM Waitlist WHERE movie_id ='" + movieId + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a movie to the list of movies which can be upgraded to a better resolution version.
     *
     * @param movieId the IMDB ID of the movie
     * @param upgradedResolution the resolution the movie can be upgraded to
     * @param messageId the discord ID of the message showing the movie has an upgrade available
     */
    public static void addMovieToUpgradableList(String movieId, int upgradedResolution, long messageId) {
        try (Connection connection = DataSource.getConnection()) {
            connection.createStatement().execute("INSERT INTO Upgrades (movie_id, upgraded_resolution, message_id) VALUES ('" + movieId + "', '" + upgradedResolution + "', '" + messageId + "')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a movie from the list of upgradable movies.
     *
     * @param movieId the IMDB ID of the movie
     */
    public static void removeMovieFromUpgradableList(String movieId) {
        try (Connection connection = DataSource.getConnection()) {
            connection.createStatement().execute("DELETE FROM Upgrades WHERE movie_id ='" + movieId + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get if a movie is already in the list of movies that have upgrades available.
     *
     * @param movieId the IMDB ID of the movie
     * @return if the movie was in the upgradable list or not
     */
    public static boolean isMovieInUpgradableList(String movieId) {
        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT movie_id FROM Upgrades WHERE movie_id = '" + movieId + "'");

            if (resultSet.next()) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the discord message about a movie that has a available upgrade.
     *
     * @param movieId the IMDB ID of the movie
     * @return the discord ID of the message
     */
    public static long getUpgradableMovieMessageId(String movieId) {
        long id = 0;

        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT message_id FROM Upgrades WHERE movie_id = '" + movieId + "'");

            while (resultSet.next()) {
                id = resultSet.getLong("message_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return id;
    }

    /**
     * Get the IDs of all the movies that can be upgraded to a higher resolution.
     *
     * @return the IDs of the movies that can be upgraded
     */
    public static ArrayList<String> getAllUpgradableMovieIds() {
        ArrayList<String> ids = new ArrayList<>();

        try (Connection connection = DataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT movie_id FROM Upgrades");

            while (resultSet.next()) {
                String id = resultSet.getString("movie_id");
                ids.add(id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }
}