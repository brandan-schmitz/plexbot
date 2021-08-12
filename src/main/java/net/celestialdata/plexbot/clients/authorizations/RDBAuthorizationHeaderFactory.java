package net.celestialdata.plexbot.clients.authorizations;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MultivaluedMap;

@ApplicationScoped
public class RDBAuthorizationHeaderFactory implements ClientHeadersFactory {

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedMapImpl<>();
        result.add("Authorization", "Bearer " + ConfigProvider.getConfig().getValue("ApiKeys.realDebridKey", String.class));
        return result;
    }
}