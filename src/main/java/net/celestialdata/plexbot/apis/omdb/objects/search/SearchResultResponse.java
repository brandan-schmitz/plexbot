package net.celestialdata.plexbot.apis.omdb.objects.search;

import java.util.ArrayList;

public class SearchResultResponse {
    public ArrayList<SearchResult> Search = new ArrayList<>();
    public int totalResults;
    public String Response;
    public String Error;

    public void setResponse(String response) {
        Response = response;
    }

    public void setTotalResults(int numResults) {
        totalResults = numResults;
    }

    public void setError(String error) {
        Error = error;
    }

    public void addSearchResult(SearchResult result) {
        Search.add(result);
    }
}
