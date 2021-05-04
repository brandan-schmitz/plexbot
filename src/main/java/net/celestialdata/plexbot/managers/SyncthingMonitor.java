package net.celestialdata.plexbot.managers;

import net.celestialdata.plexbot.client.ApiException;
import net.celestialdata.plexbot.client.BotClient;
import net.celestialdata.plexbot.configuration.BotConfig;
import net.celestialdata.plexbot.utils.CustomRunnable;

public class SyncthingMonitor implements CustomRunnable {
    private boolean wasSyncing = false;

    @Override
    public String taskName() {
        return "SyncThing Monitor";
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
    public void run() {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> endTask(e));
        boolean isSyncing = false;

        // Check the movie and TV folders on all listed devices to see if anything is being synced
        for (String device : BotConfig.getInstance().syncthingDevices()) {
            try {
                // Check if the movie folder is being synced
                isSyncing = BotClient.getInstance()
                        .syncthingApi.getCompletionStatus(BotConfig.getInstance().syncthingMovieFolderId(), device)
                        .getCompletion() != 100;

                // Break the loop if the first folder checked is already syncing, no need to check any others
                if (isSyncing) {
                    break;
                }

                // Check if the TV folder is being synced
                isSyncing = BotClient.getInstance()
                        .syncthingApi.getCompletionStatus(BotConfig.getInstance().syncthingTvFolderId(), device)
                        .getCompletion() != 100;
            } catch (ApiException e) {
                endTask(e);
                return;
            }
        }

        // Refresh the media servers if a sync has been completed
        if (wasSyncing && !isSyncing) {
            BotClient.getInstance().refreshPlexServers();
            wasSyncing = false;
        } else if (isSyncing) {
            wasSyncing = true;
        }

        endTask();
    }
}
