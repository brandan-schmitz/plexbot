package net.celestialdata.plexbot.managers.waitlist;

import net.celestialdata.plexbot.BotWorkPool;
import net.celestialdata.plexbot.apis.omdb.Omdb;
import net.celestialdata.plexbot.apis.omdb.objects.movie.OmdbMovie;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.database.DatabaseDataManager;
import net.celestialdata.plexbot.managers.BotStatusManager;
import net.celestialdata.plexbot.utils.CustomRunnable;
import net.celestialdata.plexbot.workhandlers.TorrentHandler;

import java.util.ArrayList;

public class WaitlistChecker implements CustomRunnable {

    @Override
    public String taskName() {
        return "Waitlist Manager";
    }

    @Override
    public void endTask() {
        BotStatusManager.getInstance().removeProcess(taskName());
        BotStatusManager.getInstance().clearWaitlistManagerStatus();
    }

    @Override
    public void endTask(Throwable error) {
        BotStatusManager.getInstance().removeProcess(taskName());
        BotStatusManager.getInstance().clearWaitlistManagerStatus();
    }

    @Override
    public void run() {
        // Configure task to run the endTask method if there was an error
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> endTask(e));

        int progress = 0;
        ArrayList<String> movieIds = DatabaseDataManager.getMovieIdsInWaitlist();

        // Fetch all the movies in the waiting list and cycle through them
        for (String id : movieIds) {
            progress++;
            BotStatusManager.getInstance().setWaitlistManagerStatus(progress, movieIds.size());

            // Get the info about the movie from IMDB
            OmdbMovie movie = Omdb.getMovieInfo(id);
            TorrentHandler torrentHandler;

            // Set the default movie poster if one is not available
            if (movie.Poster.equalsIgnoreCase("N/A")) {
                movie.Poster = ConfigProvider.BOT_SETTINGS.noPosterImageUrl();
            }

            // Move to the next movie if the movie was manually added to the server/db
            // or already exists for some reason.
            if (DatabaseDataManager.doesMovieExistOnServer(movie.imdbID)) {
                WaitlistUtilities.deleteWaitlistItem(movie.imdbID);
                continue;
            }

            // Set the torrent handler to the ID of the movie
            torrentHandler = new TorrentHandler(movie.imdbID);

            // Search YTS for the movie
            try {
                torrentHandler.searchYts();
            } catch (NullPointerException e) {
                continue;
            }

            // If the search failed or if the movie was not found then skip to the next movie
            if (torrentHandler.didSearchFail()) {
                WaitlistUtilities.updateMessage(movie);
                continue;
            } else if (torrentHandler.didSearchReturnNoResults()) {
                WaitlistUtilities.updateMessage(movie);
                continue;
            }

            // If the movie was found, add a task to the work queue to download the movie
            BotWorkPool.getInstance().submitProcess(new WaitlistDownloader(torrentHandler, movie));
        }

        // Remove the task info from the bot status manager
        endTask();
    }
}
