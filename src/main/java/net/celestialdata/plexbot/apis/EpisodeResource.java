package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.db.daos.EpisodeDao;
import net.celestialdata.plexbot.db.daos.EpisodeSubtitleDao;
import net.celestialdata.plexbot.db.entities.Episode;
import net.celestialdata.plexbot.db.entities.EpisodeSubtitle;
import net.celestialdata.plexbot.utilities.FileUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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

@Tag(name = "Episodes", description = "Endpoints available for fetching information about episodes in the database")
@Path("/api/v1/episodes")
@RolesAllowed({"admin", "encoder", "user"})
public class EpisodeResource {

    @ConfigProperty(name = "FolderSettings.tvFolder")
    String tvFolder;

    @ConfigProperty(name = "FolderSettings.tempFolder")
    String tempFolder;

    @Inject
    EpisodeDao episodeDao;

    @Inject
    EpisodeSubtitleDao episodeSubtitleDao;

    @Inject
    FileUtilities fileUtilities;

    @GET
    @Path("/{tvdb_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Episode get(@PathParam("tvdb_id") long tvdbId) {
        return episodeDao.getByTvdbId(tvdbId);
    }

    @GET
    @Path("/{id}/subtitles")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<EpisodeSubtitle> getSubtitles(@PathParam("id") int id) {
        Episode episode = episodeDao.get(id);
        return episodeSubtitleDao.getByEpisode(episode);
    }

    @GET
    @Path("/download/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response downloadFile(@PathParam("id") int id) {
        // Load the movie to be downloaded from the database
        Episode episode = episodeDao.get(id);

        // Build the file object for this movie
        File file = new File(tvFolder + episode.show.foldername + "/Season " + episode.season + "/" + episode.filename);

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

    @POST
    @Path("/upload")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response uploadFile(@org.jboss.resteasy.annotations.jaxrs.HeaderParam("Content-Length") long fileSize, @org.jboss.resteasy.annotations.jaxrs.HeaderParam("Content-Id") int id, InputStream data) {
        // Get the movie info from the database
        Episode episode = episodeDao.get(id);

        // Determine the temporary file path for the uploaded media file
        var path = tempFolder + "uploads/" + episode.tvdbId + ".mkv";

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
            var backupSuccess = fileUtilities.moveMedia(tvFolder + episode.show.foldername + "/Season " + episode.season + "/" + episode.filename,
                    tvFolder + episode.show.foldername + "/Season " + episode.season + "/" + episode.filename + ".bak", true);

            // Fail if the backup was not successful
            if (!backupSuccess) {
                return Response.status(500, "Failed to backup original file").build();
            }

            // Build the new filename for this episode
            var newFilename = episode.show.name + " - " + fileUtilities.buildSeasonAndEpisodeString(episode.number, episode.season) +
                    (StringUtils.isBlank(episode.title) ? ".mkv" : " - " + episode.title + ".mkv");

            // Move the file into place
            var moveSuccess = fileUtilities.moveMedia(path, tvFolder + episode.show.foldername + "/Season " + episode.season + "/" + newFilename, true);

            // If the move was not successful then fail
            if (!moveSuccess) {
                return Response.status(500, "Failed to move media file").build();
            }

            // Delete the backup file since the move was successful
            fileUtilities.deleteFile(tvFolder + episode.show.foldername + "/Season " + episode.season + "/" + episode.filename + ".bak");

            // Update the database with the new file
            episodeDao.update(id, newFilename);

            // Return that the upload was successful
            return Response.ok().build();
        } catch (IOException e) {
            return Response.status(500, e.getMessage()).build();
        }
    }
}