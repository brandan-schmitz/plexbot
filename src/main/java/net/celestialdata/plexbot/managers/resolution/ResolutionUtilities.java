package net.celestialdata.plexbot.managers.resolution;

import net.celestialdata.plexbot.BotConfig;
import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.client.model.OmdbItem;
import net.celestialdata.plexbot.database.DbOperations;
import net.celestialdata.plexbot.database.builders.UpgradeItemBuilder;
import net.celestialdata.plexbot.database.models.Movie;
import net.celestialdata.plexbot.utils.BotColors;
import net.celestialdata.plexbot.utils.MediaInfoHelper;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

/**
 * A class for handling all things related to the upgrade manager
 *
 * @author Celestialdeath99
 */
public class ResolutionUtilities {

    /**
     * Add a movie that can be upgraded to the database and upgradable-movies channel.
     *
     * @param movieInfo     the OmdbMovie containing the information about the movie to add
     * @param oldResolution the numerical value of the resolution of the current version of the movie on the server
     * @param newResolution the numerical value of the resolution of the version of the movie that can be downloaded
     * @param newSize       the size of the new video file
     */
    static void addUpgradableMovie(OmdbItem movieInfo, int oldResolution, int newResolution, String newSize) {
        Movie dbMovie = DbOperations.movieOps.getMovieById(movieInfo.getImdbID());
        String filesize = MediaInfoHelper.getFilesize(BotConfig.getInstance().movieFolder() + dbMovie.getFolderName() + "/" + dbMovie.getFilename());

        // First check to see if the movie is already listed in the upgrade list, if not add it and send the message
        if (!DbOperations.upgradeItemOps.exists(movieInfo.getImdbID())) {
            Main.getBotApi().getTextChannelById(BotConfig.getInstance().upgradableMoviesChannelId()).ifPresent(
                    textChannel -> textChannel.sendMessage(new EmbedBuilder()
                            .setTitle(movieInfo.getTitle())
                            .setDescription(
                                    "**Current Size:** " + filesize + "\n" +
                                            "**New Size:** " + newSize + "\n\n" +
                                            "**ID:** " + movieInfo.getImdbID() + "\n" +
                                            "**Year:** " + movieInfo.getYear() + "\n" +
                                            "**Director(s):** " + movieInfo.getDirector() + "\n" +
                                            "**Plot:** " + movieInfo.getPlot()
                            )
                            .setImage(movieInfo.getPoster())
                            .setColor(BotColors.INFO)
                            .setFooter(newResolution >= 2160 ?
                                    "Upgradable to 4k" + " from " + oldResolution + "p" :
                                    "Upgradable to " + newResolution + "p" + " from " + oldResolution + "p"
                            )
                    ).exceptionally(
                            ExceptionLogger.get()).thenAccept(message -> DbOperations.saveObject(
                            new UpgradeItemBuilder()
                                    .withMovie(movieInfo.getImdbID())
                                    .withNewResolution(newResolution)
                                    .withMessageId(message.getId())
                                    .build()
                            )
                    )
            );
        }
    }

    // A custom movie class used for storing information about movies used in this manager
    static class ResolutionMovie {
        final String id;
        final int oldResolution;
        int newResolution;

        public ResolutionMovie(String id, int oldResolution) {
            this.id = id;
            this.oldResolution = oldResolution;
        }
    }
}