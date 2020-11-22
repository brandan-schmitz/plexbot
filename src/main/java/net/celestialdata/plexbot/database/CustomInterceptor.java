package net.celestialdata.plexbot.database;

import net.celestialdata.plexbot.database.models.BaseModel;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;

public class CustomInterceptor extends EmptyInterceptor {
    public void onDelete(Object entity,
                         Serializable id,
                         Object[] state,
                         String[] propertyNames,
                         Type[] types) {
        if (entity instanceof BaseModel) {
            ((BaseModel) entity).onDelete();
        }
    }
}