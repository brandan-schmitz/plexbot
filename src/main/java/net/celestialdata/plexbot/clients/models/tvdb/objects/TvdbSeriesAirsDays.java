package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbSeriesAirsDays {
    public boolean friday;
    public boolean monday;
    public boolean saturday;
    public boolean sunday;
    public boolean thursday;
    public boolean tuesday;
    public boolean wednesday;
}