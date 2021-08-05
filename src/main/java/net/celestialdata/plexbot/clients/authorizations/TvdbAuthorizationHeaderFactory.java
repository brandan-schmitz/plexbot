package net.celestialdata.plexbot.clients.authorizations;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;

@ApplicationScoped
public class TvdbAuthorizationHeaderFactory implements ClientHeadersFactory {

    @Inject
    TvdbAuthorizer tvdbAuthorizer;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedMapImpl<>();
        result.add("Authorization", "Bearer " + tvdbAuthorizer.getAuthToken());
        return result;
    }
}