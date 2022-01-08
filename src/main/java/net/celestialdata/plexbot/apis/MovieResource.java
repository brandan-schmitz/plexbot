package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.db.daos.MovieDao;
import net.celestialdata.plexbot.db.daos.MovieSubtitleDao;
import net.celestialdata.plexbot.db.entities.Movie;
import net.celestialdata.plexbot.db.entities.MovieSubtitle;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

@Tag(name = "Movies", description = "Endpoints available for fetching information about movies in the database")
@Path("/api/v1/movies")
@RolesAllowed({"admin", "encoder", "user"})
public class MovieResource {

    @ConfigProperty(name = "FolderSettings.movieFolder")
    String movieFolder;

    @ConfigProperty(name = "FolderSettings.tempFolder")
    String tempFolder;

    @Inject
    MovieDao movieDao;

    @Inject
    MovieSubtitleDao movieSubtitleDao;

    @Inject
    FileUtilities fileUtilities;

    @GET
    @Path("/{tmdb_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Movie get(@PathParam("tmdb_id") long tmdbId) {
        return movieDao.getByTmdbId(tmdbId);
    }

    @GET
    @Path("/{id}/subtitles")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<MovieSubtitle> getSubtitles(@PathParam("id") int id) {
        Movie movie = movieDao.get(id);
        return movieSubtitleDao.getByMovie(movie);
    }

    @GET
    @Path("/download/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response downloadFile(@PathParam("id") int id) {
        // Load the movie to be downloaded from the database
        Movie movie = movieDao.get(id);

        // Build the file object for this movie
        File file = new File(movieFolder + movie.folderName + "/" + movie.filename);

        // Make sure the file exists otherwise throw a 404 error
        if (!file.exists()) {
            return Response.status(404).build();
        } else {
            return Response.ok((StreamingOutput) output -> {
                try {
                    InputStream input = new FileInputStream(file);
                    IOUtils.copy(input, output);
                    output.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).header("Content-Disposition", "attachment;filename=" + file.getName())
                    .header("Content-Length", file.length())
                    .build();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    @POST
    @Path("/upload")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response uploadFile(@HeaderParam("Content-Length") long fileSize, @HeaderParam("Content-Id") int id, InputStream data) {
        // Get the movie info from the database
        Movie movie = movieDao.get(id);

        // Determine the temporary file path for the uploaded media file
        var path = tempFolder + "uploads/" + movie.tmdbId + ".mkv";

        try {
            // Create the input and output streams
            ReadableByteChannel readableByteChannel = Channels.newChannel(data);
            FileChannel fileOutputStream = new FileOutputStream(path, false).getChannel();

            // Accept the file upload
            for (long bytesWritten = 0; bytesWritten < fileSize;) {
                bytesWritten += fileOutputStream.transferFrom(readableByteChannel, bytesWritten, fileSize - bytesWritten);
            }

            // Close the data streams
            readableByteChannel.close();
            fileOutputStream.close();

            // Backup the old video file
            var backupSuccess = fileUtilities.moveMedia(movieFolder + movie.folderName + "/" + movie.filename,
                    movieFolder + movie.folderName + "/" + movie.filename + ".bak", true);

            // Fail if the backup was not successful
            if (!backupSuccess) {
                return Response.status(500, "Failed to backup original file").build();
            }

            // Move the file into place
            var moveSuccess = fileUtilities.moveMedia(path, movieFolder + movie.folderName + "/" + movie.folderName + ".mkv", true);

            // If the move was not successful then fail
            if (!moveSuccess) {
                return Response.status(500, "Failed to move media file").build();
            }

            // Delete the backup file since the move was successful
            fileUtilities.deleteFile(movieFolder + movie.folderName + "/" + movie.filename + ".bak");

            // Update the database with the new file
            movieDao.update(id, movie.folderName + ".mkv");

            // Return that the upload was successful
            return Response.ok().build();
        } catch (IOException e) {
            return Response.status(500, e.getMessage()).build();
        }
    }
}