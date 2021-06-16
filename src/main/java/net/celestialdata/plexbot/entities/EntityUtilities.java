package net.celestialdata.plexbot.entities;

import javax.enterprise.context.ApplicationScoped;;
import javax.transaction.Transactional;

@ApplicationScoped
public class EntityUtilities {

    @Transactional
    public boolean movieExists(String id) {
        return Movie.count("id", id) == 1;
    }
}