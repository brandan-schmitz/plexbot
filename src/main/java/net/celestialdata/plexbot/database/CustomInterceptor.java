package net.celestialdata.plexbot.database;

import net.celestialdata.plexbot.database.models.BaseModel;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;

/**
 * Intercepts events from Hibernate in order to extend their functionality.
 */
public class CustomInterceptor extends EmptyInterceptor {
    /**
     * Override the default onDelete method to make it run the onDelete function of the BaseModel used
     * to create the database object models. This allows custom actions to be performed per-object when
     * that object is deleted.
     *
     * @param entity the class of the entity to delete
     * @param id the id of the object to delete
     * @param state the state of the object
     * @param propertyNames the property names of the object
     * @param types the types of the object
     */
    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (entity instanceof BaseModel) {
            ((BaseModel) entity).onDelete();
        }
    }

    /**
     * Override he default onSave method to make it run the onSave function of the BaseModel used
     * to create teh database object models. This allows custom actions to be performed per-object when
     * that object is deleted.
     *
     * @param entity the class of the entity to delete
     * @param id the id of the object to delete
     * @param state the state of the object
     * @param propertyNames the property names of the object
     * @param types the types of the object
     * @return true if the object was saved or updated successfully
     */
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (entity instanceof BaseModel) {
            ((BaseModel) entity).onSave();
        }

        return super.onSave(entity, id, state, propertyNames, types);
    }
}