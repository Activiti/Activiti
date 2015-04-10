package org.activiti.engine.impl.persistence;

import org.activiti.engine.impl.db.PersistentObject;

/**
 * Interface to express a condition whether or not a cached entity should be
 * used in the return result of a query.
 * 
 * @author Joram Barrez
 */
public interface CachedEntityMatcher<Entity extends PersistentObject> {

    boolean isRetained(Entity entity);

}
