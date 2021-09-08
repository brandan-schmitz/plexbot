package net.celestialdata.plexbot.clients.models.sg.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.celestialdata.plexbot.clients.models.sg.objects.SgHistoryItem;

import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SgFetchHistoryResponse {
    public String message;
    public String result;

    @JsonAlias(value = "data")
    public List<SgHistoryItem> results;
}