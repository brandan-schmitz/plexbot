package net.celestialdata.plexbot.managers;

import net.celestialdata.plexbot.apis.omdb.objects.movie.OmdbMovie;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.utils.CustomRunnable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;

/**
 * This class handled the methods required to download a movie
 * and provide information on the download status.
 *
 * @author Celestialdeath99
 */
public class DownloadManager implements CustomRunnable {
    public final Object lock = new Object();
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.0");
    private boolean isDownloading = true;
    private boolean didDownloadFail = false;
    private long progress = 0;
    private long size = 0;
    private String filename;
    private final String downloadLink;

    public DownloadManager(String downloadLink, OmdbMovie movie) {
        this.downloadLink = downloadLink;

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
    }

    @Override
    public String taskName() {
        return "Download " + filename;
    }

    @Override
    public void run() {
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

        endTask();
    }

    /**
     * Rename the file to the specified file extension.
     *
     * @param newExtension the file extension to save the file as.
     * @return if the rename operation was successful or not
     */
    public boolean renameFile(String newExtension) {
        File file = new File(ConfigProvider.BOT_SETTINGS.movieDownloadFolder() + filename);
        return file.renameTo(new File(ConfigProvider.BOT_SETTINGS.movieDownloadFolder() + filename + newExtension));
    }

    /**
     * Get the current progress of the download as a percentage.
     *
     * @return the progress of the download.
     */
    public String getProgress() {
        String _progress = decimalFormat.format(((double) progress / size) * 100) + "%";
        if (_progress.contains("NaN%")) {
            _progress = "Download Queued";
        }

        return _progress;
    }

    /**
     * Get the filename of the movie being downloaded.
     *
     * @return the filename of the movie
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Get if the thread is currently downloading the file.
     *
     * @return the boolean state of the download (true=downloading/false=finished).
     */
    public boolean isDownloading() {
        return isDownloading;
    }

    /**
     * Check if the download failed.
     *
     * @return the boolean value of the download failure status.
     */
    public boolean didDownloadFail() {
        return didDownloadFail;
    }
}