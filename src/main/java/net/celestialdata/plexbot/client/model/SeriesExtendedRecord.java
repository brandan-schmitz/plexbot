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
 * The extended record for a series
 */
@ApiModel(description = "The extended record for a series")
@javax.annotation.
        Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen",
        date = "2021-04-15T16:42:07.120298-05:00[America/Chicago]")
public class SeriesExtendedRecord {
    public static final String SERIALIZED_NAME_ABBREVIATION = "abbreviation";
    public static final String SERIALIZED_NAME_AIRS_DAYS = "airsDays";
    public static final String SERIALIZED_NAME_AIRS_TIME = "airsTime";
    public static final String SERIALIZED_NAME_ALIASES = "aliases";
    public static final String SERIALIZED_NAME_ARTWORKS = "artworks";
    public static final String SERIALIZED_NAME_CHARACTERS = "characters";
    public static final String SERIALIZED_NAME_COUNTRY = "country";
    public static final String SERIALIZED_NAME_DEFAULT_SEASON_TYPE =
            "defaultSeasonType";
    public static final String SERIALIZED_NAME_FIRST_AIRED = "firstAired";
    public static final String SERIALIZED_NAME_LISTS = "lists";
    public static final String SERIALIZED_NAME_GENRES = "genres";
    public static final String SERIALIZED_NAME_ID = "id";
    public static final String SERIALIZED_NAME_IMAGE = "image";
    public static final String SERIALIZED_NAME_IS_ORDER_RANDOMIZED =
            "isOrderRandomized";
    public static final String SERIALIZED_NAME_LAST_AIRED = "lastAired";
    public static final String SERIALIZED_NAME_NAME = "name";
    public static final String SERIALIZED_NAME_NAME_TRANSLATIONS =
            "nameTranslations";
    public static final String SERIALIZED_NAME_NETWORKS = "networks";
    public static final String SERIALIZED_NAME_NEXT_AIRED = "nextAired";
    public static final String SERIALIZED_NAME_ORIGINAL_COUNTRY =
            "originalCountry";
    public static final String SERIALIZED_NAME_ORIGINAL_LANGUAGE =
            "originalLanguage";
    public static final String SERIALIZED_NAME_OVERVIEW_TRANSLATIONS =
            "overviewTranslations";
    public static final String SERIALIZED_NAME_REMOTE_IDS = "remoteIds";
    public static final String SERIALIZED_NAME_SCORE = "score";
    public static final String SERIALIZED_NAME_SEASONS = "seasons";
    public static final String SERIALIZED_NAME_SLUG = "slug";
    public static final String SERIALIZED_NAME_STATUS = "status";
    public static final String SERIALIZED_NAME_TRAILERS = "trailers";
    @SerializedName(SERIALIZED_NAME_ABBREVIATION)
    private String abbreviation;
    @SerializedName(SERIALIZED_NAME_AIRS_DAYS)
    private SeriesAirsDays airsDays;
    @SerializedName(SERIALIZED_NAME_AIRS_TIME)
    private String airsTime;
    @SerializedName(SERIALIZED_NAME_ALIASES)
    private List<Alias> aliases = null;
    @SerializedName(SERIALIZED_NAME_ARTWORKS)
    private List<ArtworkBaseRecord> artworks = null;
    @SerializedName(SERIALIZED_NAME_CHARACTERS)
    private List<Character> characters = null;
    @SerializedName(SERIALIZED_NAME_COUNTRY)
    private String country;
    @SerializedName(SERIALIZED_NAME_DEFAULT_SEASON_TYPE)
    private Long defaultSeasonType;
    @SerializedName(SERIALIZED_NAME_FIRST_AIRED)
    private String firstAired;
    @SerializedName(SERIALIZED_NAME_LISTS)
    private List<ListBaseRecord> lists = null;
    @SerializedName(SERIALIZED_NAME_GENRES)
    private List<GenreBaseRecord> genres = null;
    @SerializedName(SERIALIZED_NAME_ID)
    private Integer id;
    @SerializedName(SERIALIZED_NAME_IMAGE)
    private String image;
    @SerializedName(SERIALIZED_NAME_IS_ORDER_RANDOMIZED)
    private Boolean isOrderRandomized;
    @SerializedName(SERIALIZED_NAME_LAST_AIRED)
    private String lastAired;
    @SerializedName(SERIALIZED_NAME_NAME)
    private String name;
    @SerializedName(SERIALIZED_NAME_NAME_TRANSLATIONS)
    private List<String> nameTranslations = null;
    @SerializedName(SERIALIZED_NAME_NETWORKS)
    private List<NetworkBaseRecord> networks = null;
    @SerializedName(SERIALIZED_NAME_NEXT_AIRED)
    private String nextAired;
    @SerializedName(SERIALIZED_NAME_ORIGINAL_COUNTRY)
    private String originalCountry;
    @SerializedName(SERIALIZED_NAME_ORIGINAL_LANGUAGE)
    private String originalLanguage;
    @SerializedName(SERIALIZED_NAME_OVERVIEW_TRANSLATIONS)
    private List<String> overviewTranslations = null;
    @SerializedName(SERIALIZED_NAME_REMOTE_IDS)
    private List<RemoteID> remoteIds = null;
    @SerializedName(SERIALIZED_NAME_SCORE)
    private Double score;
    @SerializedName(SERIALIZED_NAME_SEASONS)
    private List<SeasonBaseRecord> seasons = null;
    @SerializedName(SERIALIZED_NAME_SLUG)
    private String slug;
    @SerializedName(SERIALIZED_NAME_STATUS)
    private Status status;
    @SerializedName(SERIALIZED_NAME_TRAILERS)
    private List<Trailer> trailers = null;

