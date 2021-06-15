package net.celestialdata.plexbot.entities;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@ApplicationScoped
public class EntityUtilities {

    @Inject
    EntityManager entityManager;

    @Transactional
    public boolean movieExists(String id) {
        return Movie.count("id", id) == 1;
    }
}