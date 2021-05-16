package net.celestialdata.plexbot.clients.models.tvdb.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public enum TvdbSeasonTypeNameEnum {

    @JsonProperty(value = "Absolute Order")
    ABSOLUTE_ORDER,

    @JsonProperty(value = "Alternate Order")
    ALTERNATE_ORDER,

    @JsonProperty(value = "Regional Order")
    REGIONAL_ORDER,

    @JsonProperty(value = "Alternate DVD Order")
    ALTERNATE_DVD_ORDER,

    @JsonProperty(value = "Aired Order")
    AIRED_ORDER,

    @JsonProperty(value = "DVD Order")
    DVD_ORDER
}
