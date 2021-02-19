/*
 * Plexbot
 * Provides all the API functions necessary for the Plexbot to function.
 *
 * The version of the OpenAPI document: 1.0.0
 * Contact: brandan.schmitz@celestialdata.net
 *
 * NOTE: This class is auto generated by OpenAPI Generator
 * (https://openapi-generator.tech). https://openapi-generator.tech Do not edit
 * the class manually.
 */

package net.celestialdata.plexbot.client.model;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Information on a real-debrid torrent
 */
@ApiModel(description = "Information on a real-debrid torrent")
@javax.annotation.
        Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen",
        date = "2021-02-18T23:30:04.504837-06:00[America/Chicago]")
public class RdbTorrentInfo {
    public static final String SERIALIZED_NAME_ID = "id";
    public static final String SERIALIZED_NAME_FILENAME = "filename";
    public static final String SERIALIZED_NAME_ORIGINAL_FILENAME =
            "original_filename";
    public static final String SERIALIZED_NAME_HASH = "hash";
    public static final String SERIALIZED_NAME_BYTES = "bytes";
    public static final String SERIALIZED_NAME_ORIGINAL_BYTES = "original_bytes";
    public static final String SERIALIZED_NAME_HOST = "host";
    public static final String SERIALIZED_NAME_SPLIT = "split";
    public static final String SERIALIZED_NAME_PROGRESS = "progress";
    public static final String SERIALIZED_NAME_STATUS = "status";
    public static final String SERIALIZED_NAME_ADDED = "added";
    public static final String SERIALIZED_NAME_FILES = "files";
    public static final String SERIALIZED_NAME_LINKS = "links";
    public static final String SERIALIZED_NAME_ENDED = "ended";
    public static final String SERIALIZED_NAME_SPEED = "speed";
    public static final String SERIALIZED_NAME_SEEDERS = "seeders";
    @SerializedName(SERIALIZED_NAME_ID)
    private String id;
    @SerializedName(SERIALIZED_NAME_FILENAME)
    private String filename;
    @SerializedName(SERIALIZED_NAME_ORIGINAL_FILENAME)
    private String originalFilename;
    @SerializedName(SERIALIZED_NAME_HASH)
    private String hash;
    @SerializedName(SERIALIZED_NAME_BYTES)
    private Long bytes;
    @SerializedName(SERIALIZED_NAME_ORIGINAL_BYTES)
    private Long originalBytes;
    @SerializedName(SERIALIZED_NAME_HOST)
    private String host;
    @SerializedName(SERIALIZED_NAME_SPLIT)
    private Integer split;
    @SerializedName(SERIALIZED_NAME_PROGRESS)
    private Integer progress;
    @SerializedName(SERIALIZED_NAME_STATUS)
    private StatusEnum status;
    @SerializedName(SERIALIZED_NAME_ADDED)
    private String added;
    @SerializedName(SERIALIZED_NAME_FILES)
    private List<RdbTorrentFile> files = null;
    @SerializedName(SERIALIZED_NAME_LINKS)
    private List<URI> links = null;
    @SerializedName(SERIALIZED_NAME_ENDED)
    private String ended;
    @SerializedName(SERIALIZED_NAME_SPEED)
    private Integer speed;
    @SerializedName(SERIALIZED_NAME_SEEDERS)
    private Integer seeders;

    @SuppressWarnings("unused")
    public RdbTorrentInfo id(String id) {

        this.id = id;
        return this;
    }

