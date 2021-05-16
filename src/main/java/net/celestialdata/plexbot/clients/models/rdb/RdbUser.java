package net.celestialdata.plexbot.clients.models.rdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.celestialdata.plexbot.clients.models.rdb.enums.RdbUserTypeEnum;

import java.net.URI;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RdbUser {
    public int id;
    public String username;
    public String email;
    public int points;
    public String locale;
    public URI avatar;
    public RdbUserTypeEnum type;
    public int premium;
    public String expiration;
}