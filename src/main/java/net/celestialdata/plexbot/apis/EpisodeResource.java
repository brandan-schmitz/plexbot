package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.db.daos.EpisodeDao;
import net.celestialdata.plexbot.db.daos.EpisodeSubtitleDao;
import net.celestialdata.plexbot.db.entities.Episode;
import net.celestialdata.plexbot.db.entities.EpisodeSubtitle;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Tag(name = "Episodes", description = "Endpoints available for fetching information about episodes in the database")
@Path("/episodes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EpisodeResource {

    @Inject
    EpisodeDao episodeDao;

    @Inject
    EpisodeSubtitleDao episodeSubtitleDao;

    @GET
    @Path("/{tmdb_id}")
    public Episode get(@PathParam("tmdb_id") long tmdbId) {
        return episodeDao.getByTmdbId(tmdbId);
    }

    @GET
    @Path("/{id}/subtitles")
    public List<EpisodeSubtitle> getSubtitles(@PathParam("id") int id) {
        Episode episode = episodeDao.get(id);
        return episodeSubtitleDao.getByEpisode(episode);
    }
}