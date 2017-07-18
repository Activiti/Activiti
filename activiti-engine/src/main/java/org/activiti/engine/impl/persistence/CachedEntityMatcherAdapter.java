package org.activiti.engine.impl.persistence;

import java.util.Collection;

import org.activiti.engine.impl.persistence.cache.CachedEntity;
import org.activiti.engine.impl.persistence.entity.Entity;

/**

 */
public abstract class CachedEntityMatcherAdapter<EntityImpl extends Entity> implements CachedEntityMatcher<EntityImpl> {
  
  @Override
  public boolean isRetained(Collection<EntityImpl> databaseEntities, Collection<CachedEntity> cachedEntities, EntityImpl entity, Object param) {
    return isRetained(entity, param);
  }
  
  public abstract boolean isRetained(EntityImpl entity, Object param);
  
}
 
