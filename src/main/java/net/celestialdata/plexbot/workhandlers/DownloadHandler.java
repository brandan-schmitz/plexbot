package net.celestialdata.plexbot.workhandlers;

import net.celestialdata.plexbot.BotWorkPool;
import net.celestialdata.plexbot.apis.omdb.objects.movie.OmdbMovie;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.utils.BotStatusManager;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * This class handled the methods required to download a movie
 * and provide information on the download status.
 *
 * @author Celestialdeath99
 */
public class DownloadHandler {
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.0");
    private boolean isDownloading = true;
    private boolean didDownloadFail = false;
    private boolean didRenameFail = false;
    private long progress = 0;
    private long size = 0;
    private String filename;
    public final Object lock = new Object();

    /**
     * This is the constructor for the download handler and is responsible
     * for downloading the specified file and the progress of the download.
     *
     * @param downloadLink The link of the file to download.
     * @param movie The OmdbMovie entity that is the movie being downloaded.
     * @param separateThread Should the download occur in a separate thread or not.
     */
    public void downloadFile(String downloadLink, OmdbMovie movie, Boolean separateThread) {
        Runnable downloader = () -> {
            // Remove anything from the filename that may cause issues
            filename = movie.Title + " (" + movie.Year + ")";
            filename = filename.replace("<", "");
            filename = filename.replace(">", "");
            filename = filename.replace(":", "");
            filename = filename.replace("\"", "");
            filename = filename.replace("/", "");
            filename = filename.replace("<\\", "");
            filename = filename.replace("|", "");
            filename = filename.replace("?", "");
            filename = filename.replace("*", "");
            filename = filename.replace(".", "");

            if (separateThread) {
                BotStatusManager.getInstance().addProcess("Download " + filename);
            }

            try {
                // Open a connection to the file being downloaded
                URLConnection connection = new URL(downloadLink).openConnection();
                ReadableByteChannel readableByteChannel = Channels.newChannel(connection.getInputStream());
                FileChannel fileOutputStream = new FileOutputStream(ConfigProvider.BOT_SETTINGS.movieDownloadFolder() + filename).getChannel();

                // Get the size of the file in bytes used in calculating the progress of the download
                size = connection.getContentLengthLong();

                // Download the file from the server
                while (fileOutputStream.transferFrom(readableByteChannel, fileOutputStream.size(), 1024) > 0) {
                    synchronized (lock) {
                        progress += 1024;
                        lock.notifyAll();
                    }
                }

                // Mark that the download has finished without errors and close the connection to the server.
                synchronized (lock) {
                    isDownloading = false;
                    lock.notifyAll();
                }
                fileOutputStream.close();

            } catch (IOException e) {
                synchronized (lock) {
                    didDownloadFail = true;
                    isDownloading = false;
                    lock.notifyAll();
                }
            }

            if (separateThread) {
                BotStatusManager.getInstance().removeProcess("Download " + filename);
            }
        };

        if (separateThread) {
            BotWorkPool.getInstance().executor.submit(downloader);
        } else downloader.run();
    }

    /**
     * Rename the file to the specified file extension.
     *
     * @param newExtension The file extension to save the file as.
     */
    public void renameFile(String newExtension) {
        File file = new File(ConfigProvider.BOT_SETTINGS.movieDownloadFolder() + filename);
        didRenameFail = !file.renameTo(new File(ConfigProvider.BOT_SETTINGS.movieDownloadFolder() + filename + newExtension));
    }

    /**
     * Delete the old movie files from the server for this movie.
     *
     * @return if the files were deleted
     */
    public boolean deletePreviousVersions() {
        boolean deleted = true;

        FileFilter filter = new WildcardFileFilter(filename + ".*");
        File[] files = new File(ConfigProvider.BOT_SETTINGS.movieDownloadFolder()).listFiles(filter);
        assert files != null;
        for (File file : files) {
            if (!file.delete()) {
                deleted = false;
            }
        }

        return deleted;
    }

    /**
     * Get the current progress of the download as a percentage.
     *
     * @return The progress of the download.
     */
    public String getProgress() {
        String _progress = decimalFormat.format(((double) progress / size) * 100) + "%";
        if (_progress.contains("NaN%")) {
            _progress = "Download Queued";
        }

        return _progress;
    }

    /**
     * Get if the thread is currently downloading the file.
     *
     * @return The boolean state of the download (true=downloading/false=finished).
     */
    public boolean isDownloading() {
        return isDownloading;
    }

    /**
     * Check if the download failed.
     *
     * @return The boolean value of the download failure status.
     */
    public boolean didDownloadFail() {
        return didDownloadFail;
    }

    /**
     * Check if the rename operation failed.
     *
     * @return The boolean value of the rename operation status.
     */
    public boolean didRenameFail() {
        return didRenameFail;
    }
}
