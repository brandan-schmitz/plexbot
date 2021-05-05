package net.celestialdata.plexbot.clients.models.rdb;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public enum RdbTorrentStatus {
    @JsonProperty("magnet_error")
    MAGNET_ERROR,

    @JsonProperty("magnet_conversion")
    MAGNET_CONVERSION,

    @JsonProperty("waiting_files_selection")
    WAITING_FILES_SELECTION,

    @JsonProperty("queued")
    QUEUED,

    @JsonProperty("downloading")
    DOWNLOADING,

    @JsonProperty("downloaded")
    DOWNLOADED,

    @JsonProperty("error")
    ERROR,

    @JsonProperty("virus")
    VIRUS,

    @JsonProperty("compressing")
    COMPRESSING,

    @JsonProperty("uploading")
    UPLOADING,

    @JsonProperty("dead")
    DEAD,
}