    public SeriesExtendedRecord abbreviation(String abbreviation) {

        this.abbreviation = abbreviation;
        return this;
    }

    /**
     * Get abbreviation
     *
     * @return abbreviation
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public SeriesExtendedRecord airsDays(SeriesAirsDays airsDays) {

        this.airsDays = airsDays;
        return this;
    }

    /**
     * Get airsDays
     *
     * @return airsDays
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public SeriesAirsDays getAirsDays() {
        return airsDays;
    }

    public void setAirsDays(SeriesAirsDays airsDays) {
        this.airsDays = airsDays;
    }

    public SeriesExtendedRecord airsTime(String airsTime) {

        this.airsTime = airsTime;
        return this;
    }

    /**
     * Get airsTime
     *
     * @return airsTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public String getAirsTime() {
        return airsTime;
    }

    public void setAirsTime(String airsTime) {
        this.airsTime = airsTime;
    }

    public SeriesExtendedRecord aliases(List<Alias> aliases) {

        this.aliases = aliases;
        return this;
    }

    public SeriesExtendedRecord addAliasesItem(Alias aliasesItem) {
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

    public SeriesExtendedRecord artworks(List<ArtworkBaseRecord> artworks) {

        this.artworks = artworks;
        return this;
    }

    public SeriesExtendedRecord addArtworksItem(ArtworkBaseRecord artworksItem) {
        if (this.artworks == null) {
            this.artworks = new ArrayList<>();
        }
        this.artworks.add(artworksItem);
        return this;
    }

    /**
     * Get artworks
     *
     * @return artworks
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public List<ArtworkBaseRecord> getArtworks() {
        return artworks;
    }

    public void setArtworks(List<ArtworkBaseRecord> artworks) {
        this.artworks = artworks;
    }

    public SeriesExtendedRecord characters(List<Character> characters) {

        this.characters = characters;
        return this;
    }

    public SeriesExtendedRecord addCharactersItem(Character charactersItem) {
        if (this.characters == null) {
            this.characters = new ArrayList<>();
        }
        this.characters.add(charactersItem);
        return this;
    }

    /**
     * Get characters
     *
     * @return characters
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public List<Character> getCharacters() {
        return characters;
    }

    public void setCharacters(List<Character> characters) {
        this.characters = characters;
    }

    public SeriesExtendedRecord country(String country) {

        this.country = country;
        return this;
    }

    /**
     * Get country
     *
     * @return country
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public SeriesExtendedRecord defaultSeasonType(Long defaultSeasonType) {

        this.defaultSeasonType = defaultSeasonType;
        return this;
    }

    /**
     * Get defaultSeasonType
     *
     * @return defaultSeasonType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public Long getDefaultSeasonType() {
        return defaultSeasonType;
    }

    public void setDefaultSeasonType(Long defaultSeasonType) {
        this.defaultSeasonType = defaultSeasonType;
    }

    public SeriesExtendedRecord firstAired(String firstAired) {

        this.firstAired = firstAired;
        return this;
    }

    /**
     * Get firstAired
     *
     * @return firstAired
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public String getFirstAired() {
        return firstAired;
    }

    public void setFirstAired(String firstAired) {
        this.firstAired = firstAired;
    }

    public SeriesExtendedRecord lists(List<ListBaseRecord> lists) {

        this.lists = lists;
        return this;
    }

    public SeriesExtendedRecord addListsItem(ListBaseRecord listsItem) {
        if (this.lists == null) {
            this.lists = new ArrayList<>();
        }
        this.lists.add(listsItem);
        return this;
    }

    /**
     * Get lists
     *
     * @return lists
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public List<ListBaseRecord> getLists() {
        return lists;
    }

    public void setLists(List<ListBaseRecord> lists) {
        this.lists = lists;
    }

    public SeriesExtendedRecord genres(List<GenreBaseRecord> genres) {

        this.genres = genres;
        return this;
    }

    public SeriesExtendedRecord addGenresItem(GenreBaseRecord genresItem) {
        if (this.genres == null) {
            this.genres = new ArrayList<>();
        }
        this.genres.add(genresItem);
        return this;
    }

    /**
     * Get genres
     *
     * @return genres
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public List<GenreBaseRecord> getGenres() {
        return genres;
    }

    public void setGenres(List<GenreBaseRecord> genres) {
        this.genres = genres;
    }

    public SeriesExtendedRecord id(Integer id) {

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SeriesExtendedRecord image(String image) {

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

    public SeriesExtendedRecord isOrderRandomized(Boolean isOrderRandomized) {

        this.isOrderRandomized = isOrderRandomized;
        return this;
    }

    /**
     * Get isOrderRandomized
     *
     * @return isOrderRandomized
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public Boolean getIsOrderRandomized() {
        return isOrderRandomized;
    }

    public void setIsOrderRandomized(Boolean isOrderRandomized) {
        this.isOrderRandomized = isOrderRandomized;
    }

    public SeriesExtendedRecord lastAired(String lastAired) {

        this.lastAired = lastAired;
        return this;
    }

    /**
     * Get lastAired
     *
     * @return lastAired
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public String getLastAired() {
        return lastAired;
    }

    public void setLastAired(String lastAired) {
        this.lastAired = lastAired;
    }

    public SeriesExtendedRecord name(String name) {

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

    public SeriesExtendedRecord nameTranslations(List<String> nameTranslations) {

        this.nameTranslations = nameTranslations;
        return this;
    }

    public SeriesExtendedRecord
    addNameTranslationsItem(String nameTranslationsItem) {
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

    public SeriesExtendedRecord networks(List<NetworkBaseRecord> networks) {

        this.networks = networks;
        return this;
    }

    public SeriesExtendedRecord addNetworksItem(NetworkBaseRecord networksItem) {
        if (this.networks == null) {
            this.networks = new ArrayList<>();
        }
        this.networks.add(networksItem);
        return this;
    }

    /**
     * Get networks
     *
     * @return networks
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public List<NetworkBaseRecord> getNetworks() {
        return networks;
    }

    public void setNetworks(List<NetworkBaseRecord> networks) {
        this.networks = networks;
    }

    public SeriesExtendedRecord nextAired(String nextAired) {

        this.nextAired = nextAired;
        return this;
    }

    /**
     * Get nextAired
     *
     * @return nextAired
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public String getNextAired() {
        return nextAired;
    }

    public void setNextAired(String nextAired) {
        this.nextAired = nextAired;
    }

    public SeriesExtendedRecord originalCountry(String originalCountry) {

        this.originalCountry = originalCountry;
        return this;
    }

    /**
     * Get originalCountry
     *
     * @return originalCountry
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public String getOriginalCountry() {
        return originalCountry;
    }

    public void setOriginalCountry(String originalCountry) {
        this.originalCountry = originalCountry;
    }

    public SeriesExtendedRecord originalLanguage(String originalLanguage) {

        this.originalLanguage = originalLanguage;
        return this;
    }

    /**
     * Get originalLanguage
     *
     * @return originalLanguage
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public SeriesExtendedRecord
    overviewTranslations(List<String> overviewTranslations) {

        this.overviewTranslations = overviewTranslations;
        return this;
    }

    public SeriesExtendedRecord
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

    public SeriesExtendedRecord remoteIds(List<RemoteID> remoteIds) {

        this.remoteIds = remoteIds;
        return this;
    }

    public SeriesExtendedRecord addRemoteIdsItem(RemoteID remoteIdsItem) {
        if (this.remoteIds == null) {
            this.remoteIds = new ArrayList<>();
        }
        this.remoteIds.add(remoteIdsItem);
        return this;
    }

    /**
     * Get remoteIds
     *
     * @return remoteIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public List<RemoteID> getRemoteIds() {
        return remoteIds;
    }

    public void setRemoteIds(List<RemoteID> remoteIds) {
        this.remoteIds = remoteIds;
    }

    public SeriesExtendedRecord score(Double score) {

        this.score = score;
        return this;
    }

    /**
     * Get score
     *
     * @return score
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public SeriesExtendedRecord seasons(List<SeasonBaseRecord> seasons) {

        this.seasons = seasons;
        return this;
    }

    public SeriesExtendedRecord addSeasonsItem(SeasonBaseRecord seasonsItem) {
        if (this.seasons == null) {
            this.seasons = new ArrayList<>();
        }
        this.seasons.add(seasonsItem);
        return this;
    }

    /**
     * Get seasons
     *
     * @return seasons
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public List<SeasonBaseRecord> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<SeasonBaseRecord> seasons) {
        this.seasons = seasons;
    }

    public SeriesExtendedRecord slug(String slug) {

        this.slug = slug;
        return this;
    }

    /**
     * Get slug
     *
     * @return slug
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public SeriesExtendedRecord status(Status status) {

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public SeriesExtendedRecord trailers(List<Trailer> trailers) {

        this.trailers = trailers;
        return this;
    }

    public SeriesExtendedRecord addTrailersItem(Trailer trailersItem) {
        if (this.trailers == null) {
            this.trailers = new ArrayList<>();
        }
        this.trailers.add(trailersItem);
        return this;
    }

    /**
     * Get trailers
     *
     * @return trailers
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")

    public List<Trailer> getTrailers() {
        return trailers;
    }

    public void setTrailers(List<Trailer> trailers) {
        this.trailers = trailers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SeriesExtendedRecord seriesExtendedRecord = (SeriesExtendedRecord) o;
        return Objects.equals(this.abbreviation,
                seriesExtendedRecord.abbreviation) &&
                Objects.equals(this.airsDays, seriesExtendedRecord.airsDays) &&
                Objects.equals(this.airsTime, seriesExtendedRecord.airsTime) &&
                Objects.equals(this.aliases, seriesExtendedRecord.aliases) &&
                Objects.equals(this.artworks, seriesExtendedRecord.artworks) &&
                Objects.equals(this.characters, seriesExtendedRecord.characters) &&
                Objects.equals(this.country, seriesExtendedRecord.country) &&
                Objects.equals(this.defaultSeasonType,
                        seriesExtendedRecord.defaultSeasonType) &&
                Objects.equals(this.firstAired, seriesExtendedRecord.firstAired) &&
                Objects.equals(this.lists, seriesExtendedRecord.lists) &&
                Objects.equals(this.genres, seriesExtendedRecord.genres) &&
                Objects.equals(this.id, seriesExtendedRecord.id) &&
                Objects.equals(this.image, seriesExtendedRecord.image) &&
                Objects.equals(this.isOrderRandomized,
                        seriesExtendedRecord.isOrderRandomized) &&
                Objects.equals(this.lastAired, seriesExtendedRecord.lastAired) &&
                Objects.equals(this.name, seriesExtendedRecord.name) &&
                Objects.equals(this.nameTranslations,
                        seriesExtendedRecord.nameTranslations) &&
                Objects.equals(this.networks, seriesExtendedRecord.networks) &&
                Objects.equals(this.nextAired, seriesExtendedRecord.nextAired) &&
                Objects.equals(this.originalCountry,
                        seriesExtendedRecord.originalCountry) &&
                Objects.equals(this.originalLanguage,
                        seriesExtendedRecord.originalLanguage) &&
                Objects.equals(this.overviewTranslations,
                        seriesExtendedRecord.overviewTranslations) &&
                Objects.equals(this.remoteIds, seriesExtendedRecord.remoteIds) &&
                Objects.equals(this.score, seriesExtendedRecord.score) &&
                Objects.equals(this.seasons, seriesExtendedRecord.seasons) &&
                Objects.equals(this.slug, seriesExtendedRecord.slug) &&
                Objects.equals(this.status, seriesExtendedRecord.status) &&
                Objects.equals(this.trailers, seriesExtendedRecord.trailers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(abbreviation, airsDays, airsTime, aliases, artworks,
                characters, country, defaultSeasonType, firstAired,
                lists, genres, id, image, isOrderRandomized, lastAired,
                name, nameTranslations, networks, nextAired,
                originalCountry, originalLanguage, overviewTranslations,
                remoteIds, score, seasons, slug, status, trailers);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SeriesExtendedRecord {\n");
        sb.append("    abbreviation: ")
                .append(toIndentedString(abbreviation))
                .append("\n");
        sb.append("    airsDays: ").append(toIndentedString(airsDays)).append("\n");
        sb.append("    airsTime: ").append(toIndentedString(airsTime)).append("\n");
        sb.append("    aliases: ").append(toIndentedString(aliases)).append("\n");
        sb.append("    artworks: ").append(toIndentedString(artworks)).append("\n");
        sb.append("    characters: ")
                .append(toIndentedString(characters))
                .append("\n");
        sb.append("    country: ").append(toIndentedString(country)).append("\n");
        sb.append("    defaultSeasonType: ")
                .append(toIndentedString(defaultSeasonType))
                .append("\n");
        sb.append("    firstAired: ")
                .append(toIndentedString(firstAired))
                .append("\n");
        sb.append("    lists: ").append(toIndentedString(lists)).append("\n");
        sb.append("    genres: ").append(toIndentedString(genres)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    image: ").append(toIndentedString(image)).append("\n");
        sb.append("    isOrderRandomized: ")
                .append(toIndentedString(isOrderRandomized))
                .append("\n");
        sb.append("    lastAired: ")
                .append(toIndentedString(lastAired))
                .append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    nameTranslations: ")
                .append(toIndentedString(nameTranslations))
                .append("\n");
        sb.append("    networks: ").append(toIndentedString(networks)).append("\n");
        sb.append("    nextAired: ")
                .append(toIndentedString(nextAired))
                .append("\n");
        sb.append("    originalCountry: ")
                .append(toIndentedString(originalCountry))
                .append("\n");
        sb.append("    originalLanguage: ")
                .append(toIndentedString(originalLanguage))
                .append("\n");
        sb.append("    overviewTranslations: ")
                .append(toIndentedString(overviewTranslations))
                .append("\n");
        sb.append("    remoteIds: ")
                .append(toIndentedString(remoteIds))
                .append("\n");
        sb.append("    score: ").append(toIndentedString(score)).append("\n");
        sb.append("    seasons: ").append(toIndentedString(seasons)).append("\n");
        sb.append("    slug: ").append(toIndentedString(slug)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    trailers: ").append(toIndentedString(trailers)).append("\n");
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
