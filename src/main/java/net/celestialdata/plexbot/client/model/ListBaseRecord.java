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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * base list record
 */
@ApiModel(description = "base list record")
@javax.annotation.
        Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen",
        date = "2021-04-15T16:42:07.120298-05:00[America/Chicago]")
public class ListBaseRecord {
    public static final String SERIALIZED_NAME_ALIASES = "aliases";
    public static final String SERIALIZED_NAME_ID = "id";
    public static final String SERIALIZED_NAME_IS_OFFICIAL = "isOfficial";
    public static final String SERIALIZED_NAME_NAME = "name";
    public static final String SERIALIZED_NAME_NAME_TRANSLATIONS =
            "nameTranslations";
    public static final String SERIALIZED_NAME_OVERVIEW = "overview";
    public static final String SERIALIZED_NAME_OVERVIEW_TRANSLATIONS =
            "overviewTranslations";
    public static final String SERIALIZED_NAME_URL = "url";
    @SerializedName(SERIALIZED_NAME_ALIASES)
    private List<Alias> aliases = null;
    @SerializedName(SERIALIZED_NAME_ID)
    private Long id;
    @SerializedName(SERIALIZED_NAME_IS_OFFICIAL)
    private Boolean isOfficial;
    @SerializedName(SERIALIZED_NAME_NAME)
    private String name;
    @SerializedName(SERIALIZED_NAME_NAME_TRANSLATIONS)
    private List<String> nameTranslations = null;
    @SerializedName(SERIALIZED_NAME_OVERVIEW)
    private String overview;
    @SerializedName(SERIALIZED_NAME_OVERVIEW_TRANSLATIONS)
    private List<String> overviewTranslations = null;
    @SerializedName(SERIALIZED_NAME_URL)
    private String url;

    public ListBaseRecord aliases(List<Alias> aliases) {

        this.aliases = aliases;
        return this;
    }

    public ListBaseRecord addAliasesItem(Alias aliasesItem) {
        if (this.aliases == null) {
            this.aliases = new ArrayList<>();
        }
        this.aliases.add(aliasesItem);
        return this;
    }

    /**
     * Get aliases
     *
     * @return aliases
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public List<Alias> getAliases() {
        return aliases;
    }

    public void setAliases(List<Alias> aliases) {
        this.aliases = aliases;
    }

    public ListBaseRecord id(Long id) {

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

    public ListBaseRecord isOfficial(Boolean isOfficial) {

        this.isOfficial = isOfficial;
        return this;
    }

    /**
     * Get isOfficial
     *
     * @return isOfficial
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public Boolean getIsOfficial() {
        return isOfficial;
    }

    public void setIsOfficial(Boolean isOfficial) {
        this.isOfficial = isOfficial;
    }

    public ListBaseRecord name(String name) {

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

    public ListBaseRecord nameTranslations(List<String> nameTranslations) {

        this.nameTranslations = nameTranslations;
        return this;
    }

    public ListBaseRecord addNameTranslationsItem(String nameTranslationsItem) {
        if (this.nameTranslations == null) {
            this.nameTranslations = new ArrayList<>();
        }
        this.nameTranslations.add(nameTranslationsItem);
        return this;
    }

    /**
     * Get nameTranslations
     *
     * @return nameTranslations
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public List<String> getNameTranslations() {
        return nameTranslations;
    }

    public void setNameTranslations(List<String> nameTranslations) {
        this.nameTranslations = nameTranslations;
    }

    public ListBaseRecord overview(String overview) {

        this.overview = overview;
        return this;
    }

    /**
     * Get overview
     *
     * @return overview
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public ListBaseRecord
    overviewTranslations(List<String> overviewTranslations) {

        this.overviewTranslations = overviewTranslations;
        return this;
    }

    public ListBaseRecord
    addOverviewTranslationsItem(String overviewTranslationsItem) {
        if (this.overviewTranslations == null) {
            this.overviewTranslations = new ArrayList<>();
        }
        this.overviewTranslations.add(overviewTranslationsItem);
        return this;
    }

    /**
     * Get overviewTranslations
     *
     * @return overviewTranslations
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public List<String> getOverviewTranslations() {
        return overviewTranslations;
    }

    public void setOverviewTranslations(List<String> overviewTranslations) {
        this.overviewTranslations = overviewTranslations;
    }

    public ListBaseRecord url(String url) {

        this.url = url;
        return this;
    }

    /**
     * Get url
     *
     * @return url
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ListBaseRecord listBaseRecord = (ListBaseRecord) o;
        return Objects.equals(this.aliases, listBaseRecord.aliases) &&
                Objects.equals(this.id, listBaseRecord.id) &&
                Objects.equals(this.isOfficial, listBaseRecord.isOfficial) &&
                Objects.equals(this.name, listBaseRecord.name) &&
                Objects.equals(this.nameTranslations,
                        listBaseRecord.nameTranslations) &&
                Objects.equals(this.overview, listBaseRecord.overview) &&
                Objects.equals(this.overviewTranslations,
                        listBaseRecord.overviewTranslations) &&
                Objects.equals(this.url, listBaseRecord.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aliases, id, isOfficial, name, nameTranslations,
                overview, overviewTranslations, url);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListBaseRecord {\n");
        sb.append("    aliases: ").append(toIndentedString(aliases)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    isOfficial: ")
                .append(toIndentedString(isOfficial))
                .append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    nameTranslations: ")
                .append(toIndentedString(nameTranslations))
                .append("\n");
        sb.append("    overview: ").append(toIndentedString(overview)).append("\n");
        sb.append("    overviewTranslations: ")
                .append(toIndentedString(overviewTranslations))
                .append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
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