package net.celestialdata.plexbot.apis.realdebrid.objects;

import java.util.List;

public class Torrent {
    public String id;
    public String filename;
    public String original_filename;
    public String hash;
    public long bytes;
    public long original_bytes;
    public String host;
    public int split;
    public int progress;
    public String status;
    public String added;
    public List<RdbFile> files;
    public String[] links;
    public String ended;
    public int speed;
    public int seeders;
}
