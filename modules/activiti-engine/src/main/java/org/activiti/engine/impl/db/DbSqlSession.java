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
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.entity.PropertyEntity;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.variable.DeserializedObject;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** responsibilities:
 *   - delayed flushing of inserts updates and deletes
 *   - optional dirty checking
 *   - db specific statement name mapping
 *   
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DbSqlSession implements Session {
  
  private static final Logger log = LoggerFactory.getLogger(DbSqlSession.class);
  
  protected static final Pattern CLEAN_VERSION_REGEX = Pattern.compile("\\d\\.\\d*");
  
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
	  ACTIVITI_VERSIONS.add(new ActivitiVersion("5.20.0.0"));
	  ACTIVITI_VERSIONS.add(new ActivitiVersion("5.20.0.1"));
	  ACTIVITI_VERSIONS.add(new ActivitiVersion("5.20.0.2"));
	  ACTIVITI_VERSIONS.add(new ActivitiVersion("5.21.0.0"));
	  
	  /* Current */
	  ACTIVITI_VERSIONS.add(new ActivitiVersion(ProcessEngine.VERSION));
  }

  protected SqlSession sqlSession;
  protected DbSqlSessionFactory dbSqlSessionFactory;
  protected Map<Class<? extends PersistentObject>, List<PersistentObject>> insertedObjects = new HashMap<Class<? extends PersistentObject>, List<PersistentObject>>();
  protected Map<Class<?>, Map<String, CachedObject>> cachedObjects = new HashMap<Class<?>, Map<String,CachedObject>>();
  protected List<DeleteOperation> deleteOperations = new ArrayList<DeleteOperation>();
  protected List<DeserializedObject> deserializedObjects = new ArrayList<DeserializedObject>();
  protected String connectionMetadataDefaultCatalog;
  protected String connectionMetadataDefaultSchema;
  
  public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    this.sqlSession = dbSqlSessionFactory
      .getSqlSessionFactory()
      .openSession();
  }

  public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory, Connection connection, String catalog, String schema) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    this.sqlSession = dbSqlSessionFactory
      .getSqlSessionFactory()
      .openSession(connection);
    this.connectionMetadataDefaultCatalog = catalog;
    this.connectionMetadataDefaultSchema = schema;
  }
  
  // insert ///////////////////////////////////////////////////////////////////
  
  
  public void insert(PersistentObject persistentObject) {
    if (persistentObject.getId()==null) {
      String id = dbSqlSessionFactory.getIdGenerator().getNextId();  
      persistentObject.setId(id);
    }
    
    Class<? extends PersistentObject> clazz = persistentObject.getClass();
    if (!insertedObjects.containsKey(clazz)) {
    	insertedObjects.put(clazz, new ArrayList<PersistentObject>());
    }
    
    insertedObjects.get(clazz).add(persistentObject);
    cachePut(persistentObject, false);
  }
  
  // update ///////////////////////////////////////////////////////////////////
  
  public void update(PersistentObject persistentObject) {
    cachePut(persistentObject, false);
  }
  
  public int update(String statement, Object parameters) {
     String updateStatement = dbSqlSessionFactory.mapStatement(statement);
     return getSqlSession().update(updateStatement, parameters);
  }
  
  // delete ///////////////////////////////////////////////////////////////////

  public void delete(String statement, Object parameter) {
    deleteOperations.add(new BulkDeleteOperation(statement, parameter));
  }
  
  public void delete(PersistentObject persistentObject) {
    for (DeleteOperation deleteOperation: deleteOperations) {
        if (deleteOperation.sameIdentity(persistentObject)) {
          log.debug("skipping redundant delete: {}", persistentObject);
          return; // Skip this delete. It was already added.
        }
    }
    
    deleteOperations.add(new CheckedDeleteOperation(persistentObject));
  }

  public interface DeleteOperation {
  	
  	/**
  	 * @return The persistent object class that is being deleted.
  	 *         Null in case there are multiple objects of different types!
  	 */
  	Class<? extends PersistentObject> getPersistentObjectClass();
    
    boolean sameIdentity(PersistentObject other);

    void clearCache();
    
    void execute();
    
  }

  /**
   * Use this {@link DeleteOperation} to execute a dedicated delete statement.
   * It is important to note there won't be any optimistic locking checks done 
   * for these kind of delete operations!
   * 
   * For example, a usage of this operation would be to delete all variables for
   * a certain execution, when that certain execution is removed. The optimistic locking
   * happens on the execution, but the variables can be removed by a simple
   * 'delete from var_table where execution_id is xxx'. It could very well be there
   * are no variables, which would also work with this query, but not with the 
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
    public Class<? extends PersistentObject> getPersistentObjectClass() {
    	return null;
    }
    
    @Override
    public boolean sameIdentity(PersistentObject other) {
      // this implementation is unable to determine what the identity of the removed object(s) will be.
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
   * A {@link DeleteOperation} that checks for concurrent modifications if the persistent object implements {@link HasRevision}.
   * That is, it employs optimisting concurrency control. Used when the persistent object has been fetched already.
   */
  public class CheckedDeleteOperation implements DeleteOperation {
    protected final PersistentObject persistentObject;
    
    public CheckedDeleteOperation(PersistentObject persistentObject) {
      this.persistentObject = persistentObject;
    }
    
    @Override
    public Class<? extends PersistentObject> getPersistentObjectClass() {
    	return persistentObject.getClass();
    }
    
    @Override
    public boolean sameIdentity(PersistentObject other) {
      return persistentObject.getClass().equals(other.getClass())
          && persistentObject.getId().equals(other.getId());
    }

    @Override
    public void clearCache() {
      cacheRemove(persistentObject.getClass(), persistentObject.getId());
    }
    
    @Override
    public void execute() {
      String deleteStatement = dbSqlSessionFactory.getDeleteStatement(persistentObject.getClass());
      deleteStatement = dbSqlSessionFactory.mapStatement(deleteStatement);
      if (deleteStatement == null) {
        throw new ActivitiException("no delete statement for " + persistentObject.getClass() + " in the ibatis mapping files");
      }
      
      // It only makes sense to check for optimistic locking exceptions for objects that actually have a revision
      if (persistentObject instanceof HasRevision) {
        int nrOfRowsDeleted = sqlSession.delete(deleteStatement, persistentObject);
        if (nrOfRowsDeleted == 0) {
          throw new ActivitiOptimisticLockingException(persistentObject + " was updated by another transaction concurrently");
        }
      } else {
        sqlSession.delete(deleteStatement, persistentObject);
      }
    }

    public PersistentObject getPersistentObject() {
      return persistentObject;
    }

    @Override
    public String toString() {
      return "delete " + persistentObject;
    }
  }
  
  
  /**
   * A bulk version of the {@link CheckedDeleteOperation}.
   */
  public class BulkCheckedDeleteOperation implements DeleteOperation {
  	
  	protected Class<? extends PersistentObject> persistentObjectClass;
    protected List<PersistentObject> persistentObjects = new ArrayList<PersistentObject>();
    
    public BulkCheckedDeleteOperation(Class<? extends PersistentObject> persistentObjectClass) {
    	this.persistentObjectClass = persistentObjectClass;
    }
    
    public void addPersistentObject(PersistentObject persistentObject) {
    	persistentObjects.add(persistentObject);
    }
    
    @Override
    public boolean sameIdentity(PersistentObject other) {
    	for (PersistentObject persistentObject : persistentObjects) {
    		if (persistentObject.getClass().equals(other.getClass()) && persistentObject.getId().equals(other.getId())) {
    			return true;
    		}
    	}
    	return false;
    }

    @Override
    public void clearCache() {
    	for (PersistentObject persistentObject : persistentObjects) {
    		cacheRemove(persistentObject.getClass(), persistentObject.getId());
    	}
    }
    
    @Override
    public void execute() {
    	
    	if (persistentObjects.isEmpty()) {
    		return;
    	}
    	
      String bulkDeleteStatement = dbSqlSessionFactory.getBulkDeleteStatement(persistentObjectClass);
      bulkDeleteStatement = dbSqlSessionFactory.mapStatement(bulkDeleteStatement);
      if (bulkDeleteStatement == null) {
        throw new ActivitiException("no bulk delete statement for " + persistentObjectClass + " in the mapping files");
      }
      
      // It only makes sense to check for optimistic locking exceptions for objects that actually have a revision
      if (persistentObjects.get(0) instanceof HasRevision) {
        int nrOfRowsDeleted = sqlSession.delete(bulkDeleteStatement, persistentObjects);
        if (nrOfRowsDeleted < persistentObjects.size()) {
          throw new ActivitiOptimisticLockingException("One of the entities " + persistentObjectClass 
          		+ " was updated by another transaction concurrently while trying to do a bulk delete");
        }
      } else {
        sqlSession.delete(bulkDeleteStatement, persistentObjects);
      }
    }
    
    public Class<? extends PersistentObject> getPersistentObjectClass() {
			return persistentObjectClass;
		}

		public void setPersistentObjectClass(
		    Class<? extends PersistentObject> persistentObjectClass) {
			this.persistentObjectClass = persistentObjectClass;
		}

		public List<PersistentObject> getPersistentObjects() {
			return persistentObjects;
		}

		public void setPersistentObjects(List<PersistentObject> persistentObjects) {
			this.persistentObjects = persistentObjects;
		}

		@Override
    public String toString() {
      return "bulk delete of " + persistentObjects.size() + (!persistentObjects.isEmpty() ? " entities of " + persistentObjects.get(0).getClass() : 0 );
    }
  }
  
  // select ///////////////////////////////////////////////////////////////////

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
    if (page!=null) {
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
    if (firstResult == -1 ||  maxResults == -1) {
      return Collections.EMPTY_LIST;
    }    
    List loadedObjects = sqlSession.selectList(statement, parameter);
    return filterLoadedObjects(loadedObjects);
  }
  
  @SuppressWarnings({ "rawtypes" })
  public List selectListWithRawParameterWithoutFilter(String statement, Object parameter, int firstResult, int maxResults) {
    statement = dbSqlSessionFactory.mapStatement(statement);    
    if (firstResult == -1 ||  maxResults == -1) {
      return Collections.EMPTY_LIST;
    }    
    return sqlSession.selectList(statement, parameter);
  }

  public Object selectOne(String statement, Object parameter) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    Object result = sqlSession.selectOne(statement, parameter);
    if (result instanceof PersistentObject) {
      PersistentObject loadedObject = (PersistentObject) result;
      result = cacheFilter(loadedObject);
    }
    return result;
  }
  
  @SuppressWarnings("unchecked")
  public <T extends PersistentObject> T selectById(Class<T> entityClass, String id) {
    T persistentObject = cacheGet(entityClass, id);
    if (persistentObject!=null) {
      return persistentObject;
    }
    String selectStatement = dbSqlSessionFactory.getSelectStatement(entityClass);
    selectStatement = dbSqlSessionFactory.mapStatement(selectStatement);
    persistentObject = (T) sqlSession.selectOne(selectStatement, id);
    if (persistentObject==null) {
      return null;
    }
    cachePut(persistentObject, true);
    return persistentObject;
  }

  // internal session cache ///////////////////////////////////////////////////
  
  @SuppressWarnings("rawtypes")
  protected List filterLoadedObjects(List<Object> loadedObjects) {
    if (loadedObjects.isEmpty()) {
      return loadedObjects;
    }
    if (!(loadedObjects.get(0) instanceof PersistentObject)) {
      return loadedObjects;
    }
    
    List<PersistentObject> filteredObjects = new ArrayList<PersistentObject>(loadedObjects.size());
    for (Object loadedObject: loadedObjects) {
      PersistentObject cachedPersistentObject = cacheFilter((PersistentObject) loadedObject);
      filteredObjects.add(cachedPersistentObject);
    }
    return filteredObjects;
  }

  protected CachedObject cachePut(PersistentObject persistentObject, boolean storeState) {
    Map<String, CachedObject> classCache = cachedObjects.get(persistentObject.getClass());
    if (classCache==null) {
      classCache = new HashMap<String, CachedObject>();
      cachedObjects.put(persistentObject.getClass(), classCache);
    }
    CachedObject cachedObject = new CachedObject(persistentObject, storeState);
    classCache.put(persistentObject.getId(), cachedObject);
    return cachedObject;
  }
  
  /** returns the object in the cache.  if this object was loaded before, 
   * then the original object is returned.  if this is the first time 
   * this object is loaded, then the loadedObject is added to the cache. */
  protected PersistentObject cacheFilter(PersistentObject persistentObject) {
    PersistentObject cachedPersistentObject = cacheGet(persistentObject.getClass(), persistentObject.getId());
    if (cachedPersistentObject!=null) {
      return cachedPersistentObject;
    }
    cachePut(persistentObject, true);
    return persistentObject;
  }

  @SuppressWarnings("unchecked")
  protected <T> T cacheGet(Class<T> entityClass, String id) {
    CachedObject cachedObject = null;
    Map<String, CachedObject> classCache = cachedObjects.get(entityClass);
    if (classCache!=null) {
      cachedObject = classCache.get(id);
    }
    if (cachedObject!=null) {
      return (T) cachedObject.getPersistentObject();
    }
    return null;
  }
  
  protected void cacheRemove(Class<?> persistentObjectClass, String persistentObjectId) {
    Map<String, CachedObject> classCache = cachedObjects.get(persistentObjectClass);
    if (classCache==null) {
      return;
    }
    classCache.remove(persistentObjectId);
  }
  
  @SuppressWarnings("unchecked")
  public <T> List<T> findInCache(Class<T> entityClass) {
    Map<String, CachedObject> classCache = cachedObjects.get(entityClass);
    if (classCache!=null) {
      List<T> entities = new ArrayList<T>(classCache.size());
      for (CachedObject cachedObject: classCache.values()) {
        entities.add((T) cachedObject.getPersistentObject());
      }
      return entities;
    }
    return Collections.emptyList();
  }
  
  public <T> T findInCache(Class<T> entityClass, String id) {
    return cacheGet(entityClass, id);
  }
  
  public static class CachedObject {
    protected PersistentObject persistentObject;
    protected Object persistentObjectState;
    
    public CachedObject(PersistentObject persistentObject, boolean storeState) {
      this.persistentObject = persistentObject;
      if (storeState) {
        this.persistentObjectState = persistentObject.getPersistentState();
      }
    }

    public PersistentObject getPersistentObject() {
      return persistentObject;
    }

    public Object getPersistentObjectState() {
      return persistentObjectState;
    }
  }

  // deserialized objects /////////////////////////////////////////////////////
  
  public void addDeserializedObject(DeserializedObject deserializedObject) {
  	deserializedObjects.add(deserializedObject);
  }

  // flush ////////////////////////////////////////////////////////////////////

  @Override
  public void flush() {
    List<DeleteOperation> removedOperations = removeUnnecessaryOperations();
    
    flushDeserializedObjects();
    List<PersistentObject> updatedObjects = getUpdatedObjects();
    
    if (log.isDebugEnabled()) {
      Collection<List<PersistentObject>> insertedObjectLists = insertedObjects.values();
      int nrOfInserts = 0, nrOfUpdates = 0, nrOfDeletes = 0;
      for (List<PersistentObject> insertedObjectList: insertedObjectLists) {
      	for (PersistentObject insertedObject : insertedObjectList) {
      		log.debug("  insert {}", insertedObject);
      		nrOfInserts++;
      	}
      }
      for (PersistentObject updatedObject: updatedObjects) {
        log.debug("  update {}", updatedObject);
        nrOfUpdates++;
      }
      for (DeleteOperation deleteOperation: deleteOperations) {
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
   * Clears all deleted and inserted objects from the cache, 
   * and removes inserts and deletes that cancel each other.
   */
  protected List<DeleteOperation> removeUnnecessaryOperations() {
    List<DeleteOperation> removedDeleteOperations = new ArrayList<DeleteOperation>();

    for (Iterator<DeleteOperation> deleteIterator = deleteOperations.iterator(); deleteIterator.hasNext();) {
    	
      DeleteOperation deleteOperation = deleteIterator.next();
      Class<? extends PersistentObject> deletedPersistentObjectClass = deleteOperation.getPersistentObjectClass();
      
      List<PersistentObject> insertedObjectsOfSameClass = insertedObjects.get(deletedPersistentObjectClass);
      if (insertedObjectsOfSameClass != null && insertedObjectsOfSameClass.size() > 0) {
      	
	      for (Iterator<PersistentObject> insertIterator = insertedObjectsOfSameClass.iterator(); insertIterator.hasNext();) {
	        PersistentObject insertedObject = insertIterator.next();
	        
	        // if the deleted object is inserted,
	        if (deleteOperation.sameIdentity(insertedObject)) {
	          // remove the insert and the delete, they cancel each other
	          insertIterator.remove();
	          deleteIterator.remove();
	          // add removed operations to be able to fire events
	          removedDeleteOperations.add( deleteOperation);
	        }
	      }
	      
	      if (insertedObjects.get(deletedPersistentObjectClass).size() == 0) {
	      	insertedObjects.remove(deletedPersistentObjectClass);
	      }
	      
      }
      
      // in any case, remove the deleted object from the cache
      deleteOperation.clearCache();
    }
    
    for (Class<? extends PersistentObject> persistentObjectClass : insertedObjects.keySet()) {
    	for (PersistentObject insertedObject : insertedObjects.get(persistentObjectClass)) {
    		cacheRemove(insertedObject.getClass(), insertedObject.getId());
    	}
    }

    return removedDeleteOperations;
  }
  
//  
//  [Joram] Put this in comments. Had all kinds of errors.
//  
//  /**
//   * Optimizes the given delete operations:
//   * for example, if there are two deletes for two different variables, merges this into
//   * one bulk delete which improves performance
//   */
//  protected List<DeleteOperation> optimizeDeleteOperations(List<DeleteOperation> deleteOperations) {
//  	
//  	// No optimization possible for 0 or 1 operations
//  	if (!isOptimizeDeleteOperationsEnabled || deleteOperations.size() <= 1) {
//  		return deleteOperations;
//  	}
//  	
//  	List<DeleteOperation> optimizedDeleteOperations = new ArrayList<DbSqlSession.DeleteOperation>();
//  	boolean[] checkedIndices = new boolean[deleteOperations.size()];
//  	for (int i=0; i<deleteOperations.size(); i++) {
//  		
//  		if (checkedIndices[i] == true) {
//  			continue;
//  		}
//  		
//  		DeleteOperation deleteOperation = deleteOperations.get(i);
//  		boolean couldOptimize = false;
//  		if (deleteOperation instanceof CheckedDeleteOperation) {
//  			
//  			PersistentObject persistentObject = ((CheckedDeleteOperation) deleteOperation).getPersistentObject();
//  			if (persistentObject instanceof BulkDeleteable) {
//				String bulkDeleteStatement = dbSqlSessionFactory.getBulkDeleteStatement(persistentObject.getClass());
//				bulkDeleteStatement = dbSqlSessionFactory.mapStatement(bulkDeleteStatement);
//				if (bulkDeleteStatement != null) {
//					BulkCheckedDeleteOperation bulkCheckedDeleteOperation = null;
//					
//					// Find all objects of the same type
//					for (int j=0; j<deleteOperations.size(); j++) {
//						DeleteOperation otherDeleteOperation = deleteOperations.get(j);
//						if (j != i && checkedIndices[j] == false && otherDeleteOperation instanceof CheckedDeleteOperation) {
//							PersistentObject otherPersistentObject = ((CheckedDeleteOperation) otherDeleteOperation).getPersistentObject();
//							if (otherPersistentObject.getClass().equals(persistentObject.getClass())) {
//	  							if (bulkCheckedDeleteOperation == null) {
//	  								bulkCheckedDeleteOperation = new BulkCheckedDeleteOperation(persistentObject.getClass());
//	  								bulkCheckedDeleteOperation.addPersistentObject(persistentObject);
//	  								optimizedDeleteOperations.add(bulkCheckedDeleteOperation);
//	  							}
//	  							couldOptimize = true;
//	  							bulkCheckedDeleteOperation.addPersistentObject(otherPersistentObject);
//	  							checkedIndices[j] = true;
//							} else {
//							    // We may only optimize subsequent delete operations of the same type, to prevent messing up 
//							    // the order of deletes of related entities which may depend on the referenced entity being deleted before
//							    break;
//							}
//						}
//						
//					}
//				}
//  			}
//  		}
//  		
//   		if (!couldOptimize) {
//  			optimizedDeleteOperations.add(deleteOperation);
//  		}
//  		checkedIndices[i]=true;
//  		
//  	}
//  	return optimizedDeleteOperations;
//  }

  protected void flushDeserializedObjects() {
    for (DeserializedObject deserializedObject: deserializedObjects) {
      deserializedObject.flush();
    }
  }

  public List<PersistentObject> getUpdatedObjects() {
    List<PersistentObject> updatedObjects = new ArrayList<PersistentObject>();
    for (Class<?> clazz: cachedObjects.keySet()) {
      
      Map<String, CachedObject> classCache = cachedObjects.get(clazz);
      for (CachedObject cachedObject: classCache.values()) {
        
        PersistentObject persistentObject = cachedObject.getPersistentObject();
        if (!isPersistentObjectDeleted(persistentObject)) {
          Object originalState = cachedObject.getPersistentObjectState();
          if (persistentObject.getPersistentState() != null && 
          		!persistentObject.getPersistentState().equals(originalState)) {
            updatedObjects.add(persistentObject);
          } else {
            log.trace("loaded object '{}' was not updated", persistentObject);
          }
        }
        
      }
      
    }
    return updatedObjects;
  }
  
  protected boolean isPersistentObjectDeleted(PersistentObject persistentObject) {
    for (DeleteOperation deleteOperation : deleteOperations) {
      if (deleteOperation.sameIdentity(persistentObject)) {
        return true;
      }
    }
    return false;
  }
  
  public <T extends PersistentObject> List<T> pruneDeletedEntities(List<T> listToPrune) {
    List<T> prunedList = new ArrayList<T>(listToPrune);
    for (T potentiallyDeleted : listToPrune) {
      for (DeleteOperation deleteOperation: deleteOperations) {
          
        if (deleteOperation.sameIdentity(potentiallyDeleted)) {
          prunedList.remove(potentiallyDeleted);
        }
          
      }
    }
    return prunedList;
  }

  protected void flushInserts() {
  	
  	// Handle in entity dependency order
    for (Class<? extends PersistentObject> persistentObjectClass : EntityDependencyOrder.INSERT_ORDER) {
      if (insertedObjects.containsKey(persistentObjectClass)) {
      	flushPersistentObjects(persistentObjectClass, insertedObjects.get(persistentObjectClass));
      	insertedObjects.remove(persistentObjectClass);
      }
    }
    
    // Next, in case of custom entities or we've screwed up and forgotten some entity
    if (insertedObjects.size() > 0) {
	    for (Class<? extends PersistentObject> persistentObjectClass : insertedObjects.keySet()) {
      	flushPersistentObjects(persistentObjectClass, insertedObjects.get(persistentObjectClass));
	    }
    }
    
    insertedObjects.clear();
  }

	protected void flushPersistentObjects(Class<? extends PersistentObject> persistentObjectClass, List<PersistentObject> persistentObjectsToInsert) {
	  if (persistentObjectsToInsert.size() == 1) {
	  	flushRegularInsert(persistentObjectsToInsert.get(0), persistentObjectClass);
	  } else if (Boolean.FALSE.equals(dbSqlSessionFactory.isBulkInsertable(persistentObjectClass))) {
	  	for (PersistentObject persistentObject : persistentObjectsToInsert) {
	  		flushRegularInsert(persistentObject, persistentObjectClass);
	  	}
	  }	else {
	  	flushBulkInsert(insertedObjects.get(persistentObjectClass), persistentObjectClass);
	  }
  }
  
  protected void flushRegularInsert(PersistentObject persistentObject, Class<? extends PersistentObject> clazz) {
  	 String insertStatement = dbSqlSessionFactory.getInsertStatement(persistentObject);
     insertStatement = dbSqlSessionFactory.mapStatement(insertStatement);

     if (insertStatement==null) {
       throw new ActivitiException("no insert statement for " + persistentObject.getClass() + " in the ibatis mapping files");
     }
     
     log.debug("inserting: {}", persistentObject);
     sqlSession.insert(insertStatement, persistentObject);
     
     // See https://activiti.atlassian.net/browse/ACT-1290
     if (persistentObject instanceof HasRevision) {
       ((HasRevision) persistentObject).setRevision(((HasRevision) persistentObject).getRevisionNext());
     }
  }

  protected void flushBulkInsert(List<PersistentObject> persistentObjectList, Class<? extends PersistentObject> clazz) {
    String insertStatement = dbSqlSessionFactory.getBulkInsertStatement(clazz);
    insertStatement = dbSqlSessionFactory.mapStatement(insertStatement);

    if (insertStatement==null) {
      throw new ActivitiException("no insert statement for " + persistentObjectList.get(0).getClass() + " in the ibatis mapping files");
    }

    if (persistentObjectList.size() <= dbSqlSessionFactory.getMaxNrOfStatementsInBulkInsert()) {
      sqlSession.insert(insertStatement, persistentObjectList);
    } else {
      
      for (int start = 0; start < persistentObjectList.size(); start += dbSqlSessionFactory.getMaxNrOfStatementsInBulkInsert()) {
        List<PersistentObject> subList = persistentObjectList.subList(start, 
            Math.min(start + dbSqlSessionFactory.getMaxNrOfStatementsInBulkInsert(), persistentObjectList.size()));
        sqlSession.insert(insertStatement, subList);
      }
      
    }

    if (persistentObjectList.get(0) instanceof HasRevision) {
      for (PersistentObject insertedObject: persistentObjectList) {
        ((HasRevision) insertedObject).setRevision(((HasRevision) insertedObject).getRevisionNext());
      }
    }
  }

  protected void flushUpdates(List<PersistentObject> updatedObjects) {
    for (PersistentObject updatedObject: updatedObjects) {
      String updateStatement = dbSqlSessionFactory.getUpdateStatement(updatedObject);
      updateStatement = dbSqlSessionFactory.mapStatement(updateStatement);
      
      if (updateStatement==null) {
        throw new ActivitiException("no update statement for "+updatedObject.getClass()+" in the ibatis mapping files");
      }
      
      log.debug("updating: {}", updatedObject);
      int updatedRecords = sqlSession.update(updateStatement, updatedObject);
      if (updatedRecords!=1) {
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
    flushRegularDeletes();
    deleteOperations.clear();
  }

  protected void flushRegularDeletes() {
  	for (DeleteOperation delete : deleteOperations) {
      log.debug("executing: {}", delete);
      delete.execute();
    }
  }

  @Override
  public void close() {
    sqlSession.close();
  }

  public void commit() {
    sqlSession.commit();
  }

  public void rollback() {
    sqlSession.rollback();
  }
  
  // schema operations ////////////////////////////////////////////////////////
  
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
      
      if (errorMessage!=null) {
        throw new ActivitiException("Activiti database problem: "+errorMessage);
      }
      
    } catch (Exception e) {
      if (isMissingTablesException(e)) {
        throw new ActivitiException("no activiti tables in db. set <property name=\"databaseSchemaUpdate\" to value=\"true\" or value=\"create-drop\" (use create-drop for testing only!) in bean processEngineConfiguration in activiti.cfg.xml for automatic schema creation", e);
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
    if (missingComponents==null) {
      return "Tables missing for component(s) "+component;
    }
    return missingComponents+", "+component;
  }

  protected String getDbVersion() {
    String selectSchemaVersionStatement = dbSqlSessionFactory.mapStatement("selectDbSchemaVersion");
    return (String) sqlSession.selectOne(selectSchemaVersionStatement);
  }

  public void dbSchemaCreate() {
    if (isEngineTablePresent()) {
      String dbVersion = getDbVersion();
      if (!ProcessEngine.VERSION.equals(dbVersion)) {
        throw new ActivitiWrongDbException(ProcessEngine.VERSION, dbVersion);
      }
    } else {
      dbSchemaCreateEngine();
    }

    if (dbSqlSessionFactory.isDbHistoryUsed()) {
      dbSchemaCreateHistory();
    }

    if (dbSqlSessionFactory.isDbIdentityUsed()) {
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

  public static String[] JDBC_METADATA_TABLE_TYPES = {"TABLE"};

	public String dbSchemaUpdate() {

		String feedback = null;
		boolean isUpgradeNeeded = false;
		int matchingVersionIndex = -1;

		if (isEngineTablePresent()) {

			PropertyEntity dbVersionProperty = selectById(PropertyEntity.class,"schema.version");
			String dbVersion = dbVersionProperty.getValue();

			// Determine index in the sequence of Activiti releases
			int index = 0;
			while (matchingVersionIndex < 0 && index < ACTIVITI_VERSIONS.size()) {
				if (ACTIVITI_VERSIONS.get(index).matches(dbVersion)) {
					matchingVersionIndex = index;
				} else {
					index++;
				}
			}

			// Exception when no match was found: unknown/unsupported version
			if (matchingVersionIndex < 0) {
				throw new ActivitiException(
				    "Could not update Activiti database schema: unknown version from database: '"
				        + dbVersion + "'");
			}

			isUpgradeNeeded = (matchingVersionIndex != (ACTIVITI_VERSIONS.size() - 1));

			if (isUpgradeNeeded) {
				dbVersionProperty.setValue(ProcessEngine.VERSION);

				PropertyEntity dbHistoryProperty;
				if ("5.0".equals(dbVersion)) {
					dbHistoryProperty = new PropertyEntity("schema.history", "create(5.0)");
					insert(dbHistoryProperty);
				} else {
					dbHistoryProperty = selectById(PropertyEntity.class, "schema.history");
				}

				// Set upgrade history
				String dbHistoryValue = dbHistoryProperty.getValue() + " upgrade(" + dbVersion + "->" + ProcessEngine.VERSION + ")";
				dbHistoryProperty.setValue(dbHistoryValue);

				// Engine upgrade
				dbSchemaUpgrade("engine", matchingVersionIndex);
				feedback = "upgraded Activiti from " + dbVersion + " to "+ ProcessEngine.VERSION;
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

  public boolean isEngineTablePresent(){
    return isTablePresent("ACT_RU_EXECUTION");
  }
  public boolean isHistoryTablePresent(){
    return isTablePresent("ACT_HI_PROCINST");
  }
  public boolean isIdentityTablePresent(){
    return isTablePresent("ACT_ID_USER");
  }

  public boolean isTablePresent(String tableName) {
  	// ACT-1610: in case the prefix IS the schema itself, we don't add the prefix, since the
  	// check is already aware of the schema
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
      throw new ActivitiException("couldn't check if tables are already present using metadata: "+e.getMessage(), e);
    }
  }
  
  protected boolean isUpgradeNeeded(String versionInDatabase) {
    if(ProcessEngine.VERSION.equals(versionInDatabase)) {
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
      
    if((dbMajorVersion > engineMajorVersion)
            || ( (dbMajorVersion <= engineMajorVersion) && (dbMinorVersion > engineMinorVersion) )) {
      throw new ActivitiException("Version of activiti database (" + versionInDatabase + ") is more recent than the engine (" + ProcessEngine.VERSION +")");
    } else if(cleanDbVersion.compareTo(cleanEngineVersion) == 0) {
      // Versions don't match exactly, possibly snapshot is being used
      log.warn("Engine-version is the same, but not an exact match: {} vs. {}. Not performing database-upgrade.", versionInDatabase, ProcessEngine.VERSION);
      return false;
    }
    return true;
  }
  
  protected String getCleanVersion(String versionString) {
    Matcher matcher = CLEAN_VERSION_REGEX.matcher(versionString);
    if(!matcher.find()) {
      throw new ActivitiException("Illegal format for version: " + versionString);
    }
    
    String cleanString = matcher.group();
    try {
      Double.parseDouble(cleanString); // try to parse it, to see if it is really a number
      return cleanString;
    } catch(NumberFormatException nfe) {
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
    for (int i=currentDatabaseVersionsIndex + 1; i<ACTIVITI_VERSIONS.size(); i++) {
    	String nextVersion = ACTIVITI_VERSIONS.get(i).getMainVersion();
    	
    	// Taking care of -SNAPSHOT version in development
      if (nextVersion.endsWith("-SNAPSHOT")) {
      	nextVersion = nextVersion.substring(0, nextVersion.length()-"-SNAPSHOT".length());
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
    return "org/activiti/db/" + directory + "/activiti." + databaseType + "." + operation + "."+component+".sql";
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
      String databaseType = dbSqlSessionFactory.getDatabaseType();
      
      // Special DDL handling for certain databases
      try {
	    	if ("mysql".equals(databaseType)) {
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
            throw new ActivitiException("database update java class '"+upgradestepClassName+"' can't be instantiated: "+e.getMessage(), e);
          }
          try {
            log.debug("executing upgrade step java class {}", upgradestepClassName);
            dbUpgradeStep.execute(this);
          } catch (Exception e) {
            throw new ActivitiException("error while executing database update java class '"+upgradestepClassName+"': "+e.getMessage(), e);
          }
          
        } else if (line.length()>0) {
          
          if ("oracle".equals(databaseType) && line.startsWith("begin")) {
            inOraclePlsqlBlock = true;
            sqlStatement = addSqlStatementPiece(sqlStatement, line);
            
          } else if ((line.endsWith(";") && inOraclePlsqlBlock == false) ||
              (line.startsWith("/") && inOraclePlsqlBlock == true)) {
            
            if (inOraclePlsqlBlock) {
              inOraclePlsqlBlock = false;
            } else {
              sqlStatement = addSqlStatementPiece(sqlStatement, line.substring(0, line.length()-1));
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
      throw new ActivitiException("couldn't "+operation+" db schema: "+exceptionSqlStatement, e);
    }
  }
  
  /**
   * MySQL is funny when it comes to timestamps and dates.
   *  
   * More specifically, for a DDL statement like 'MYCOLUMN timestamp(3)':
   *   - MySQL 5.6.4+ has support for timestamps/dates with millisecond (or smaller) precision. 
   *     The DDL above works and the data in the table will have millisecond precision
   *   - MySQL < 5.5.3 allows the DDL statement, but ignores it.
   *     The DDL above works but the data won't have millisecond precision
   *   - MySQL 5.5.3 < [version] < 5.6.4 gives and exception when using the DDL above.
   *   
   * Also, the 5.5 and 5.6 branches of MySQL are both actively developed and patched.
   * 
   * Hence, when doing auto-upgrade/creation of the Activiti tables, the default 
   * MySQL DDL file is used and all timestamps/datetimes are converted to not use the 
   * millisecond precision by string replacement done in the method below.
   * 
   * If using the DDL files directly (which is a sane choice in production env.),
   * there is a distinction between MySQL version < 5.6.
   */
  protected String updateDdlForMySqlVersionLowerThan56(String ddlStatements) {
	  return ddlStatements.replace("timestamp(3)", "timestamp")
			  			  .replace("datetime(3)", "datetime")
			  			  .replace("TIMESTAMP(3)", "TIMESTAMP")
			  			  .replace("DATETIME(3)", "DATETIME");
  }

  protected String addSqlStatementPiece(String sqlStatement, String line) {
    if (sqlStatement==null) {
      return line;
    }
    return sqlStatement + " \n" + line;
  }
  
  protected String readNextTrimmedLine(BufferedReader reader) throws IOException {
    String line = reader.readLine();
    if (line!=null) {
      line = line.trim();
    }
    return line;
  }
  
  protected boolean isMissingTablesException(Exception e) {
    String exceptionMessage = e.getMessage();
    if(e.getMessage() != null) {      
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
    if ( org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP.equals(databaseSchemaUpdate) 
         || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(databaseSchemaUpdate)
         || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE.equals(databaseSchemaUpdate)
       ) {
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

  // query factory methods ////////////////////////////////////////////////////  

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

  // getters and setters //////////////////////////////////////////////////////
  
  public SqlSession getSqlSession() {
    return sqlSession;
  }
  public DbSqlSessionFactory getDbSqlSessionFactory() {
    return dbSqlSessionFactory;
  }

}
