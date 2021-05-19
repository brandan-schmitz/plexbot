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
 * character record
 */
@ApiModel(description = "character record")
@javax.annotation.
        Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen",
        date = "2021-04-15T16:42:07.120298-05:00[America/Chicago]")
public class Character {
    public static final String SERIALIZED_NAME_ALIASES = "aliases";
    public static final String SERIALIZED_NAME_EPISODE_ID = "episodeId";
    public static final String SERIALIZED_NAME_ID = "id";
    public static final String SERIALIZED_NAME_IMAGE = "image";
    public static final String SERIALIZED_NAME_IS_FEATURED = "isFeatured";
    public static final String SERIALIZED_NAME_MOVIE_ID = "movieId";
    public static final String SERIALIZED_NAME_NAME = "name";
    public static final String SERIALIZED_NAME_NAME_TRANSLATIONS =
            "nameTranslations";
    public static final String SERIALIZED_NAME_OVERVIEW_TRANSLATIONS =
            "overviewTranslations";
    public static final String SERIALIZED_NAME_PEOPLE_ID = "peopleId";
    public static final String SERIALIZED_NAME_SERIES_ID = "seriesId";
    public static final String SERIALIZED_NAME_SORT = "sort";
    public static final String SERIALIZED_NAME_TYPE = "type";
    public static final String SERIALIZED_NAME_URL = "url";
    @SerializedName(SERIALIZED_NAME_ALIASES)
    private List<Alias> aliases = null;
    @SerializedName(SERIALIZED_NAME_EPISODE_ID)
    private Integer episodeId;
    @SerializedName(SERIALIZED_NAME_ID)
    private Long id;
    @SerializedName(SERIALIZED_NAME_IMAGE)
    private String image;
    @SerializedName(SERIALIZED_NAME_IS_FEATURED)
    private Boolean isFeatured;
    @SerializedName(SERIALIZED_NAME_MOVIE_ID)
    private Integer movieId;
    @SerializedName(SERIALIZED_NAME_NAME)
    private String name;
    @SerializedName(SERIALIZED_NAME_NAME_TRANSLATIONS)
    private List<String> nameTranslations = null;
    @SerializedName(SERIALIZED_NAME_OVERVIEW_TRANSLATIONS)
    private List<String> overviewTranslations = null;
    @SerializedName(SERIALIZED_NAME_PEOPLE_ID)
    private Integer peopleId;
    @SerializedName(SERIALIZED_NAME_SERIES_ID)
    private Integer seriesId;
    @SerializedName(SERIALIZED_NAME_SORT)
    private Long sort;
    @SerializedName(SERIALIZED_NAME_TYPE)
    private Long type;
    @SerializedName(SERIALIZED_NAME_URL)
    private String url;

    public Character aliases(List<Alias> aliases) {

        this.aliases = aliases;
        return this;
    }

    public Character addAliasesItem(Alias aliasesItem) {
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

    public Character episodeId(Integer episodeId) {

        this.episodeId = episodeId;
        return this;
    }

    /**
     * Get episodeId
     *
     * @return episodeId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public Integer getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(Integer episodeId) {
        this.episodeId = episodeId;
    }

    public Character id(Long id) {

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

    public Character image(String image) {

        this.image = image;
        return this;
    }

    /**
     * Get image
     *
     * @return image
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Character isFeatured(Boolean isFeatured) {

        this.isFeatured = isFeatured;
        return this;
    }

    /**
     * Get isFeatured
     *
     * @return isFeatured
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Character movieId(Integer movieId) {

        this.movieId = movieId;
        return this;
    }

    /**
     * Get movieId
     *
     * @return movieId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public Character name(String name) {

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

    public Character nameTranslations(List<String> nameTranslations) {

        this.nameTranslations = nameTranslations;
        return this;
    }

    public Character addNameTranslationsItem(String nameTranslationsItem) {
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

    public Character overviewTranslations(List<String> overviewTranslations) {

        this.overviewTranslations = overviewTranslations;
        return this;
    }

    public Character
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

    public Character peopleId(Integer peopleId) {

        this.peopleId = peopleId;
        return this;
    }

    /**
     * Get peopleId
     *
     * @return peopleId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public Integer getPeopleId() {
        return peopleId;
    }

    public void setPeopleId(Integer peopleId) {
        this.peopleId = peopleId;
    }

    public Character seriesId(Integer seriesId) {

        this.seriesId = seriesId;
        return this;
    }

    /**
     * Get seriesId
     *
     * @return seriesId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public Integer getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Integer seriesId) {
        this.seriesId = seriesId;
    }

    public Character sort(Long sort) {

        this.sort = sort;
        return this;
    }

    /**
     * Get sort
     *
     * @return sort
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public Long getSort() {
        return sort;
    }

    public void setSort(Long sort) {
        this.sort = sort;
    }

    public Character type(Long type) {

        this.type = type;
        return this;
    }

    /**
     * Get type
     *
     * @return type
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public Character url(String url) {

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
        Character character = (Character) o;
        return Objects.equals(this.aliases, character.aliases) &&
                Objects.equals(this.episodeId, character.episodeId) &&
                Objects.equals(this.id, character.id) &&
                Objects.equals(this.image, character.image) &&
                Objects.equals(this.isFeatured, character.isFeatured) &&
                Objects.equals(this.movieId, character.movieId) &&
                Objects.equals(this.name, character.name) &&
                Objects.equals(this.nameTranslations, character.nameTranslations) &&
                Objects.equals(this.overviewTranslations,
                        character.overviewTranslations) &&
                Objects.equals(this.peopleId, character.peopleId) &&
                Objects.equals(this.seriesId, character.seriesId) &&
                Objects.equals(this.sort, character.sort) &&
                Objects.equals(this.type, character.type) &&
                Objects.equals(this.url, character.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aliases, episodeId, id, image, isFeatured, movieId,
                name, nameTranslations, overviewTranslations, peopleId,
                seriesId, sort, type, url);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Character {\n");
        sb.append("    aliases: ").append(toIndentedString(aliases)).append("\n");
        sb.append("    episodeId: ")
                .append(toIndentedString(episodeId))
                .append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    image: ").append(toIndentedString(image)).append("\n");
        sb.append("    isFeatured: ")
                .append(toIndentedString(isFeatured))
                .append("\n");
        sb.append("    movieId: ").append(toIndentedString(movieId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    nameTranslations: ")
                .append(toIndentedString(nameTranslations))
                .append("\n");
        sb.append("    overviewTranslations: ")
                .append(toIndentedString(overviewTranslations))
                .append("\n");
        sb.append("    peopleId: ").append(toIndentedString(peopleId)).append("\n");
        sb.append("    seriesId: ").append(toIndentedString(seriesId)).append("\n");
        sb.append("    sort: ").append(toIndentedString(sort)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
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