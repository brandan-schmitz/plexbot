package net.celestialdata.plexbot.apis.yts;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import net.celestialdata.plexbot.apis.CloudflareAuthorizer;
import net.celestialdata.plexbot.apis.yts.objects.YtsSearchResponse;
import net.celestialdata.plexbot.config.ConfigProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClients;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Yts {
    private static String getCloudflareClearanceCode() {
        String clearanceCode = "";

        CloudflareAuthorizer cloudflareAuthorizer = new CloudflareAuthorizer(HttpClients.createDefault(), HttpClientContext.create());
        try {
            if (cloudflareAuthorizer.tryAuthorization("https://" + ConfigProvider.BOT_SETTINGS.currentYtsDomain())) {
                clearanceCode = cloudflareAuthorizer.getClearanceCookie();
            }
        } catch (IOException | ScriptException e) {
            e.printStackTrace();
        }

        return clearanceCode;
    }

    public static YtsSearchResponse search(String imdbCode) {
        HttpResponse<YtsSearchResponse> response = null;
        try {
            response = Unirest.get("https://" + ConfigProvider.BOT_SETTINGS.currentYtsDomain() + "/api/v2/list_movies.json")
                    .header("cf_clearance", getCloudflareClearanceCode())
                    .queryString("query_term", imdbCode).asObjectAsync(YtsSearchResponse.class).get(15, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.out.println(e.getMessage());
        }

        return response.getBody();
    }
}