    /**
     * Torrent ID
     *
     * @return id
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Torrent ID")

    public String getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(String id) {
        this.id = id;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo filename(String filename) {

        this.filename = filename;
        return this;
    }

    /**
     * Torrent filename
     *
     * @return filename
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Torrent filename")

    public String getFilename() {
        return filename;
    }

    @SuppressWarnings("unused")
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo originalFilename(String originalFilename) {

        this.originalFilename = originalFilename;
        return this;
    }

    /**
     * Original name of the torrent
     *
     * @return originalFilename
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Original name of the torrent")

    public String getOriginalFilename() {
        return originalFilename;
    }

    @SuppressWarnings("unused")
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo hash(String hash) {

        this.hash = hash;
        return this;
    }

    /**
     * SHA1 Hash of the torrent
     *
     * @return hash
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(value = "SHA1 Hash of the torrent")

    public String getHash() {
        return hash;
    }

    @SuppressWarnings("unused")
    public void setHash(String hash) {
        this.hash = hash;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo bytes(Long bytes) {

        this.bytes = bytes;
        return this;
    }

    /**
     * Size of selected files only
     *
     * @return bytes
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Size of selected files only")

    public Long getBytes() {
        return bytes;
    }

    @SuppressWarnings("unused")
    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo originalBytes(Long originalBytes) {

        this.originalBytes = originalBytes;
        return this;
    }

    /**
     * Total size of the torrent
     *
     * @return originalBytes
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Total size of the torrent")

    public Long getOriginalBytes() {
        return originalBytes;
    }

    @SuppressWarnings("unused")
    public void setOriginalBytes(Long originalBytes) {
        this.originalBytes = originalBytes;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo host(String host) {

        this.host = host;
        return this;
    }

    /**
     * Host main domain
     *
     * @return host
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Host main domain")

    public String getHost() {
        return host;
    }

    @SuppressWarnings("unused")
    public void setHost(String host) {
        this.host = host;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo split(Integer split) {

        this.split = split;
        return this;
    }

    /**
     * Split size of links
     *
     * @return split
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Split size of links")

    public Integer getSplit() {
        return split;
    }

    @SuppressWarnings("unused")
    public void setSplit(Integer split) {
        this.split = split;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo progress(Integer progress) {

        this.progress = progress;
        return this;
    }

    /**
     * Progress of torrent download to real-debrid
     *
     * @return progress
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Progress of torrent download to real-debrid")

    public Integer getProgress() {
        return progress;
    }

    @SuppressWarnings("unused")
    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo status(StatusEnum status) {

        this.status = status;
        return this;
    }

    /**
     * Current status of the torrent
     *
     * @return status
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Current status of the torrent")

    public StatusEnum getStatus() {
        return status;
    }

    @SuppressWarnings("unused")
    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo added(String added) {

        this.added = added;
        return this;
    }

    /**
     * Date added
     *
     * @return added
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Date added")

    public String getAdded() {
        return added;
    }

    @SuppressWarnings("unused")
    public void setAdded(String added) {
        this.added = added;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo files(List<RdbTorrentFile> files) {

        this.files = files;
        return this;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo addFilesItem(RdbTorrentFile filesItem) {
        if (this.files == null) {
            this.files = new ArrayList<>();
        }
        this.files.add(filesItem);
        return this;
    }

    /**
     * Get files
     *
     * @return files
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public List<RdbTorrentFile> getFiles() {
        return files;
    }

    @SuppressWarnings("unused")
    public void setFiles(List<RdbTorrentFile> files) {
        this.files = files;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo links(List<URI> links) {

        this.links = links;
        return this;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo addLinksItem(URI linksItem) {
        if (this.links == null) {
            this.links = new ArrayList<>();
        }
        this.links.add(linksItem);
        return this;
    }

    /**
     * Host download links
     *
     * @return links
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Host download links")

    public List<URI> getLinks() {
        return links;
    }

    @SuppressWarnings("unused")
    public void setLinks(List<URI> links) {
        this.links = links;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo ended(String ended) {

        this.ended = ended;
        return this;
    }

    /**
     * Date/Time torrent finished downloading (Only present when status is
     * downloaded)
     *
     * @return ended
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(
            value =
                    "Date/Time torrent finished downloading (Only present when status is downloaded)")

    public String
    getEnded() {
        return ended;
    }

    @SuppressWarnings("unused")
    public void setEnded(String ended) {
        this.ended = ended;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo speed(Integer speed) {

        this.speed = speed;
        return this;
    }

    /**
     * Download speed in bytes (Only present in \&quot;downloading\&quot;,
     * \&quot;compressing\&quot;, \&quot;uploading\&quot; status)
     *
     * @return speed
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(
            value =
                    "Download speed in bytes (Only present in \"downloading\", \"compressing\", \"uploading\" status)")

    public Integer
    getSpeed() {
        return speed;
    }

    @SuppressWarnings("unused")
    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    @SuppressWarnings("unused")
    public RdbTorrentInfo seeders(Integer seeders) {

        this.seeders = seeders;
        return this;
    }

    /**
     * Number of seeders (Only present in \&quot;downloading\&quot;,
     * \&quot;magnet_conversion\&quot; status)
     *
     * @return seeders
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(
            value =
                    "Number of seeders (Only present in \"downloading\", \"magnet_conversion\" status)")

    public Integer
    getSeeders() {
        return seeders;
    }

    @SuppressWarnings("unused")
    public void setSeeders(Integer seeders) {
        this.seeders = seeders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RdbTorrentInfo rdbTorrentInfo = (RdbTorrentInfo) o;
        return Objects.equals(this.id, rdbTorrentInfo.id) &&
                Objects.equals(this.filename, rdbTorrentInfo.filename) &&
                Objects.equals(this.originalFilename,
                        rdbTorrentInfo.originalFilename) &&
                Objects.equals(this.hash, rdbTorrentInfo.hash) &&
                Objects.equals(this.bytes, rdbTorrentInfo.bytes) &&
                Objects.equals(this.originalBytes, rdbTorrentInfo.originalBytes) &&
                Objects.equals(this.host, rdbTorrentInfo.host) &&
                Objects.equals(this.split, rdbTorrentInfo.split) &&
                Objects.equals(this.progress, rdbTorrentInfo.progress) &&
                Objects.equals(this.status, rdbTorrentInfo.status) &&
                Objects.equals(this.added, rdbTorrentInfo.added) &&
                Objects.equals(this.files, rdbTorrentInfo.files) &&
                Objects.equals(this.links, rdbTorrentInfo.links) &&
                Objects.equals(this.ended, rdbTorrentInfo.ended) &&
                Objects.equals(this.speed, rdbTorrentInfo.speed) &&
                Objects.equals(this.seeders, rdbTorrentInfo.seeders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, filename, originalFilename, hash, bytes,
                originalBytes, host, split, progress, status, added,
                files, links, ended, speed, seeders);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RdbTorrentInfo {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    filename: ").append(toIndentedString(filename)).append("\n");
        sb.append("    originalFilename: ")
                .append(toIndentedString(originalFilename))
                .append("\n");
        sb.append("    hash: ").append(toIndentedString(hash)).append("\n");
        sb.append("    bytes: ").append(toIndentedString(bytes)).append("\n");
        sb.append("    originalBytes: ")
                .append(toIndentedString(originalBytes))
                .append("\n");
        sb.append("    host: ").append(toIndentedString(host)).append("\n");
        sb.append("    split: ").append(toIndentedString(split)).append("\n");
        sb.append("    progress: ").append(toIndentedString(progress)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    added: ").append(toIndentedString(added)).append("\n");
        sb.append("    files: ").append(toIndentedString(files)).append("\n");
        sb.append("    links: ").append(toIndentedString(links)).append("\n");
        sb.append("    ended: ").append(toIndentedString(ended)).append("\n");
        sb.append("    speed: ").append(toIndentedString(speed)).append("\n");
        sb.append("    seeders: ").append(toIndentedString(seeders)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * Current status of the torrent
     */
    @JsonAdapter(StatusEnum.Adapter.class)
    public enum StatusEnum {
        MAGNET_ERROR("magnet_error"),

        MAGNET_CONVERSION("magnet_conversion"),

        WAITING_FILES_SELECTION("waiting_files_selection"),

        QUEUED("queued"),

        DOWNLOADING("downloading"),

        DOWNLOADED("downloaded"),

        ERROR("error"),

        VIRUS("virus"),

        COMPRESSING("compressing"),

        UPLOADING("uploading"),

        DEAD("dead");

        private final String value;

        StatusEnum(String value) {
            this.value = value;
        }

        public static StatusEnum fromValue(String value) {
            for (StatusEnum b : StatusEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static class Adapter extends TypeAdapter<StatusEnum> {
            @Override
            public void write(final JsonWriter jsonWriter,
                              final StatusEnum enumeration) throws IOException {
                jsonWriter.value(enumeration.getValue());
            }

            @Override
            public StatusEnum read(final JsonReader jsonReader) throws IOException {
                String value = jsonReader.nextString();
                return StatusEnum.fromValue(value);
            }
        }
    }
}
