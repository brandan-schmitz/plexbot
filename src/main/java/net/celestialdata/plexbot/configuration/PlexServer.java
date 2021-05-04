package net.celestialdata.plexbot.configuration;

/**
 * Represents a single plex server as configured in the bots configuration file
 */
public class PlexServer {
    private final String address;
    private final int port;
    private final String username;
    private final String password;

    /**
     * Construct a new PlexServer object
     *
     * @param address  the IP address the server is running on
     * @param port     the port the server is running on
     * @param username the username used to login to this server
     * @param password the password used to login to this server
     */
    public PlexServer(String address, int port, String username, String password) {
        this.address = address;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * Return the IP address of the plex server
     *
     * @return ip address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Return the port of the plex server
     *
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * Return the username of the plex server
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Return the password of the plex server
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }
}