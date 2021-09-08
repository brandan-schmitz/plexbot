package net.celestialdata.plexbot.clients.services;

import net.celestialdata.plexbot.clients.models.sg.responses.SgFetchHistoryResponse;
import net.celestialdata.plexbot.clients.models.sg.responses.SgGetEpisodeResponse;
import net.celestialdata.plexbot.clients.models.sg.responses.SgSimpleResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Singleton
@RegisterRestClient(configKey = "SickgearSettings.address")
public interface SgService {

    @GET
    @Path(value = "/{api_key}/")
    @Produces(MediaType.APPLICATION_JSON)
    SgSimpleResponse addShow(@PathParam("api_key") String apiKey, @QueryParam("cmd") String sgCommand, @QueryParam("indexer") int indexerNum,
                             @QueryParam("status") String status, @QueryParam("upgrade_once") boolean upgradeOnce, @QueryParam("initial[]") String initial1,
                             @QueryParam("initial[]") String initial2, @QueryParam("initial[]") String initial3, @QueryParam("initial[]") String initial4,
                             @QueryParam("initial[]") String initial5, @QueryParam("indexerid") long showTvdbId);

    @GET
    @Path(value = "/{api_key}/")
    @Produces(MediaType.APPLICATION_JSON)
    SgFetchHistoryResponse fetchHistory(@PathParam("api_key") String apiKey, @QueryParam("cmd") String sgCommand, @QueryParam("type") String operationType);

    @GET
    @Path(value = "/{api_key}/")
    @Produces(MediaType.APPLICATION_JSON)
    SgGetEpisodeResponse getEpisode(@PathParam("api_key") String apiKey, @QueryParam("cmd") String sgCommand, @QueryParam("indexer") int indexerNum,
                                    @QueryParam("indexerid") long showTvdbId, @QueryParam("season") int season, @QueryParam("episode") int episode);

    @GET
    @Path(value = "/{api_key}/")
    @Produces(MediaType.APPLICATION_JSON)
    SgSimpleResponse setEpisodeStatus(@PathParam("api_key") String apiKey, @QueryParam("cmd") String sgCommand, @QueryParam("indexer") int indexerNum,
                                      @QueryParam("indexerid") long showTvdbId, @QueryParam("season") int season, @QueryParam("episode") int episode,
                                      @QueryParam("status") String status, @QueryParam("quality") String quality);
}