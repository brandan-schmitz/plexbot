package net.celestialdata.plexbot.periodictasks;

import io.quarkus.scheduler.Scheduled;
import net.celestialdata.plexbot.db.daos.DownloadQueueItemDao;
import net.celestialdata.plexbot.processors.EpisodeDownloadProcessor;
import org.eclipse.microprofile.context.ManagedExecutor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class EpisodeDownloadStarter {

    @Inject
    DownloadQueueItemDao downloadQueueItemDao;

    @Inject
    Instance<ManagedExecutor> executor;

    @Inject
    Instance<EpisodeDownloadProcessor> episodeDownloadProcessors;

    @Scheduled(every = "15s", delay = 10, delayUnit = TimeUnit.SECONDS)
    public void startDownload() {
        // Check to see how many are currently downloading
        var runningCount = downloadQueueItemDao.getDownloadingCount();

        // Execute the next download task as long as there is not more than 3 already running. This will limit
        // the number of parallel downloads to 4 at a time. This also spaces out the downloads by 1 minute.
        if (runningCount <= 3) {
            executor.get().execute(episodeDownloadProcessors.get());
        }
    }
}