package net.celestialdata.plexbot.managers.resolution;

import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.apis.omdb.objects.movie.OmdbMovie;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.database.DatabaseDataManager;
import net.celestialdata.plexbot.utils.BotColors;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import uk.co.caprica.vlcjinfo.MediaInfo;

/**
 * A class for handling all things related to the upgrade manager
 *
 * @author Celestialdeath99
 */
public class ResolutionUtilities {

    /**
     * Add a movie that can be upgraded to the database and upgradable-movies channel.
     *
     * @param movie         the OmdbMovie containing the information about the movie to add
     * @param oldResolution the numerical value of the resolution of the current version of the movie on the server
     * @param newResolution the numerical value of the resolution of the version of the movie that can be downloaded
     * @param newSize       the size of the new video file
     */
    static void addUpgradableMovie(OmdbMovie movie, int oldResolution, int newResolution, String newSize) {
        MediaInfo mediaInfo = MediaInfo.mediaInfo(ConfigProvider.BOT_SETTINGS.movieDownloadFolder() + DatabaseDataManager.getMovieFilename(movie.imdbID));

        // First check to see if the movie is already listed in the upgrade list, if not add it and send the message
        if (!DatabaseDataManager.isMovieInUpgradableList(movie.imdbID)) {
            Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.upgradableMoviesChannelId()).ifPresent(
                    textChannel -> textChannel.sendMessage(new EmbedBuilder()
                            .setTitle(movie.Title)
                            .setDescription(
                                    "**Current Size:** " + mediaInfo.first("General").value("File size") + "\n" +
                                            "**New Size:** " + newSize + "\n\n" +
                                            "**ID:** " + movie.imdbID + "\n" +
                                            "**Year:** " + movie.Year + "\n" +
                                            "**Director(s):** " + movie.Director + "\n" +
                                            "**Plot:** " + movie.Plot
                            )
                            .setImage(movie.Poster)
                            .setColor(BotColors.INFO)
                            .setFooter(newResolution >= 2160 ?
                                    "Upgradable to 4k" + " from " + oldResolution + "p" :
                                    "Upgradable to " + newResolution + "p" + " from " + oldResolution + "p"
                            )
                    ).exceptionally(
                            ExceptionLogger.get()).thenAccept(message ->
                                    DatabaseDataManager.addMovieToUpgradableList(movie.imdbID, newResolution, message.getId()))
            );
        }
    }

    // A custom movie class used for storing information about movies used in this manager
    static class Movie {
        String id;
        int oldResolution;
        int newResolution;

        public Movie(String id, int oldResolution) {
            this.id = id;
            this.oldResolution = oldResolution;
        }
    }
}