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
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * InlineResponse2002
 */
@javax.annotation.
        Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen",
        date = "2021-04-15T16:42:07.120298-05:00[America/Chicago]")
public class InlineResponse2002 {
    public static final String SERIALIZED_NAME_DATA = "data";
    public static final String SERIALIZED_NAME_STATUS = "status";
    @SerializedName(SERIALIZED_NAME_DATA)
    private AuthToken data;
    @SerializedName(SERIALIZED_NAME_STATUS)
    private String status;

    public InlineResponse2002 data(AuthToken data) {

        this.data = data;
        return this;
    }

    /**
     * Get data
     *
     * @return data
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public AuthToken getData() {
        return data;
    }

    public void setData(AuthToken data) {
        this.data = data;
    }

    public InlineResponse2002 status(String status) {

        this.status = status;
        return this;
    }

    /**
     * Get status
     *
     * @return status
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InlineResponse2002 inlineResponse2002 = (InlineResponse2002) o;
        return Objects.equals(this.data, inlineResponse2002.data) &&
                Objects.equals(this.status, inlineResponse2002.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, status);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class InlineResponse2002 {\n");
        sb.append("    data: ").append(toIndentedString(data)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
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
