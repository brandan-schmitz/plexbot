package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.celestialdata.plexbot.clients.models.tvdb.enums.TvdbSeasonTypeNameEnum;
import net.celestialdata.plexbot.clients.models.tvdb.enums.TvdbSeasonTypeTypeEnum;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbSeasonType {
    public int id;
    public TvdbSeasonTypeNameEnum name;
    public TvdbSeasonTypeTypeEnum type;
}