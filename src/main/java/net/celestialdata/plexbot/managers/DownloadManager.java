package net.celestialdata.plexbot.managers;

import net.celestialdata.plexbot.client.model.OmdbMovieInfo;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.utils.CustomRunnable;
import org.apache.commons.lang3.StringUtils;

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
    private final String downloadLink;
    private boolean isDownloading = true;
    private boolean didDownloadFail = false;
    private boolean isProcessing = false;
    private boolean didProcessingFail = false;
    private boolean isFileServerMounted = true;
    private long progress = 0;
    private long size = 0;
    private String filename;
    private final String fileExtension;

    public DownloadManager(String downloadLink, OmdbMovieInfo movieInfo, String fileExtension) {
        this.downloadLink = downloadLink;
        this.fileExtension = fileExtension;

        // Remove anything from the filename that may cause issues
        filename = movieInfo.getTitle() + " (" + movieInfo.getYear() + ")";
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
        filename = StringUtils.stripAccents(filename);
    }

    @Override
    public String taskName() {
        return "Download: " + filename;
    }

    @Override
    public boolean cancelOnDuplicate() {
        return true;
    }

    @Override
    public void run() {
        // Configure task to run the endTask method if there was an error
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> endTask(e));

        try {
            // Open a connection to the file being downloaded
            URLConnection connection = new URL(downloadLink).openConnection();
            ReadableByteChannel readableByteChannel = Channels.newChannel(connection.getInputStream());
            FileChannel fileOutputStream = new FileOutputStream(ConfigProvider.BOT_SETTINGS.tempFolder() + filename + ".pbdownload").getChannel();

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
                isProcessing = true;
                lock.notifyAll();
            }
            fileOutputStream.close();

        } catch (IOException e) {
            synchronized (lock) {
                didDownloadFail = true;
                isDownloading = false;
                isProcessing = false;
                lock.notifyAll();
            }
        }

        // Attempt to move the file to the movie folder. This folder should contain a file called movie.pb otherwise the
        // rename process should be aborted.
        isFileServerMounted = new File(ConfigProvider.BOT_SETTINGS.movieFolder() + "movie.pb").exists();
        if (isFileServerMounted) {
            File file = new File(ConfigProvider.BOT_SETTINGS.tempFolder() + filename + ".pbdownload");

            synchronized (lock) {
                didProcessingFail = file.renameTo(new File(ConfigProvider.BOT_SETTINGS.movieFolder() + filename + fileExtension));
                isProcessing = false;
                lock.notifyAll();
            }
        } else {
            synchronized (lock) {
                didProcessingFail = true;
                isProcessing = false;
                isFileServerMounted = false;
                lock.notifyAll();
            }
        }

        endTask();
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


    /**
     * Get if the thread is currently processing the downloaded file.
     *
     * @return the boolean states of the processing (true=processing/false=finished).
     */
    public boolean isProcessing() {
        return isProcessing;
    }

    /**
     * Check if the processing of the file failed
     *
     * @return the boolean value of the processing failure status.
     */
    public boolean didProcessingFail() {
        return didProcessingFail;
    }

    /**
     * Get if the file server was mounted or not when trying to rename and move the file
     *
     * @return if the server was mounted or not
     */
    public boolean isFileServerMounted() {
        return isFileServerMounted;
    }
}