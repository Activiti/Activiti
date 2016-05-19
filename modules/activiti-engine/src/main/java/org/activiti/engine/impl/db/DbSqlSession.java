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

package org.activiti.engine.impl.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.ActivitiWrongDbException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.DeploymentQueryImpl;
import org.activiti.engine.impl.ExecutionQueryImpl;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.HistoricDetailQueryImpl;
import org.activiti.engine.impl.HistoricProcessInstanceQueryImpl;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.HistoricVariableInstanceQueryImpl;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.ModelQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.ProcessInstanceQueryImpl;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.upgrade.DbUpgradeStep;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.cache.CachedEntity;
import org.activiti.engine.impl.persistence.cache.EntityCache;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.PropertyEntity;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DbSqlSession implements Session {

  private static final Logger log = LoggerFactory.getLogger(DbSqlSession.class);
  
  protected static final Pattern CLEAN_VERSION_REGEX = Pattern.compile("\\d\\.\\d*");
  
  protected static final String LAST_V5_VERSION = "5.99.0.0";
  
  protected static final List<ActivitiVersion> ACTIVITI_VERSIONS = new ArrayList<ActivitiVersion>();
  static {

    /* Previous */

    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.7"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.8"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.9"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.10"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.11"));

    // 5.12.1 was a bugfix release on 5.12 and did NOT change the version in ACT_GE_PROPERTY
    // On top of that, DB2 create script for 5.12.1 was shipped with a 'T' suffix ...
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.12", Arrays.asList("5.12.1", "5.12T")));

    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.13"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.14"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.15"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.15.1"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.16"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.16.1"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.16.2-SNAPSHOT"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.16.2"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.16.3.0"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.16.4.0"));

    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.17.0.0"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.17.0.1"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.17.0.2"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.18.0.0"));
    ACTIVITI_VERSIONS.add(new ActivitiVersion("5.18.0.1"));
    
    
    /*
     * Version 5.18.0.1 is the latest v5 version in the list here, although if you would look at the v5 code,
     * you'll see there are a few other releases afterwards.
     * 
     * The reasoning is as follows: after 5.18.0.1, no database changes were done anymore.
     * And if there would be database changes, they would have been part of both 5.x _and_ 6.x upgrade scripts.
     * The logic below will assume it's one of these releases in case it isn't found in the list here
     * and do the upgrade from the 'virtual' release 5.99.0.0 to make sure th v6 changes are applied.
     */
    

    // This is the latest version of the 5 branch. It's a 'virtual' version cause it doesn't exist, but it is
    // there to make sure all previous version can upgrade to the 6 version correctly.
    ACTIVITI_VERSIONS.add(new ActivitiVersion(LAST_V5_VERSION));
    
    // Version 6
    ACTIVITI_VERSIONS.add(new ActivitiVersion("6.0.0.0"));
    
    /* Current */
    ACTIVITI_VERSIONS.add(new ActivitiVersion(ProcessEngine.VERSION));
  }

  protected SqlSession sqlSession;
  protected DbSqlSessionFactory dbSqlSessionFactory;
  protected EntityCache entityCache;
  protected Map<Class<? extends Entity>, List<Entity>> insertedObjects = new HashMap<Class<? extends Entity>, List<Entity>>();
  protected List<DeleteOperation> deleteOperations = new ArrayList<DeleteOperation>();
  protected String connectionMetadataDefaultCatalog;
  protected String connectionMetadataDefaultSchema;

  public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory, EntityCache entityCache) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    this.sqlSession = dbSqlSessionFactory.getSqlSessionFactory().openSession();
    this.entityCache = entityCache;
  }

  public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory, EntityCache entityCache, Connection connection, String catalog, String schema) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    this.sqlSession = dbSqlSessionFactory.getSqlSessionFactory().openSession(connection); // Note the use of connection param here, different from other constructor
    this.entityCache = entityCache;
    this.connectionMetadataDefaultCatalog = catalog;
    this.connectionMetadataDefaultSchema = schema;
  }
  
  // insert ///////////////////////////////////////////////////////////////////
  
  
  public void insert(Entity entity) {
    if (entity.getId() == null) {
      String id = dbSqlSessionFactory.getIdGenerator().getNextId();
      entity.setId(id);
    }
    
    Class<? extends Entity> clazz = entity.getClass();
    if (!insertedObjects.containsKey(clazz)) {
    	insertedObjects.put(clazz, new ArrayList<Entity>());
    }
    
    insertedObjects.get(clazz).add(entity);
    entityCache.put(entity, false); // False -> entity is inserted, so always changed
  }

  // update
  // ///////////////////////////////////////////////////////////////////

  public void update(Entity entity) {
    entityCache.put(entity, false); // false -> we don't store state, meaning it will always be seen as changed 
  }

  public int update(String statement, Object parameters) {
    String updateStatement = dbSqlSessionFactory.mapStatement(statement);
    return getSqlSession().update(updateStatement, parameters);
  }

  // delete
  // ///////////////////////////////////////////////////////////////////

  public void delete(String statement, Object parameter) {
    deleteOperations.add(new BulkDeleteOperation(statement, parameter));
  }

  public void delete(Entity entity) {
    for (DeleteOperation deleteOperation : deleteOperations) {
      if (deleteOperation.sameIdentity(entity)) {
        log.debug("skipping redundant delete: {}", entity);
        return; // Skip this delete. It was already added.
      }
    }

    deleteOperations.add(new CheckedDeleteOperation(entity));
  }

  public interface DeleteOperation {
  	
  	/**
  	 * @return The persistent object class that is being deleted.
  	 *         Null in case there are multiple objects of different types!
  	 */
  	Class<? extends Entity> getEntityClass();
    
    boolean sameIdentity(Entity other);

    void clearCache();

    void execute();

  }

  /**
   * Use this {@link DeleteOperation} to execute a dedicated delete statement. It is important to note there won't be any optimistic locking checks done for these kind of delete operations!
   * 
   * For example, a usage of this operation would be to delete all variables for a certain execution, when that certain execution is removed. The optimistic locking happens on the execution, but the
   * variables can be removed by a simple 'delete from var_table where execution_id is xxx'. It could very well be there are no variables, which would also work with this query, but not with the
   * regular {@link CheckedDeleteOperation}.
   */
  public class BulkDeleteOperation implements DeleteOperation {
    private String statement;
    private Object parameter;

    public BulkDeleteOperation(String statement, Object parameter) {
      this.statement = dbSqlSessionFactory.mapStatement(statement);
      this.parameter = parameter;
    }

    @Override
    public Class<? extends Entity> getEntityClass() {
    	return null;
    }
    
    @Override
    public boolean sameIdentity(Entity other) {
      // this implementation is unable to determine what the identity of
      // the removed object(s) will be.
      return false;
    }

    @Override
    public void clearCache() {
      // this implementation cannot clear the object(s) to be removed from the cache.
    }

    @Override
    public void execute() {
      sqlSession.delete(statement, parameter);
    }

    @Override
    public String toString() {
      return "bulk delete: " + statement + "(" + parameter + ")";
    }
  }

  /**
   * A {@link DeleteOperation} that checks for concurrent modifications if the persistent object implements {@link HasRevision}. That is, it employs optimisting concurrency control. Used when the
   * persistent object has been fetched already.
   */
  public class CheckedDeleteOperation implements DeleteOperation {
    protected final Entity entity;

    public CheckedDeleteOperation(Entity entity) {
      this.entity = entity;
    }

    @Override
    public Class<? extends Entity> getEntityClass() {
    	return entity.getClass();
    }
    
    @Override
    public boolean sameIdentity(Entity other) {
      return entity.getClass().equals(other.getClass()) && entity.getId().equals(other.getId());
    }

    @Override
    public void clearCache() {
      entityCache.cacheRemove(entity.getClass(), entity.getId());
    }

    public void execute() {
      String deleteStatement = dbSqlSessionFactory.getDeleteStatement(entity.getClass());
      deleteStatement = dbSqlSessionFactory.mapStatement(deleteStatement);
      if (deleteStatement == null) {
        throw new ActivitiException("no delete statement for " + entity.getClass() + " in the ibatis mapping files");
      }

      // It only makes sense to check for optimistic locking exceptions
      // for objects that actually have a revision
      if (entity instanceof HasRevision) {
        int nrOfRowsDeleted = sqlSession.delete(deleteStatement, entity);
        if (nrOfRowsDeleted == 0) {
          throw new ActivitiOptimisticLockingException(entity + " was updated by another transaction concurrently");
        }
      } else {
        sqlSession.delete(deleteStatement, entity);
      }
    }

    public Entity getEntity() {
      return entity;
    }

    @Override
    public String toString() {
      return "delete " + entity;
    }
  }

  /**
   * A bulk version of the {@link CheckedDeleteOperation}.
   */
  public class BulkCheckedDeleteOperation implements DeleteOperation {

    protected Class<? extends Entity> entityClass;
    protected List<Entity> entities = new ArrayList<Entity>();

    public BulkCheckedDeleteOperation(Class<? extends Entity> entityClass) {
      this.entityClass = entityClass;
    }

    public void addEntity(Entity entity) {
      entities.add(entity);
    }

    @Override
    public boolean sameIdentity(Entity other) {
      for (Entity entity : entities) {
        if (entity.getClass().equals(other.getClass()) && entity.getId().equals(other.getId())) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void clearCache() {
      for (Entity entity : entities) {
        entityCache.cacheRemove(entity.getClass(), entity.getId());
      }
    }

    public void execute() {

      if (entities.isEmpty()) {
        return;
      }

      String bulkDeleteStatement = dbSqlSessionFactory.getBulkDeleteStatement(entityClass);
      bulkDeleteStatement = dbSqlSessionFactory.mapStatement(bulkDeleteStatement);
      if (bulkDeleteStatement == null) {
        throw new ActivitiException("no bulk delete statement for " + entityClass + " in the mapping files");
      }

      sqlSession.delete(bulkDeleteStatement, entities);
    }

    public Class<? extends Entity> getEntityClass() {
      return entityClass;
    }

    public void setEntityClass(Class<? extends Entity> entityClass) {
      this.entityClass = entityClass;
    }

    public List<Entity> getEntities() {
      return entities;
    }

    public void setEntities(List<Entity> entities) {
      this.entities = entities;
    }
    
    @SuppressWarnings("unchecked")
    public void setEntityObjects(List<? extends Entity> entities) {
      this.entities = (List<Entity>) entities;
    }

    @Override
    public String toString() {
      return "bulk delete of " + entities.size() + (!entities.isEmpty() ? " entities of " + entities.get(0).getClass() : 0);
    }
  }

  // select
  // ///////////////////////////////////////////////////////////////////

  @SuppressWarnings({ "rawtypes" })
  public List selectList(String statement) {
    return selectList(statement, null, 0, Integer.MAX_VALUE);
  }

  @SuppressWarnings("rawtypes")
  public List selectList(String statement, Object parameter) {
    return selectList(statement, parameter, 0, Integer.MAX_VALUE);
  }

  @SuppressWarnings("rawtypes")
  public List selectList(String statement, Object parameter, Page page) {
    if (page != null) {
      return selectList(statement, parameter, page.getFirstResult(), page.getMaxResults());
    } else {
      return selectList(statement, parameter, 0, Integer.MAX_VALUE);
    }
  }

  @SuppressWarnings("rawtypes")
  public List selectList(String statement, ListQueryParameterObject parameter, Page page) {
    if (page != null) {
      parameter.setFirstResult(page.getFirstResult());
      parameter.setMaxResults(page.getMaxResults());
    }
    return selectList(statement, parameter);
  }

  @SuppressWarnings("rawtypes")
  public List selectList(String statement, Object parameter, int firstResult, int maxResults) {
    return selectList(statement, new ListQueryParameterObject(parameter, firstResult, maxResults));
  }

  @SuppressWarnings("rawtypes")
  public List selectList(String statement, ListQueryParameterObject parameter) {
    return selectListWithRawParameter(statement, parameter, parameter.getFirstResult(), parameter.getMaxResults());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List selectListWithRawParameter(String statement, Object parameter, int firstResult, int maxResults) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    if (firstResult == -1 || maxResults == -1) {
      return Collections.EMPTY_LIST;
    }
    List loadedObjects = sqlSession.selectList(statement, parameter);
    return cacheLoadOrStore(loadedObjects);
  }

  @SuppressWarnings({ "rawtypes" })
  public List selectListWithRawParameterWithoutFilter(String statement, Object parameter, int firstResult, int maxResults) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    if (firstResult == -1 || maxResults == -1) {
      return Collections.EMPTY_LIST;
    }
    return sqlSession.selectList(statement, parameter);
  }

  public Object selectOne(String statement, Object parameter) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    Object result = sqlSession.selectOne(statement, parameter);
    if (result instanceof Entity) {
      Entity loadedObject = (Entity) result;
      result = cacheLoadOrStore(loadedObject);
    }
    return result;
  }

  public <T extends Entity> T selectById(Class<T> entityClass, String id) {
    return selectById(entityClass, id, true);
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Entity> T selectById(Class<T> entityClass, String id, boolean useCache) {
    T entity = null;
    
    if (useCache) {
      entity = entityCache.findInCache(entityClass, id);
      if (entity != null) {
        return entity;
      }
    }
    
    String selectStatement = dbSqlSessionFactory.getSelectStatement(entityClass);
    selectStatement = dbSqlSessionFactory.mapStatement(selectStatement);
    entity = (T) sqlSession.selectOne(selectStatement, id);
    if (entity == null) {
      return null;
    }
    
    entityCache.put(entity, true); // true -> store state so we can see later if it is updated later on
    return entity;
  }

  // internal session cache
  // ///////////////////////////////////////////////////

  @SuppressWarnings("rawtypes")
  protected List cacheLoadOrStore(List<Object> loadedObjects) {
    if (loadedObjects.isEmpty()) {
      return loadedObjects;
    }
    if (!(loadedObjects.get(0) instanceof Entity)) {
      return loadedObjects;
    }

    List<Entity> filteredObjects = new ArrayList<Entity>(loadedObjects.size());
    for (Object loadedObject : loadedObjects) {
      Entity cachedEntity = cacheLoadOrStore((Entity) loadedObject);
      filteredObjects.add(cachedEntity);
    }
    return filteredObjects;
  }

  /**
   * Returns the object in the cache. If this object was loaded before, then the original object is returned (the cached version is more recent). 
   * If this is the first time this object is loaded, then the loadedObject is added to the cache.
   */
  protected Entity cacheLoadOrStore(Entity entity) {
    Entity cachedEntity = entityCache.findInCache(entity.getClass(), entity.getId());
    if (cachedEntity != null) {
      return cachedEntity;
    }
    entityCache.put(entity, true);
    return entity;
  }

  // flush
  // ////////////////////////////////////////////////////////////////////

  public void flush() {
    List<DeleteOperation> removedOperations = removeUnnecessaryOperations();

    List<Entity> updatedObjects = getUpdatedObjects();

    if (log.isDebugEnabled()) {
      Collection<List<Entity>> insertedObjectLists = insertedObjects.values();
      int nrOfInserts = 0, nrOfUpdates = 0, nrOfDeletes = 0;
      for (List<Entity> insertedObjectList: insertedObjectLists) {
      	for (Entity insertedObject : insertedObjectList) {
      		log.debug("  insert {}", insertedObject);
      		nrOfInserts++;
      	}
      }
      for (Entity updatedObject : updatedObjects) {
        log.debug("  update {}", updatedObject);
        nrOfUpdates++;
      }
      for (DeleteOperation deleteOperation : deleteOperations) {
        log.debug("  {}", deleteOperation);
        nrOfDeletes++;
      }
      log.debug("flush summary: {} insert, {} update, {} delete.", nrOfInserts, nrOfUpdates, nrOfDeletes);
      log.debug("now executing flush...");
    }

    flushInserts();
    flushUpdates(updatedObjects);
    flushDeletes(removedOperations);
  }

  /**
   * Clears all deleted and inserted objects from the cache, and removes inserts and deletes that cancel each other.
   */
  protected List<DeleteOperation> removeUnnecessaryOperations() {
    List<DeleteOperation> removedDeleteOperations = new ArrayList<DeleteOperation>();

    for (Iterator<DeleteOperation> deleteIterator = deleteOperations.iterator(); deleteIterator.hasNext();) {
    	
      DeleteOperation deleteOperation = deleteIterator.next();
      Class<? extends Entity> deletedEntity = deleteOperation.getEntityClass();
      
      List<Entity> insertedObjectsOfSameClass = insertedObjects.get(deletedEntity);
      if (insertedObjectsOfSameClass != null && insertedObjectsOfSameClass.size() > 0) {
      	
	      for (Iterator<Entity> insertIterator = insertedObjectsOfSameClass.iterator(); insertIterator.hasNext();) {
	        Entity insertedObject = insertIterator.next();
	        
	        // if the deleted object is inserted,
	        if (deleteOperation.sameIdentity(insertedObject)) {
	          // remove the insert and the delete, they cancel each other
	          insertIterator.remove();
	          deleteIterator.remove();
	          // add removed operations to be able to fire events
	          removedDeleteOperations.add( deleteOperation);
	        }
	      }
	      
	      if (insertedObjects.get(deletedEntity).size() == 0) {
	      	insertedObjects.remove(deletedEntity);
	      }
	      
      }

      // in any case, remove the deleted object from the cache
      deleteOperation.clearCache();
    }
    
    for (Class<? extends Entity> entityClass : insertedObjects.keySet()) {
    	for (Entity insertedObject : insertedObjects.get(entityClass)) {
    	  entityCache.cacheRemove(insertedObject.getClass(), insertedObject.getId());
    	}
    }

    return removedDeleteOperations;
  }

  protected List<DeleteOperation> optimizeDeleteOperations(List<DeleteOperation> deleteOperations) {
    
    // TODO: cannot be deleted yet, determines the order of deletion
    
    // TODO: currently only for Execution entities. Needs to be done for all.
    
    List<DeleteOperation> optimizedDeleteOperations = new ArrayList<DbSqlSession.DeleteOperation>(deleteOperations.size());

    int nrOfExecutionEntities = 0;

    for (int i = 0; i < deleteOperations.size(); i++) {
      DeleteOperation deleteOperation = deleteOperations.get(i);
      if (isCheckedExecutionEntityDelete(deleteOperation)) {
        nrOfExecutionEntities++;
      }
    }

    List<ExecutionEntity> executionEntitiesToDelete = new ArrayList<ExecutionEntity>(nrOfExecutionEntities);

    for (DeleteOperation deleteOperation : deleteOperations) {

      if (isCheckedExecutionEntityDelete(deleteOperation) && nrOfExecutionEntities > 1) {

        // Check parent id / super execution id to know the order of deletions
        // (children first)
        ExecutionEntity executionEntity = (ExecutionEntity) ((CheckedDeleteOperation) deleteOperation).getEntity();
        int parentIndex = -1;
        for (int deleteIndex = 0; deleteIndex < executionEntitiesToDelete.size(); deleteIndex++) {
          ExecutionEntity executionEntityToDelete = executionEntitiesToDelete.get(deleteIndex);
          if (executionEntityToDelete.getId().equals(executionEntity.getParentId()) || executionEntityToDelete.getId().equals(executionEntity.getSuperExecutionId())) {
            parentIndex = deleteIndex;
            break;
          }
        }

        if (parentIndex == -1) {
          executionEntitiesToDelete.add(executionEntity);
        } else {
          executionEntitiesToDelete.add(parentIndex, executionEntity);
        }

        // If all execution entities have been found, make a bulk delete out of it
        
        if (executionEntitiesToDelete.size() == nrOfExecutionEntities) {
          
          // All Databases except MySQL can handle bulk deletes. 
          // The next best thing if mysql can't have bulk deletes is 'layered' deletes, ie the childs are removed first
          // before the parents, but in two different sql statements (vs 1 in the bulk delete).
          
          BulkCheckedDeleteOperation bulkCheckedDeleteOperation = new BulkCheckedDeleteOperation(ExecutionEntity.class);
          bulkCheckedDeleteOperation.setEntityObjects(executionEntitiesToDelete);
          optimizedDeleteOperations.add(bulkCheckedDeleteOperation);
          
        }
          
      } else {
        optimizedDeleteOperations.add(deleteOperation);
      }

    }
    
    return optimizedDeleteOperations;
  }
  
  protected boolean isCheckedExecutionEntityDelete(DeleteOperation deleteOperation) {
    return deleteOperation instanceof CheckedDeleteOperation && ((CheckedDeleteOperation) deleteOperation).getEntity() instanceof ExecutionEntity;
  }
  
  public List<Entity> getUpdatedObjects() {
    List<Entity> updatedObjects = new ArrayList<Entity>();
    Map<Class<?>, Map<String, CachedEntity>> cachedObjects = entityCache.getAllCachedEntities();
    for (Class<?> clazz : cachedObjects.keySet()) {

      Map<String, CachedEntity> classCache = cachedObjects.get(clazz);
      for (CachedEntity cachedObject : classCache.values()) {

        Entity cachedEntity = cachedObject.getEntity();
        if (!isEntityToBeDeleted(cachedEntity)) {
          if (cachedObject.hasChanged()) {
            updatedObjects.add(cachedEntity);
          } else {
            log.trace("loaded object '{}' was not updated", cachedEntity);
          }
        }

      }

    }
    return updatedObjects;
  }

  public boolean isEntityToBeDeleted(Entity entity) {
    for (DeleteOperation deleteOperation : deleteOperations) {
      if (deleteOperation.sameIdentity(entity)) {
        return true;
      }
    }
    return false;
  }

  public <T extends Entity> List<T> pruneDeletedEntities(List<T> listToPrune) {
    List<T> prunedList = new ArrayList<T>(listToPrune);
    for (T potentiallyDeleted : listToPrune) {
      for (DeleteOperation deleteOperation : deleteOperations) {

        if (deleteOperation.sameIdentity(potentiallyDeleted)) {
          prunedList.remove(potentiallyDeleted);
        }

      }
    }
    return prunedList;
  }

  protected void flushInserts() {
    
    if (insertedObjects.size() == 0) {
      return;
    }
  	
  	// Handle in entity dependency order
    for (Class<? extends Entity> entityClass : EntityDependencyOrder.INSERT_ORDER) {
      if (insertedObjects.containsKey(entityClass)) {
      	flushEntities(entityClass, insertedObjects.get(entityClass));
      	insertedObjects.remove(entityClass);
      }
    }
    
    // Next, in case of custom entities or we've screwed up and forgotten some entity
    if (insertedObjects.size() > 0) {
	    for (Class<? extends Entity> entityClass : insertedObjects.keySet()) {
      	flushEntities(entityClass, insertedObjects.get(entityClass));
	    }
    }
    
    insertedObjects.clear();
  }

	protected void flushEntities(Class<? extends Entity> entityClass, List<Entity> entitiesToInsert) {
	  if (entitiesToInsert.size() == 1) {
	  	flushRegularInsert(entitiesToInsert.get(0), entityClass);
	  } else if (Boolean.FALSE.equals(dbSqlSessionFactory.isBulkInsertable(entityClass))) {
	  	for (Entity entity : entitiesToInsert) {
	  		flushRegularInsert(entity, entityClass);
	  	}
	  }	else {
	  	flushBulkInsert(insertedObjects.get(entityClass), entityClass);
	  }
  }
  
  protected void flushRegularInsert(Entity entity, Class<? extends Entity> clazz) {
  	 String insertStatement = dbSqlSessionFactory.getInsertStatement(entity);
     insertStatement = dbSqlSessionFactory.mapStatement(insertStatement);

     if (insertStatement==null) {
       throw new ActivitiException("no insert statement for " + entity.getClass() + " in the ibatis mapping files");
     }
     
     log.debug("inserting: {}", entity);
     sqlSession.insert(insertStatement, entity);
     
     // See https://activiti.atlassian.net/browse/ACT-1290
     if (entity instanceof HasRevision) {
       ((HasRevision) entity).setRevision(((HasRevision) entity).getRevisionNext());
     }
  }

  protected void flushBulkInsert(List<Entity> entityList, Class<? extends Entity> clazz) {
    String insertStatement = dbSqlSessionFactory.getBulkInsertStatement(clazz);
    insertStatement = dbSqlSessionFactory.mapStatement(insertStatement);

    if (insertStatement==null) {
      throw new ActivitiException("no insert statement for " + entityList.get(0).getClass() + " in the ibatis mapping files");
    }

    if (entityList.size() <= dbSqlSessionFactory.getMaxNrOfStatementsInBulkInsert()) {
      sqlSession.insert(insertStatement, entityList);
    } else {
      
      for (int start = 0; start < entityList.size(); start += dbSqlSessionFactory.getMaxNrOfStatementsInBulkInsert()) {
        List<Entity> subList = entityList.subList(start, 
            Math.min(start + dbSqlSessionFactory.getMaxNrOfStatementsInBulkInsert(), entityList.size()));
        sqlSession.insert(insertStatement, subList);
      }
      
    }
    
    if (entityList.get(0) instanceof HasRevision) {
      for (Entity insertedObject: entityList) {
        ((HasRevision) insertedObject).setRevision(((HasRevision) insertedObject).getRevisionNext());
      }
    }
   
  }


  protected void flushUpdates(List<Entity> updatedObjects) {
    for (Entity updatedObject : updatedObjects) {
      String updateStatement = dbSqlSessionFactory.getUpdateStatement(updatedObject);
      updateStatement = dbSqlSessionFactory.mapStatement(updateStatement);

      if (updateStatement == null) {
        throw new ActivitiException("no update statement for " + updatedObject.getClass() + " in the ibatis mapping files");
      }

      log.debug("updating: {}", updatedObject);
      int updatedRecords = sqlSession.update(updateStatement, updatedObject);
      if (updatedRecords == 0) {
        throw new ActivitiOptimisticLockingException(updatedObject + " was updated by another transaction concurrently");
      }

      // See https://activiti.atlassian.net/browse/ACT-1290
      if (updatedObject instanceof HasRevision) {
        ((HasRevision) updatedObject).setRevision(((HasRevision) updatedObject).getRevisionNext());
      }

    }
    updatedObjects.clear();
  }

  protected void flushDeletes(List<DeleteOperation> removedOperations) {
    boolean dispatchEvent = Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled();
    flushRegularDeletes(dispatchEvent);
    deleteOperations.clear();
  }

  protected void flushRegularDeletes(boolean dispatchEvent) {
    List<DeleteOperation> optimizedDeleteOperations = optimizeDeleteOperations(deleteOperations);
    for (DeleteOperation delete : optimizedDeleteOperations) {
      log.debug("executing: {}", delete);
      delete.execute();
    }
  }

  public void close() {
    sqlSession.close();
  }

  public void commit() {
    sqlSession.commit();
  }

  public void rollback() {
    sqlSession.rollback();
  }

  // schema operations
  // ////////////////////////////////////////////////////////

  public void dbSchemaCheckVersion() {
    try {
      String dbVersion = getDbVersion();
      if (!ProcessEngine.VERSION.equals(dbVersion)) {
        throw new ActivitiWrongDbException(ProcessEngine.VERSION, dbVersion);
      }

      String errorMessage = null;
      if (!isEngineTablePresent()) {
        errorMessage = addMissingComponent(errorMessage, "engine");
      }
      if (dbSqlSessionFactory.isDbHistoryUsed() && !isHistoryTablePresent()) {
        errorMessage = addMissingComponent(errorMessage, "history");
      }
      if (dbSqlSessionFactory.isDbIdentityUsed() && !isIdentityTablePresent()) {
        errorMessage = addMissingComponent(errorMessage, "identity");
      }

      if (errorMessage != null) {
        throw new ActivitiException("Activiti database problem: " + errorMessage);
      }

    } catch (Exception e) {
      if (isMissingTablesException(e)) {
        throw new ActivitiException(
            "no activiti tables in db. set <property name=\"databaseSchemaUpdate\" to value=\"true\" or value=\"create-drop\" (use create-drop for testing only!) in bean processEngineConfiguration in activiti.cfg.xml for automatic schema creation",
            e);
      } else {
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        } else {
          throw new ActivitiException("couldn't get db schema version", e);
        }
      }
    }

    log.debug("activiti db schema check successful");
  }

  protected String addMissingComponent(String missingComponents, String component) {
    if (missingComponents == null) {
      return "Tables missing for component(s) " + component;
    }
    return missingComponents + ", " + component;
  }

  protected String getDbVersion() {
    String selectSchemaVersionStatement = dbSqlSessionFactory.mapStatement("selectDbSchemaVersion");
    return (String) sqlSession.selectOne(selectSchemaVersionStatement);
  }

  public void dbSchemaCreate() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    if (isEngineTablePresent()) {
      String dbVersion = getDbVersion();
      if (!ProcessEngine.VERSION.equals(dbVersion)) {
        throw new ActivitiWrongDbException(ProcessEngine.VERSION, dbVersion);
      }
    } else {
      dbSchemaCreateEngine();
    }

    if (processEngineConfiguration.getHistoryLevel() != HistoryLevel.NONE) {
      dbSchemaCreateHistory();
    }

    if (processEngineConfiguration.isDbIdentityUsed()) {
      dbSchemaCreateIdentity();
    }
  }

  protected void dbSchemaCreateIdentity() {
    executeMandatorySchemaResource("create", "identity");
  }

  protected void dbSchemaCreateHistory() {
    executeMandatorySchemaResource("create", "history");
  }

  protected void dbSchemaCreateEngine() {
    executeMandatorySchemaResource("create", "engine");
  }

  public void dbSchemaDrop() {
    executeMandatorySchemaResource("drop", "engine");
    if (dbSqlSessionFactory.isDbHistoryUsed()) {
      executeMandatorySchemaResource("drop", "history");
    }
    if (dbSqlSessionFactory.isDbIdentityUsed()) {
      executeMandatorySchemaResource("drop", "identity");
    }
  }

  public void dbSchemaPrune() {
    if (isHistoryTablePresent() && !dbSqlSessionFactory.isDbHistoryUsed()) {
      executeMandatorySchemaResource("drop", "history");
    }
    if (isIdentityTablePresent() && dbSqlSessionFactory.isDbIdentityUsed()) {
      executeMandatorySchemaResource("drop", "identity");
    }
  }

  public void executeMandatorySchemaResource(String operation, String component) {
    executeSchemaResource(operation, component, getResourceForDbOperation(operation, operation, component), false);
  }

  public static String[] JDBC_METADATA_TABLE_TYPES = { "TABLE" };

  public String dbSchemaUpdate() {

    String feedback = null;
    boolean isUpgradeNeeded = false;
    int matchingVersionIndex = -1;

    if (isEngineTablePresent()) {

      PropertyEntity dbVersionProperty = selectById(PropertyEntity.class, "schema.version");
      String dbVersion = dbVersionProperty.getValue();

      // Determine index in the sequence of Activiti releases
      matchingVersionIndex = findMatchingVersionIndex(dbVersion);
      
      // If no match has been found, but the version starts with '5.x', 
      // we assume it's the last version (see comment in the VERSIONS list)
      if (matchingVersionIndex < 0 && dbVersion != null && dbVersion.startsWith("5.")) {
        matchingVersionIndex = findMatchingVersionIndex(LAST_V5_VERSION);
      }

      // Exception when no match was found: unknown/unsupported version
      if (matchingVersionIndex < 0) {
        throw new ActivitiException("Could not update Activiti database schema: unknown version from database: '" + dbVersion + "'");
      }

      isUpgradeNeeded = (matchingVersionIndex != (ACTIVITI_VERSIONS.size() - 1));

      if (isUpgradeNeeded) {
        dbVersionProperty.setValue(ProcessEngine.VERSION);

        PropertyEntity dbHistoryProperty;
        if ("5.0".equals(dbVersion)) {
          dbHistoryProperty = Context.getCommandContext().getPropertyEntityManager().create();
          dbHistoryProperty.setName("schema.history");
          dbHistoryProperty.setValue("create(5.0)");
          insert(dbHistoryProperty);
        } else {
          dbHistoryProperty = selectById(PropertyEntity.class, "schema.history");
        }

        // Set upgrade history
        String dbHistoryValue = dbHistoryProperty.getValue() + " upgrade(" + dbVersion + "->" + ProcessEngine.VERSION + ")";
        dbHistoryProperty.setValue(dbHistoryValue);

        // Engine upgrade
        dbSchemaUpgrade("engine", matchingVersionIndex);
        feedback = "upgraded Activiti from " + dbVersion + " to " + ProcessEngine.VERSION;
      }

    } else {
      dbSchemaCreateEngine();
    }
    if (isHistoryTablePresent()) {
      if (isUpgradeNeeded) {
        dbSchemaUpgrade("history", matchingVersionIndex);
      }
    } else if (dbSqlSessionFactory.isDbHistoryUsed()) {
      dbSchemaCreateHistory();
    }

    if (isIdentityTablePresent()) {
      if (isUpgradeNeeded) {
        dbSchemaUpgrade("identity", matchingVersionIndex);
      }
    } else if (dbSqlSessionFactory.isDbIdentityUsed()) {
      dbSchemaCreateIdentity();
    }

    return feedback;
  }

  /**
   * Returns the index in the list of {@link #ACTIVITI_VERSIONS} matching the provided string version.
   * Returns -1 if no match can be found.  
   */
  protected int findMatchingVersionIndex(String dbVersion) {
    int index = 0;
    int matchingVersionIndex = -1;
    while (matchingVersionIndex < 0 && index < ACTIVITI_VERSIONS.size()) {
      if (ACTIVITI_VERSIONS.get(index).matches(dbVersion)) {
        matchingVersionIndex = index;
      } else {
        index++;
      }
    }
    return matchingVersionIndex;
  }

  public boolean isEngineTablePresent() {
    return isTablePresent("ACT_RU_EXECUTION");
  }

  public boolean isHistoryTablePresent() {
    return isTablePresent("ACT_HI_PROCINST");
  }

  public boolean isIdentityTablePresent() {
    return isTablePresent("ACT_ID_USER");
  }

  public boolean isTablePresent(String tableName) {
    // ACT-1610: in case the prefix IS the schema itself, we don't add the
    // prefix, since the check is already aware of the schema
    if (!dbSqlSessionFactory.isTablePrefixIsSchema()) {
      tableName = prependDatabaseTablePrefix(tableName);
    }

    Connection connection = null;
    try {
      connection = sqlSession.getConnection();
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      ResultSet tables = null;

      String catalog = this.connectionMetadataDefaultCatalog;
      if (dbSqlSessionFactory.getDatabaseCatalog() != null && dbSqlSessionFactory.getDatabaseCatalog().length() > 0) {
        catalog = dbSqlSessionFactory.getDatabaseCatalog();
      }

      String schema = this.connectionMetadataDefaultSchema;
      if (dbSqlSessionFactory.getDatabaseSchema() != null && dbSqlSessionFactory.getDatabaseSchema().length() > 0) {
        schema = dbSqlSessionFactory.getDatabaseSchema();
      }

      String databaseType = dbSqlSessionFactory.getDatabaseType();

      if ("postgres".equals(databaseType)) {
        tableName = tableName.toLowerCase();
      }

      try {
        tables = databaseMetaData.getTables(catalog, schema, tableName, JDBC_METADATA_TABLE_TYPES);
        return tables.next();
      } finally {
        try {
          tables.close();
        } catch (Exception e) {
          log.error("Error closing meta data tables", e);
        }
      }

    } catch (Exception e) {
      throw new ActivitiException("couldn't check if tables are already present using metadata: " + e.getMessage(), e);
    }
  }

  protected boolean isUpgradeNeeded(String versionInDatabase) {
    if (ProcessEngine.VERSION.equals(versionInDatabase)) {
      return false;
    }

    String cleanDbVersion = getCleanVersion(versionInDatabase);
    String[] cleanDbVersionSplitted = cleanDbVersion.split("\\.");
    int dbMajorVersion = Integer.valueOf(cleanDbVersionSplitted[0]);
    int dbMinorVersion = Integer.valueOf(cleanDbVersionSplitted[1]);

    String cleanEngineVersion = getCleanVersion(ProcessEngine.VERSION);
    String[] cleanEngineVersionSplitted = cleanEngineVersion.split("\\.");
    int engineMajorVersion = Integer.valueOf(cleanEngineVersionSplitted[0]);
    int engineMinorVersion = Integer.valueOf(cleanEngineVersionSplitted[1]);

    if ((dbMajorVersion > engineMajorVersion) || ((dbMajorVersion <= engineMajorVersion) && (dbMinorVersion > engineMinorVersion))) {
      throw new ActivitiException("Version of activiti database (" + versionInDatabase + ") is more recent than the engine (" + ProcessEngine.VERSION + ")");
    } else if (cleanDbVersion.compareTo(cleanEngineVersion) == 0) {
      // Versions don't match exactly, possibly snapshot is being used
      log.warn("Engine-version is the same, but not an exact match: {} vs. {}. Not performing database-upgrade.", versionInDatabase, ProcessEngine.VERSION);
      return false;
    }
    return true;
  }

  protected String getCleanVersion(String versionString) {
    Matcher matcher = CLEAN_VERSION_REGEX.matcher(versionString);
    if (!matcher.find()) {
      throw new ActivitiException("Illegal format for version: " + versionString);
    }

    String cleanString = matcher.group();
    try {
      Double.parseDouble(cleanString); // try to parse it, to see if it is
                                       // really a number
      return cleanString;
    } catch (NumberFormatException nfe) {
      throw new ActivitiException("Illegal format for version: " + versionString);
    }
  }

  protected String prependDatabaseTablePrefix(String tableName) {
    return dbSqlSessionFactory.getDatabaseTablePrefix() + tableName;
  }

  protected void dbSchemaUpgrade(final String component, final int currentDatabaseVersionsIndex) {
    ActivitiVersion activitiVersion = ACTIVITI_VERSIONS.get(currentDatabaseVersionsIndex);
    String dbVersion = activitiVersion.getMainVersion();
    log.info("upgrading activiti {} schema from {} to {}", component, dbVersion, ProcessEngine.VERSION);

    // Actual execution of schema DDL SQL
    for (int i = currentDatabaseVersionsIndex + 1; i < ACTIVITI_VERSIONS.size(); i++) {
      String nextVersion = ACTIVITI_VERSIONS.get(i).getMainVersion();

      // Taking care of -SNAPSHOT version in development
      if (nextVersion.endsWith("-SNAPSHOT")) {
        nextVersion = nextVersion.substring(0, nextVersion.length() - "-SNAPSHOT".length());
      }

      dbVersion = dbVersion.replace(".", "");
      nextVersion = nextVersion.replace(".", "");
      log.info("Upgrade needed: {} -> {}. Looking for schema update resource for component '{}'", dbVersion, nextVersion, component);
      executeSchemaResource("upgrade", component, getResourceForDbOperation("upgrade", "upgradestep." + dbVersion + ".to." + nextVersion, component), true);
      dbVersion = nextVersion;
    }
  }

  public String getResourceForDbOperation(String directory, String operation, String component) {
    String databaseType = dbSqlSessionFactory.getDatabaseType();
    return "org/activiti/db/" + directory + "/activiti." + databaseType + "." + operation + "." + component + ".sql";
  }

  public void executeSchemaResource(String operation, String component, String resourceName, boolean isOptional) {
    InputStream inputStream = null;
    try {
      inputStream = ReflectUtil.getResourceAsStream(resourceName);
      if (inputStream == null) {
        if (isOptional) {
          log.info("no schema resource {} for {}", resourceName, operation);
        } else {
          throw new ActivitiException("resource '" + resourceName + "' is not available");
        }
      } else {
        executeSchemaResource(operation, component, resourceName, inputStream);
      }

    } finally {
      IoUtil.closeSilently(inputStream);
    }
  }

  private void executeSchemaResource(String operation, String component, String resourceName, InputStream inputStream) {
    log.info("performing {} on {} with resource {}", operation, component, resourceName);
    String sqlStatement = null;
    String exceptionSqlStatement = null;
    try {
      Connection connection = sqlSession.getConnection();
      Exception exception = null;
      byte[] bytes = IoUtil.readInputStream(inputStream, resourceName);
      String ddlStatements = new String(bytes);

      // Special DDL handling for certain databases
      try {
        if (isMysql()) {
          DatabaseMetaData databaseMetaData = connection.getMetaData();
          int majorVersion = databaseMetaData.getDatabaseMajorVersion();
          int minorVersion = databaseMetaData.getDatabaseMinorVersion();
          log.info("Found MySQL: majorVersion=" + majorVersion + " minorVersion=" + minorVersion);

          // Special care for MySQL < 5.6
          if (majorVersion <= 5 && minorVersion < 6) {
            ddlStatements = updateDdlForMySqlVersionLowerThan56(ddlStatements);
          }
        }
      } catch (Exception e) {
        log.info("Could not get database metadata", e);
      }

      BufferedReader reader = new BufferedReader(new StringReader(ddlStatements));
      String line = readNextTrimmedLine(reader);
      boolean inOraclePlsqlBlock = false;
      while (line != null) {
        if (line.startsWith("# ")) {
          log.debug(line.substring(2));

        } else if (line.startsWith("-- ")) {
          log.debug(line.substring(3));

        } else if (line.startsWith("execute java ")) {
          String upgradestepClassName = line.substring(13).trim();
          DbUpgradeStep dbUpgradeStep = null;
          try {
            dbUpgradeStep = (DbUpgradeStep) ReflectUtil.instantiate(upgradestepClassName);
          } catch (ActivitiException e) {
            throw new ActivitiException("database update java class '" + upgradestepClassName + "' can't be instantiated: " + e.getMessage(), e);
          }
          try {
            log.debug("executing upgrade step java class {}", upgradestepClassName);
            dbUpgradeStep.execute(this);
          } catch (Exception e) {
            throw new ActivitiException("error while executing database update java class '" + upgradestepClassName + "': " + e.getMessage(), e);
          }

        } else if (line.length() > 0) {

          if (isOracle() && line.startsWith("begin")) {
            inOraclePlsqlBlock = true;
            sqlStatement = addSqlStatementPiece(sqlStatement, line);

          } else if ((line.endsWith(";") && inOraclePlsqlBlock == false) || (line.startsWith("/") && inOraclePlsqlBlock == true)) {

            if (inOraclePlsqlBlock) {
              inOraclePlsqlBlock = false;
            } else {
              sqlStatement = addSqlStatementPiece(sqlStatement, line.substring(0, line.length() - 1));
            }

            Statement jdbcStatement = connection.createStatement();
            try {
              // no logging needed as the connection will log it
              log.debug("SQL: {}", sqlStatement);
              jdbcStatement.execute(sqlStatement);
              jdbcStatement.close();
            } catch (Exception e) {
              if (exception == null) {
                exception = e;
                exceptionSqlStatement = sqlStatement;
              }
              log.error("problem during schema {}, statement {}", operation, sqlStatement, e);
            } finally {
              sqlStatement = null;
            }
          } else {
            sqlStatement = addSqlStatementPiece(sqlStatement, line);
          }
        }

        line = readNextTrimmedLine(reader);
      }

      if (exception != null) {
        throw exception;
      }

      log.debug("activiti db schema {} for component {} successful", operation, component);

    } catch (Exception e) {
      throw new ActivitiException("couldn't " + operation + " db schema: " + exceptionSqlStatement, e);
    }
  }

  /**
   * MySQL is funny when it comes to timestamps and dates.
   * 
   * More specifically, for a DDL statement like 'MYCOLUMN timestamp(3)': - MySQL 5.6.4+ has support for timestamps/dates with millisecond (or smaller) precision. The DDL above works and the data in
   * the table will have millisecond precision - MySQL < 5.5.3 allows the DDL statement, but ignores it. The DDL above works but the data won't have millisecond precision - MySQL 5.5.3 < [version] <
   * 5.6.4 gives and exception when using the DDL above.
   * 
   * Also, the 5.5 and 5.6 branches of MySQL are both actively developed and patched.
   * 
   * Hence, when doing auto-upgrade/creation of the Activiti tables, the default MySQL DDL file is used and all timestamps/datetimes are converted to not use the millisecond precision by string
   * replacement done in the method below.
   * 
   * If using the DDL files directly (which is a sane choice in production env.), there is a distinction between MySQL version < 5.6.
   */
  protected String updateDdlForMySqlVersionLowerThan56(String ddlStatements) {
    return ddlStatements.replace("timestamp(3)", "timestamp").replace("datetime(3)", "datetime").replace("TIMESTAMP(3)", "TIMESTAMP").replace("DATETIME(3)", "DATETIME");
  }

  protected String addSqlStatementPiece(String sqlStatement, String line) {
    if (sqlStatement == null) {
      return line;
    }
    return sqlStatement + " \n" + line;
  }

  protected String readNextTrimmedLine(BufferedReader reader) throws IOException {
    String line = reader.readLine();
    if (line != null) {
      line = line.trim();
    }
    return line;
  }

  protected boolean isMissingTablesException(Exception e) {
    String exceptionMessage = e.getMessage();
    if (e.getMessage() != null) {
      // Matches message returned from H2
      if ((exceptionMessage.indexOf("Table") != -1) && (exceptionMessage.indexOf("not found") != -1)) {
        return true;
      }

      // Message returned from MySQL and Oracle
      if (((exceptionMessage.indexOf("Table") != -1 || exceptionMessage.indexOf("table") != -1)) && (exceptionMessage.indexOf("doesn't exist") != -1)) {
        return true;
      }

      // Message returned from Postgres
      if (((exceptionMessage.indexOf("relation") != -1 || exceptionMessage.indexOf("table") != -1)) && (exceptionMessage.indexOf("does not exist") != -1)) {
        return true;
      }
    }
    return false;
  }

  public void performSchemaOperationsProcessEngineBuild() {
    String databaseSchemaUpdate = Context.getProcessEngineConfiguration().getDatabaseSchemaUpdate();
    if (ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(databaseSchemaUpdate)) {
      try {
        dbSchemaDrop();
      } catch (RuntimeException e) {
        // ignore
      }
    }
    if (org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP.equals(databaseSchemaUpdate)
        || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(databaseSchemaUpdate) || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE.equals(databaseSchemaUpdate)) {
      dbSchemaCreate();

    } else if (org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE.equals(databaseSchemaUpdate)) {
      dbSchemaCheckVersion();

    } else if (ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE.equals(databaseSchemaUpdate)) {
      dbSchemaUpdate();
    }
  }

  public void performSchemaOperationsProcessEngineClose() {
    String databaseSchemaUpdate = Context.getProcessEngineConfiguration().getDatabaseSchemaUpdate();
    if (org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP.equals(databaseSchemaUpdate)) {
      dbSchemaDrop();
    }
  }

  public <T> T getCustomMapper(Class<T> type) {
    return sqlSession.getMapper(type);
  }
  
  public boolean isMysql() {
    return dbSqlSessionFactory.getDatabaseType().equals("mysql");
  }
  
  public boolean isOracle() {
    return dbSqlSessionFactory.getDatabaseType().equals("oracle");
  }

  // query factory methods
  // ////////////////////////////////////////////////////

  public DeploymentQueryImpl createDeploymentQuery() {
    return new DeploymentQueryImpl();
  }

  public ModelQueryImpl createModelQueryImpl() {
    return new ModelQueryImpl();
  }

  public ProcessDefinitionQueryImpl createProcessDefinitionQuery() {
    return new ProcessDefinitionQueryImpl();
  }

  public ProcessInstanceQueryImpl createProcessInstanceQuery() {
    return new ProcessInstanceQueryImpl();
  }

  public ExecutionQueryImpl createExecutionQuery() {
    return new ExecutionQueryImpl();
  }

  public TaskQueryImpl createTaskQuery() {
    return new TaskQueryImpl();
  }

  public JobQueryImpl createJobQuery() {
    return new JobQueryImpl();
  }

  public HistoricProcessInstanceQueryImpl createHistoricProcessInstanceQuery() {
    return new HistoricProcessInstanceQueryImpl();
  }

  public HistoricActivityInstanceQueryImpl createHistoricActivityInstanceQuery() {
    return new HistoricActivityInstanceQueryImpl();
  }

  public HistoricTaskInstanceQueryImpl createHistoricTaskInstanceQuery() {
    return new HistoricTaskInstanceQueryImpl();
  }

  public HistoricDetailQueryImpl createHistoricDetailQuery() {
    return new HistoricDetailQueryImpl();
  }

  public HistoricVariableInstanceQueryImpl createHistoricVariableInstanceQuery() {
    return new HistoricVariableInstanceQueryImpl();
  }

  public UserQueryImpl createUserQuery() {
    return new UserQueryImpl();
  }

  public GroupQueryImpl createGroupQuery() {
    return new GroupQueryImpl();
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  public SqlSession getSqlSession() {
    return sqlSession;
  }

  public DbSqlSessionFactory getDbSqlSessionFactory() {
    return dbSqlSessionFactory;
  }

}
