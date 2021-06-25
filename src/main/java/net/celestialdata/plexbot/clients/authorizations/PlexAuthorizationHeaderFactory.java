package net.celestialdata.plexbot.clients.authorizations;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Base64;

@ApplicationScoped
public class PlexAuthorizationHeaderFactory implements ClientHeadersFactory {

    @Inject
    @Named("botVersion")
    Instance<String> botVersion;

    @Inject
    PlexAuthorizer plexAuthorizer;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedMapImpl<>();

        // Add basic login header
        result.add("Authorization", "Basic " + Base64.getEncoder().encodeToString((
                ConfigProvider.getConfig().getValue("PlexSettings.username", String.class) +
                        ":" + ConfigProvider.getConfig().getValue("PlexSettings.password", String.class)).getBytes()));

        // Add auth token header
        result.add("X-Plex-Token", plexAuthorizer.getAuthToken());

        // Add client identifier header
        result.add("X-Plex-Client-Identifier", ConfigProvider.getConfig().getValue("PlexSettings.clientIdentifier", String.class));

        // Add application info headers
        result.add("X-Plex-Product", "Plexbot for Discord");
        result.add("X-Plex-Version", botVersion.get());
        result.add("X-Plex-Device", System.getProperty("os.name"));
        result.add("X-Plex-Device-Name", "Plexbot");
        result.add("X-Plex-Platform", System.getProperty("os.name"));
        result.add("X-Plex-Platform-Version", System.getProperty("os.version"));

        return result;
    }
}