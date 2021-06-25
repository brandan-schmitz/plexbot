package net.celestialdata.plexbot.clients.models.plex;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName(value = "user")
public class PlexUser {
    public int id;
    public String uuid;
    public String email;

    @JsonAlias(value = "joined_at")
    public String joinedAt;

    public String username;
    public String title;
    public String thumb;
    public boolean hasPassword;
    public String authToken;

    @JsonAlias(value = "authentication_token")
    public String authenticationToken;

    public String confirmedAt;
}