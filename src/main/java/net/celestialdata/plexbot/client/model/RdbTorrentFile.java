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
import java.util.Objects;

/**
 * Information about a file within a real-debrid torrent
 */
@SuppressWarnings("unused")
@ApiModel(description = "Information about a file within a real-debrid torrent")
@javax.annotation.
        Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen",
        date = "2021-02-18T23:30:04.504837-06:00[America/Chicago]")
public class RdbTorrentFile {
    public static final String SERIALIZED_NAME_ID = "id";
    public static final String SERIALIZED_NAME_PATH = "path";
    public static final String SERIALIZED_NAME_BYTES = "bytes";
    public static final String SERIALIZED_NAME_SELECTED = "selected";
    @SerializedName(SERIALIZED_NAME_ID)
    private Integer id;
    @SerializedName(SERIALIZED_NAME_PATH)
    private String path;
    @SerializedName(SERIALIZED_NAME_BYTES)
    private Long bytes;
    @SerializedName(SERIALIZED_NAME_SELECTED)
    private SelectedEnum selected;

    @SuppressWarnings("unused")
    public RdbTorrentFile id(Integer id) {

        this.id = id;
        return this;
    }

    /**
     * File ID
     *
     * @return id
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(value = "File ID")

    public Integer getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(Integer id) {
        this.id = id;
    }

    @SuppressWarnings("unused")
    public RdbTorrentFile path(String path) {

        this.path = path;
        return this;
    }

    /**
     * Path to the file within the torrent folder
     *
     * @return path
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Path to the file within the torrent folder")

    public String getPath() {
        return path;
    }

    @SuppressWarnings("unused")
    public void setPath(String path) {
        this.path = path;
    }

    @SuppressWarnings("unused")
    public RdbTorrentFile bytes(Long bytes) {

        this.bytes = bytes;
        return this;
    }

    /**
     * Size of file in bytes
     *
     * @return bytes
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Size of file in bytes")

    public Long getBytes() {
        return bytes;
    }

    @SuppressWarnings("unused")
    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    @SuppressWarnings("unused")
    public RdbTorrentFile selected(SelectedEnum selected) {

        this.selected = selected;
        return this;
    }

    /**
     * Status of file selection
     *
     * @return selected
     **/
    @SuppressWarnings("unused")
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Status of file selection")

    public SelectedEnum getSelected() {
        return selected;
    }

    @SuppressWarnings("unused")
    public void setSelected(SelectedEnum selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RdbTorrentFile rdbTorrentFile = (RdbTorrentFile) o;
        return Objects.equals(this.id, rdbTorrentFile.id) &&
                Objects.equals(this.path, rdbTorrentFile.path) &&
                Objects.equals(this.bytes, rdbTorrentFile.bytes) &&
                Objects.equals(this.selected, rdbTorrentFile.selected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, bytes, selected);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RdbTorrentFile {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    bytes: ").append(toIndentedString(bytes)).append("\n");
        sb.append("    selected: ").append(toIndentedString(selected)).append("\n");
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
     * Status of file selection
     */
    @JsonAdapter(SelectedEnum.Adapter.class)
    public enum SelectedEnum {
        NUMBER_0(0),

        NUMBER_1(1);

        private final Integer value;

        @SuppressWarnings("unused")
        SelectedEnum(Integer value) {
            this.value = value;
        }

        public static SelectedEnum fromValue(Integer value) {
            for (SelectedEnum b : SelectedEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }

        public Integer getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @SuppressWarnings("unused")
        public static class Adapter extends TypeAdapter<SelectedEnum> {
            @Override
            public void write(final JsonWriter jsonWriter,
                              final SelectedEnum enumeration) throws IOException {
                jsonWriter.value(enumeration.getValue());
            }

            @Override
            public SelectedEnum read(final JsonReader jsonReader) throws IOException {
                Integer value = jsonReader.nextInt();
                return SelectedEnum.fromValue(value);
            }
        }
    }
}
