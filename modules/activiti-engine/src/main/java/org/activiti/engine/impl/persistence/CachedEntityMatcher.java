package org.activiti.engine.impl.persistence;

import org.activiti.engine.impl.db.Entity;

/**
 * Interface to express a condition whether or not a cached entity should be used in the return result of a query.
 * 
 * @author Joram Barrez
 */
public interface CachedEntityMatcher<EntityImpl extends Entity> {

  boolean isRetained(EntityImpl entity);

}
