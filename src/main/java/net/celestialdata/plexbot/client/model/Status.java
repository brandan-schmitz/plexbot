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

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * status record
 */
@ApiModel(description = "status record")
@javax.annotation.
        Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen",
        date = "2021-04-15T16:42:07.120298-05:00[America/Chicago]")
public class Status {
    public static final String SERIALIZED_NAME_ID = "id";
    public static final String SERIALIZED_NAME_KEEP_UPDATED = "keepUpdated";
    public static final String SERIALIZED_NAME_NAME = "name";
    public static final String SERIALIZED_NAME_RECORD_TYPE = "recordType";
    @SerializedName(SERIALIZED_NAME_ID)
    private Long id;
    @SerializedName(SERIALIZED_NAME_KEEP_UPDATED)
    private Boolean keepUpdated;
    @SerializedName(SERIALIZED_NAME_NAME)
    private String name;
    @SerializedName(SERIALIZED_NAME_RECORD_TYPE)
    private String recordType;

    public Status id(Long id) {

        this.id = id;
        return this;
    }

    /**
     * Get id
     *
     * @return id
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Status keepUpdated(Boolean keepUpdated) {

        this.keepUpdated = keepUpdated;
        return this;
    }

    /**
     * Get keepUpdated
     *
     * @return keepUpdated
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public Boolean getKeepUpdated() {
        return keepUpdated;
    }

    public void setKeepUpdated(Boolean keepUpdated) {
        this.keepUpdated = keepUpdated;
    }

    public Status name(String name) {

        this.name = name;
        return this;
    }

    /**
     * Get name
     *
     * @return name
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status recordType(String recordType) {

        this.recordType = recordType;
        return this;
    }

    /**
     * Get recordType
     *
     * @return recordType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Status status = (Status) o;
        return Objects.equals(this.id, status.id) &&
                Objects.equals(this.keepUpdated, status.keepUpdated) &&
                Objects.equals(this.name, status.name) &&
                Objects.equals(this.recordType, status.recordType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, keepUpdated, name, recordType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Status {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    keepUpdated: ")
                .append(toIndentedString(keepUpdated))
                .append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    recordType: ")
                .append(toIndentedString(recordType))
                .append("\n");
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
}
