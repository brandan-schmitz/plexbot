package net.celestialdata.plexbot.workhandlers;

import net.celestialdata.plexbot.client.ApiException;
import net.celestialdata.plexbot.client.BotClient;
import net.celestialdata.plexbot.client.model.YtsMovieInfo;
import net.celestialdata.plexbot.client.model.YtsSearchResult;
import net.celestialdata.plexbot.client.model.YtsTorrentInfo;

import java.util.ArrayList;
import java.util.List;

public class TorrentHandler {
    private final String movieId;
    private List<YtsMovieInfo> movieList = new ArrayList<>();
    private List<YtsTorrentInfo> torrents = new ArrayList<>();
    private YtsSearchResult response;
    private String magnetLink;
    private YtsTorrentInfo selectedTorrent = new YtsTorrentInfo();

    public TorrentHandler(String movieId) {
        this.movieId = movieId;
    }

    public void searchYts() throws ApiException {
        response = BotClient.getInstance().ytsApi.searchYts(this.movieId).getData();
    }

    public boolean didSearchReturnNoResults() {
        return response.getMovieCount() == 0;
    }

    public void buildMovieList() {
        movieList = response.getMovies();
    }

    public void buildTorrentList() {
        for (YtsMovieInfo movieInfo : movieList) {
            if (movieInfo.getImdbCode().equalsIgnoreCase(movieId)) {
                torrents = movieInfo.getTorrents();
            }
        }
    }

    public boolean didBuildMovieListFail() {
        return movieList == null;
    }

    public boolean areNoTorrentsAvailable() {
        return torrents.size() == 0;
    }

    private void chooseBestTorrent() {
        for (YtsTorrentInfo torrent : torrents) {
            if (torrent.getQuality().contains("720")) {
                selectedTorrent = torrent;
            }
        }

        for (YtsTorrentInfo torrent : torrents) {
            if (torrent.getQuality().contains("720") && torrent.getType().contains("bluray")) {
                selectedTorrent = torrent;
            }
        }

        for (YtsTorrentInfo torrent : torrents) {
            if (torrent.getQuality().contains("1080")) {
                selectedTorrent = torrent;
            }
        }

        for (YtsTorrentInfo torrent : torrents) {
            if (torrent.getQuality().contains("1080") && torrent.getType().contains("bluray")) {
                selectedTorrent = torrent;
            }
        }

        for (YtsTorrentInfo torrent : torrents) {
            if (torrent.getQuality().contains("2160")) {
                selectedTorrent = torrent;
            }
        }

        for (YtsTorrentInfo torrent : torrents) {
            if (torrent.getQuality().contains("2160") && torrent.getType().contains("bluray")) {
                selectedTorrent = torrent;
            }
        }
    }

    public void generateMagnetLink() {
        chooseBestTorrent();
        magnetLink = "magnet:?xt=urn:btih:" + selectedTorrent.getHash() + "&tr=udp://open.demonii.com:1337/announce&tr=" +
                "udp://tracker.openbittorrent.com:80&tr=udp://tracker.coppersurfer.tk:6969&tr=" +
                "udp://glotorrents.pw:6969/announce&tr=udp://tracker.opentrackr.org:1337/announce&tr=" +
                "udp://torrent.gresille.org:80/announce&tr=udp://p4p.arenabg.com:1337&tr=" +
                "udp://tracker.leechers-paradise.org:6969";
    }

    public boolean isNotMagnetLink() {
        return magnetLink.isEmpty();
    }

    public String getMagnetLink() {
        return magnetLink;
    }

    public int getTorrentQuality() {
        int quality = 0;

        if (selectedTorrent.getQuality().contains("720")) {
            quality = 720;
        } else if (selectedTorrent.getQuality().contains("1080")) {
            quality = 1080;
        } else if (selectedTorrent.getQuality().contains("2160")) {
            quality = 2160;
        }

        return quality;
    }

    public String getTorrentSize() {
        return selectedTorrent.getSize();
    }
}