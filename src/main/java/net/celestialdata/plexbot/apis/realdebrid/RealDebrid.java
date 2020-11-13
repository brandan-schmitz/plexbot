package net.celestialdata.plexbot.apis.realdebrid;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.celestialdata.plexbot.apis.realdebrid.objects.MagnetResponse;
import net.celestialdata.plexbot.apis.realdebrid.objects.Torrent;
import net.celestialdata.plexbot.apis.realdebrid.objects.UnrestrictedLink;
import net.celestialdata.plexbot.config.ConfigProvider;

public class RealDebrid {

    public static MagnetResponse addMagnet(String magnet) {
        HttpResponse<MagnetResponse> response = null;
        try {
            response = Unirest.post("https://api.real-debrid.com/rest/1.0/torrents/addMagnet")
                    .header("Authorization", "Bearer " + ConfigProvider.API_KEYS.realDebridKey())
                    .field("magnet", magnet)
                    .asObject(MagnetResponse.class);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return response.getBody();
    }

    public static Torrent getTorrentInfo(String torrentId) {
        HttpResponse<Torrent> response = null;
        try {
            response = Unirest.get("https://api.real-debrid.com/rest/1.0/torrents/info/{id}")
                    .header("Authorization", "Bearer " + ConfigProvider.API_KEYS.realDebridKey())
                    .routeParam("id", torrentId)
                    .asObject(Torrent.class);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return response.getBody();
    }

    public static int selectFile(String torrentID, String fileIds) {
        int status = 0;
        try {
            status = Unirest.post("https://api.real-debrid.com/rest/1.0/torrents/selectFiles/" + torrentID)
                    .header("Authorization", "Bearer " + ConfigProvider.API_KEYS.realDebridKey())
                    .field("files", fileIds).asString().getStatus();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        return status;
    }

    public static UnrestrictedLink unrestrictLink(String link) {
        HttpResponse<UnrestrictedLink> response = null;
        try {
            response = Unirest.post("https://api.real-debrid.com/rest/1.0/unrestrict/link")
                    .header("Authorization", "Bearer " + ConfigProvider.API_KEYS.realDebridKey())
                    .field("link", link)
                    .asObject(UnrestrictedLink.class);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return response.getBody();
    }

    public static int deleteTorrent(String torrentId) {
        int status = 0;
        try {
            status = Unirest.delete("https://api.real-debrid.com/rest/1.0/torrents/delete/{id}")
                    .header("Authorization", "Bearer " + ConfigProvider.API_KEYS.realDebridKey())
                    .routeParam("id", torrentId)
                    .asString().getStatus();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        return status;
    }
}
