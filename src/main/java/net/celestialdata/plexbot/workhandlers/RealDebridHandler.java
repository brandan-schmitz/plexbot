package net.celestialdata.plexbot.workhandlers;

import net.celestialdata.plexbot.apis.realdebrid.RealDebrid;
import net.celestialdata.plexbot.apis.realdebrid.objects.*;

public class RealDebridHandler {
    private final String magnetLink;
    private MagnetResponse magnetResponse;
    private Torrent torrent;
    private boolean didSelectOperationFail = false;
    private UnrestrictedLink unrestrictedLink;
    private String extension;
    public final Object lock = new Object();

    public RealDebridHandler(String magnetLink) {
        this.magnetLink = magnetLink;
    }

    public void addMagnet() {
        magnetResponse = RealDebrid.addMagnet(magnetLink);
    }

    public boolean didMagnetAdditionFail() {
        return magnetResponse.id == null;
    }

    public void getTorrentInformation() {
        torrent = new Torrent();
        torrent = RealDebrid.getTorrentInfo(magnetResponse.id);
    }

    public boolean didTorrentInfoError() {
        return torrent.id == null;
    }

    public void selectTorrentFiles() {
        String filesToSelect = "";

        for (RdbFile file : torrent.files) {
            if (file.path.contains(".mp4") || file.path.contains(".MP4")) {
                filesToSelect = String.valueOf(file.id);
                extension = ".mp4";
            } else if (file.path.contains(".mkv") || file.path.contains(".MKV")) {
                filesToSelect = String.valueOf(file.id);
                extension = ".mkv";
            }
        }

        didSelectOperationFail = RealDebrid.selectFile(torrent.id, filesToSelect) != 204;
    }

    public boolean didSelectOperationFail() {
        return didSelectOperationFail;
    }

    int getProgress() {
        getTorrentInformation();
        return torrent.progress;
    }

    public boolean isNotReadyForDownload() {
        getTorrentInformation();
        return !torrent.status.equalsIgnoreCase("downloaded");
    }

    public void unrestrictLinks() {
        for (String s : torrent.links) {
            unrestrictedLink = RealDebrid.unrestrictLink(s);
        }
    }

    public boolean didUnrestrictOperationFail() {
        return unrestrictedLink.error != null;
    }

    public String getDownloadLink() {
        return unrestrictedLink.download;
    }

    public String getExtension() {
        return extension;
    }

    public void deleteTorrent() {
        RealDebrid.deleteTorrent(torrent.id);
    }
}
