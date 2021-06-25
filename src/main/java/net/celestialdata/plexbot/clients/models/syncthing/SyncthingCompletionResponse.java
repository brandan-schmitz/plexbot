package net.celestialdata.plexbot.clients.models.syncthing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncthingCompletionResponse {
    public double completion;
    public long globalBytes;
    public int globalItems;
    public long needBytes;
    public int needDeletes;
    public int needItems;
    public int sequence;
}