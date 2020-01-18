package org.activiti.engine.impl.persistence;

import org.activiti.engine.impl.persistence.entity.Entity;

/**
 * Interface to express a condition whether or not one specific cached entity should be used in the return result of a query.
 * 

 */
public interface SingleCachedEntityMatcher<EntityImpl extends Entity> {

  boolean isRetained(EntityImpl entity, Object param);

}