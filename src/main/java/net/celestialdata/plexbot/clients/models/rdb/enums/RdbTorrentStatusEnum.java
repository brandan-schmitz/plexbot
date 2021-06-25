package net.celestialdata.plexbot.clients.models.rdb.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public enum RdbTorrentStatusEnum {
    @JsonProperty(value = "magnet_error")
    MAGNET_ERROR,

    @JsonProperty(value = "magnet_conversion")
    MAGNET_CONVERSION,

    @JsonProperty(value = "waiting_files_selection")
    WAITING_FILES_SELECTION,

    @JsonProperty(value = "queued")
    QUEUED,

    @JsonProperty(value = "downloading")
    DOWNLOADING,

    @JsonProperty(value = "downloaded")
    DOWNLOADED,

    @JsonProperty(value = "error")
    ERROR,

    @JsonProperty(value = "virus")
    VIRUS,

    @JsonProperty(value = "compressing")
    COMPRESSING,

    @JsonProperty(value = "uploading")
    UPLOADING,

    @JsonProperty(value = "dead")
    DEAD,
}