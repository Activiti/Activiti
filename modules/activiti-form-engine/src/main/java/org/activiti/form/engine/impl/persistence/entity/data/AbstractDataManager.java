/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.form.engine.impl.persistence.entity.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.db.DbSqlSession;
import org.activiti.form.engine.impl.db.Entity;
import org.activiti.form.engine.impl.persistence.AbstractManager;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public abstract class AbstractDataManager<EntityImpl extends Entity> extends AbstractManager implements DataManager<EntityImpl> {
  
  public AbstractDataManager(FormEngineConfiguration dmnEngineConfiguration) {
    super(dmnEngineConfiguration);
  }

  public abstract Class<? extends EntityImpl> getManagedEntityClass();
  
  public List<Class<? extends EntityImpl>> getManagedEntitySubClasses() {
    return null;
  }
  
  protected DbSqlSession getDbSqlSession() {
    return getSession(DbSqlSession.class);
  }
  
  @Override
  public EntityImpl findById(String entityId) {
    if (entityId == null) {
      return null;
    }
    
    // Database
    return getDbSqlSession().selectById(getManagedEntityClass(), entityId);
  }

  @Override
  public void insert(EntityImpl entity) {
    getDbSqlSession().insert(entity);
  }
  
  public EntityImpl update(EntityImpl entity) {
    getDbSqlSession().update(entity);
    return entity;
  }
  
  @Override
  public void delete(String id) {
    EntityImpl entity = findById(id);
    delete(entity);
  }
  
  @Override
  public void delete(EntityImpl entity) {
    getDbSqlSession().delete(entity);
  }
  
  @SuppressWarnings("unchecked")
  protected EntityImpl findByQuery(String selectQuery, Object parameter) {
    return (EntityImpl) getDbSqlSession().selectOne(selectQuery, parameter);
  }
  
  @SuppressWarnings("unchecked")
  protected List<EntityImpl> getList(String dbQueryName, Object parameter) {
    Collection<EntityImpl> result = getDbSqlSession().selectList(dbQueryName, parameter);
    return new ArrayList<EntityImpl>(result);
  }

}
