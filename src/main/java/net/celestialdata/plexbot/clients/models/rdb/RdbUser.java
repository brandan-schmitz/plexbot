package net.celestialdata.plexbot.clients.models.rdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.net.URI;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RdbUser {
    public Integer id;
    public String username;
    public String email;
    public Integer points;
    public String locale;
    public URI avatar;
    public RdbUserType type;
    public Integer premium;
    public String expiration;
}