package net.celestialdata.plexbot.clients.utilities;

import net.celestialdata.plexbot.clients.models.sg.enums.SgQuality;
import net.celestialdata.plexbot.clients.models.sg.enums.SgStatus;
import net.celestialdata.plexbot.clients.models.sg.responses.SgFetchHistoryResponse;
import net.celestialdata.plexbot.clients.models.sg.responses.SgGetEpisodeResponse;
import net.celestialdata.plexbot.clients.models.sg.responses.SgSimpleResponse;
import net.celestialdata.plexbot.clients.services.SgService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@SuppressWarnings("UnusedReturnValue")
@ApplicationScoped
public class SgServiceWrapper {

    @ConfigProperty(name = "ApiKeys.sickgearApiKey")
    String apiKey;

    @Inject
    @RestClient
    SgService sgService;

    /**
     * Provide a wrapper function to simplify adding a show to SickGear through the SG REST client.
     * @param showTvdbId TVDB ID of the show to add
     * @return A simple response from SG
     */
    public SgSimpleResponse addShow(long showTvdbId) {
        return sgService.addShow(apiKey, "sg.show.addnew", 1, "skipped", false, "fullhdtv",
                "hdwebdl", "fullhdwebdl", "hdbluray", "fullhdbluray", "uhd4kweb", showTvdbId);
    }

    public SgFetchHistoryResponse fetchHistory() {
        return sgService.fetchHistory(apiKey, "sg.history", "snatched");
    }

    public SgGetEpisodeResponse getEpisode(long showTvdbId, int season, int episode) {
        return sgService.getEpisode(apiKey, "sg.episode", 1, showTvdbId, season, episode);
    }

    public boolean isShowAdded(long showTvdbId) {
        return sgService.getShowSimple(apiKey, "sg.show", 1, showTvdbId).result.equalsIgnoreCase("success");
    }

    public SgSimpleResponse setEpisodeStatus(long showTvdbId, int season, int episode, SgStatus status) {
        return sgService.setEpisodeStatus(apiKey, "sg.episode.setstatus", 1, showTvdbId, season, episode, status.getValue());
    }

    public SgSimpleResponse setEpisodeStatus(long showTvdbId, int season, int episode, SgStatus status, SgQuality quality) {
        return sgService.setEpisodeStatus(apiKey, "sg.episode.setstatus", 1, showTvdbId, season, episode, status.getValue(), quality.getApiString());
    }
}