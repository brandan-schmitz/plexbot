package net.celestialdata.plexbot.utilities;

import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;

@ApplicationScoped
public class FileUtilities {
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");

    @ConfigProperty(name = "FolderSettings.tempFolder")
    String tempFolder;

    public Multi<String> downloadFile(String url, String outputFilename) {
        return Multi.createFrom().emitter(multiEmitter -> {
            long progress = 0;
            try {
                // Open a connection to the file being downloaded
                URLConnection connection = new URL(url).openConnection();
                ReadableByteChannel readableByteChannel = Channels.newChannel(connection.getInputStream());
                FileChannel fileOutputStream = new FileOutputStream(tempFolder + outputFilename + ".pbdownload").getChannel();

                // Get the size of the file in bytes used in calculating the progress of the download
                long size = connection.getContentLengthLong();

                // Download the file
                while (!multiEmitter.isCancelled() && fileOutputStream.transferFrom(readableByteChannel, fileOutputStream.size(), 1024) > 0) {
                    progress += 1024;
                    multiEmitter.emit(decimalFormat.format(((double) progress / size) * 100));
                }

                // Close the connection to the server
                fileOutputStream.close();
                multiEmitter.complete();
            } catch (IOException e) {
                multiEmitter.fail(e);
            }
        });
    }
}