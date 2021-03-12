package net.celestialdata.plexbot.managers.waitlist;

import net.celestialdata.plexbot.BotWorkPool;
import net.celestialdata.plexbot.client.ApiException;
import net.celestialdata.plexbot.client.BotClient;
import net.celestialdata.plexbot.client.model.OmdbItem;
import net.celestialdata.plexbot.database.DbOperations;
import net.celestialdata.plexbot.database.models.WaitlistItem;
import net.celestialdata.plexbot.managers.BotStatusManager;
import net.celestialdata.plexbot.utils.CustomRunnable;
import net.celestialdata.plexbot.workhandlers.TorrentHandler;

public class WaitlistChecker implements CustomRunnable {

    @Override
    public String taskName() {
        return "Waitlist Manager";
    }

    @Override
    public boolean cancelOnFull() {
        return true;
    }

    @Override
    public boolean cancelOnDuplicate() {
        return true;
    }

    @Override
    public void endTask() {
        BotStatusManager.getInstance().removeProcess(taskName());
        BotStatusManager.getInstance().clearWaitlistManagerStatus();
    }

    @Override
    public void endTask(Throwable error) {
        reportError(error);
        BotStatusManager.getInstance().removeProcess(taskName());
        BotStatusManager.getInstance().clearWaitlistManagerStatus();
    }

    @Override
    public void run() {
        // Configure task to run the endTask method if there was an error
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> endTask(e));

        int progress = 0;

        // Fetch all the movies in the waiting list and cycle through them
        for (WaitlistItem item : DbOperations.waitlistItemOps.getAllItems()) {
            progress++;
            BotStatusManager.getInstance().setWaitlistManagerStatus(progress, DbOperations.waitlistItemOps.getCount());

            // Get the info about the movie from IMDB
            OmdbItem movieInfo;
            try {
                movieInfo = BotClient.getInstance().omdbApi.getById(item.getId());
            } catch (ApiException e) {
                endTask(e);
                continue;
            }
            TorrentHandler torrentHandler;

            // Move to the next movie if the movie was manually added to the server/db
            // or already exists for some reason.
            if (DbOperations.movieOps.exists(item.getId())) {
                DbOperations.deleteItem(WaitlistItem.class, item.getId());
                continue;
            }

            // Set the torrent handler to the ID of the movie
            torrentHandler = new TorrentHandler(item.getId());

            // Search YTS for the movie
            try {
                torrentHandler.searchYts();
            } catch (Exception e) {
                WaitlistUtilities.updateMessage(movieInfo);
                continue;
            }

            // If the search failed or if the movie was not found then skip to the next movie
            if (torrentHandler.didSearchReturnNoResults()) {
                WaitlistUtilities.updateMessage(movieInfo);
                continue;
            }

            // If the movie was found, add a task to the work queue to download the movie
            BotWorkPool.getInstance().submitProcess(new WaitlistDownloader(torrentHandler, movieInfo));
        }

        // Remove the task info from the bot status manager
        endTask();
    }
}