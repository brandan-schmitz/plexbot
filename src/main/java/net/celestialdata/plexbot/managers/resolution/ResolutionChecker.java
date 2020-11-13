package net.celestialdata.plexbot.managers.resolution;

import net.celestialdata.plexbot.BotWorkPool;
import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.apis.omdb.Omdb;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.database.DatabaseDataManager;
import net.celestialdata.plexbot.managers.BotStatusManager;
import net.celestialdata.plexbot.utils.BotEmojis;
import net.celestialdata.plexbot.utils.CustomRunnable;
import net.celestialdata.plexbot.workhandlers.TorrentHandler;
import org.javacord.api.entity.channel.Channel;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;

public class ResolutionChecker implements CustomRunnable {
    @Override
    public String taskName() {
        return "Resolution Manager";
    }

    @Override
    public void endTask() {
        BotStatusManager.getInstance().removeProcess(taskName());
        BotStatusManager.getInstance().clearResolutionManagerStatus();
    }

    @Override
    public void endTask(Throwable error) {
        BotStatusManager.getInstance().removeProcess(taskName());
        BotStatusManager.getInstance().clearResolutionManagerStatus();
        error.printStackTrace();
    }

    @Override
    public void run() {
        // Create the list of movie objects that will contain a list of all movies on the server
        ArrayList<ResolutionManager.Movie> movies = new ArrayList<>();

        // Create a list of movies in the database
        for (String id : DatabaseDataManager.getAllMovieIDs()) {
            movies.add(new ResolutionManager.Movie(id, DatabaseDataManager.getMovieResolution(id)));
        }

        // Cycle through all the movies in the database to find any that can be upgraded
        int progress = 0;
        for (ResolutionManager.Movie m : movies) {
            TorrentHandler torrentHandler;
            torrentHandler = new TorrentHandler(m.id);

            progress++;
            BotStatusManager.getInstance().setResolutionManagerStatus(progress, movies.size());

            // Search YTS for the movie
            try {
                torrentHandler.searchYts();
            } catch (NullPointerException e) {
                continue;
            }

            // Skip to the next movie if the search failed or returned no results
            if (torrentHandler.didSearchFail()) {
                continue;
            } else if (torrentHandler.didSearchReturnNoResults()) {
                continue;
            }

            // Get a list of movies matching the movie id
            torrentHandler.buildMovieList();
            if (torrentHandler.didBuildMovieListFail()) {
                continue;
            }

            // Build the list of torrent files for the movie
            torrentHandler.buildTorrentList();
            if (torrentHandler.areNoTorrentsAvailable()) {
                continue;
            }

            // Generate the magnet link for the movie torrent file
            // Required to be here in order to determine the resolution of the available movies
            torrentHandler.generateMagnetLink();
            if (torrentHandler.isNotMagnetLink()) {
                continue;
            }

            // Add the movie to the list of upgradable movies if the torrent has a higher resolution available
            if (m.oldResolution != 0 && torrentHandler.getTorrentQuality() > m.oldResolution) {
                m.newResolution = torrentHandler.getTorrentQuality();
                ResolutionManager.addUpgradableMovie(Omdb.getMovieInfo(m.id), m.oldResolution, m.newResolution, torrentHandler.getTorrentSize());
            }
        }

        // Cycle through all the movies that can be upgraded, and start a task for any that have the
        // thumbsup reaction on the message for the upgrade availability.
        for (String id : DatabaseDataManager.getAllUpgradableMovieIds()) {
            Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.upgradableMoviesChannelId())
                    .flatMap(textChannel -> textChannel.getMessageById(DatabaseDataManager.getUpgradableMovieMessageId(id)).join()
                            .getReactionByEmoji(BotEmojis.THUMBS_UP)).ifPresent(reaction -> BotWorkPool.getInstance().submitProcess(
                    new ResolutionUpgrader(Omdb.getMovieInfo(id))));
        }

        // Update the upgradable-movies channel to show when this check last finished running
        Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.upgradableMoviesChannelId())
                .flatMap(Channel::asServerTextChannel).ifPresent(serverTextChannel -> serverTextChannel.updateTopic(
                "Last checked: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        .format(ZonedDateTime.now()) + " CST"));

        // Remove the task info from the bot status manager
        endTask();
    }
}
