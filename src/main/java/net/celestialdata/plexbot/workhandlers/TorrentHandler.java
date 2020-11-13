package net.celestialdata.plexbot.workhandlers;

import net.celestialdata.plexbot.apis.yts.Yts;
import net.celestialdata.plexbot.apis.yts.objects.YtsMovie;
import net.celestialdata.plexbot.apis.yts.objects.YtsSearchResponse;
import net.celestialdata.plexbot.apis.yts.objects.YtsTorrent;

import java.util.ArrayList;

public class TorrentHandler {
    private final String movieId;
    private ArrayList<YtsMovie> movieList = new ArrayList<>();
    private ArrayList<YtsTorrent> torrents = new ArrayList<>();
    private YtsSearchResponse response;
    private String magnetLink;
    private YtsTorrent selectedTorrent = new YtsTorrent();

    public TorrentHandler(String movieId) {
        this.movieId = movieId;
    }

    public void searchYts() {
        response = Yts.search(movieId);
    }

    public boolean didSearchFail() {
        return !response.status_message.equalsIgnoreCase("Query was successful");
    }

    public boolean didSearchReturnNoResults() {
        return response.data.movie_count == 0;
    }

    public void buildMovieList() {
        movieList = response.data.movies;
    }

    public void buildTorrentList() {
        for (YtsMovie movie : movieList) {
            if (movie.imdb_code.equalsIgnoreCase(movieId)) {
                torrents = movie.torrents;
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
        for (YtsTorrent torrent : torrents) {
            if (torrent.quality.contains("720")) {
                selectedTorrent = torrent;
            }
        }

        for (YtsTorrent torrent : torrents) {
            if (torrent.quality.contains("720") && torrent.type.contains("bluray")) {
                selectedTorrent = torrent;
            }
        }

        for (YtsTorrent torrent : torrents) {
            if (torrent.quality.contains("1080")) {
                selectedTorrent = torrent;
            }
        }

        for (YtsTorrent torrent : torrents) {
            if (torrent.quality.contains("1080") && torrent.type.contains("bluray")) {
                selectedTorrent = torrent;
            }
        }

        for (YtsTorrent torrent : torrents) {
            if (torrent.quality.contains("2160")) {
                selectedTorrent = torrent;
            }
        }

        for (YtsTorrent torrent : torrents) {
            if (torrent.quality.contains("2160") && torrent.type.contains("bluray")) {
                selectedTorrent = torrent;
            }
        }
    }

    public void generateMagnetLink() {
        chooseBestTorrent();
        magnetLink = "magnet:?xt=urn:btih:" + selectedTorrent.hash + "&tr=udp://open.demonii.com:1337/announce&tr=" +
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

        if (selectedTorrent.quality.contains("720")) {
            quality = 720;
        } else if (selectedTorrent.quality.contains("1080")) {
            quality = 1080;
        } else if (selectedTorrent.quality.contains("2160")) {
            quality = 2160;
        }

        return quality;
    }

    public String getTorrentSize() {
        return selectedTorrent.size;
    }
}