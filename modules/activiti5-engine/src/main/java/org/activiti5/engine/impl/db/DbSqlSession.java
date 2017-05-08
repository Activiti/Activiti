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

package org.activiti5.engine.impl.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiVariableEvent;
import org.activiti5.engine.ActivitiException;
import org.activiti5.engine.ActivitiOptimisticLockingException;
import org.activiti5.engine.ActivitiWrongDbException;
import org.activiti5.engine.ProcessEngine;
import org.activiti5.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti5.engine.impl.DeploymentQueryImpl;
import org.activiti5.engine.impl.ExecutionQueryImpl;
import org.activiti5.engine.impl.GroupQueryImpl;
import org.activiti5.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti5.engine.impl.HistoricDetailQueryImpl;
import org.activiti5.engine.impl.HistoricProcessInstanceQueryImpl;
import org.activiti5.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti5.engine.impl.HistoricVariableInstanceQueryImpl;
import org.activiti5.engine.impl.JobQueryImpl;
import org.activiti5.engine.impl.ModelQueryImpl;
import org.activiti5.engine.impl.Page;
import org.activiti5.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti5.engine.impl.ProcessInstanceQueryImpl;
import org.activiti5.engine.impl.TaskQueryImpl;
import org.activiti5.engine.impl.UserQueryImpl;
import org.activiti5.engine.impl.context.Context;
import org.activiti5.engine.impl.interceptor.Session;
import org.activiti5.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti5.engine.impl.variable.DeserializedObject;
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
	  insertedObjects.remove(persistentObjectClass);
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
       HasRevision revisionEntity = (HasRevision) persistentObject;
       if (revisionEntity.getRevision() == 0) {
         revisionEntity.setRevision(revisionEntity.getRevisionNext());
       }
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
        HasRevision revisionEntity = (HasRevision) insertedObject;
        if (revisionEntity.getRevision() == 0) {
          revisionEntity.setRevision(revisionEntity.getRevisionNext());
        }
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
    boolean dispatchEvent = false;
    ActivitiEventDispatcher eventDispatcher = Context.getProcessEngineConfiguration().getEventDispatcher();
    if (eventDispatcher != null && eventDispatcher.isEnabled()) {
      dispatchEvent = eventDispatcher.isEnabled();
    }

    flushRegularDeletes(dispatchEvent);

    if (dispatchEvent) {
      dispatchEventsForRemovedOperations(removedOperations);
    }

    deleteOperations.clear();
  }

  protected void dispatchEventsForRemovedOperations(List<DeleteOperation> removedOperations) {
    for (DeleteOperation delete : removedOperations) {
      // dispatch removed delete events
      if (delete instanceof CheckedDeleteOperation) {
        CheckedDeleteOperation checkedDeleteOperation = (CheckedDeleteOperation) delete;
        PersistentObject persistentObject = checkedDeleteOperation.getPersistentObject();
        if (persistentObject instanceof VariableInstanceEntity) {
          VariableInstanceEntity variableInstance = (VariableInstanceEntity) persistentObject;
          Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
            createVariableDeleteEvent(variableInstance)
          );
        }
      }
    }
  }

  protected static ActivitiVariableEvent createVariableDeleteEvent(VariableInstanceEntity variableInstance) {
    return ActivitiEventBuilder.createVariableEvent(ActivitiEventType.VARIABLE_DELETED, variableInstance.getName(), null, variableInstance.getType(),
    		variableInstance.getTaskId(), variableInstance.getExecutionId(), variableInstance.getProcessInstanceId(), null);
  }

  protected void flushRegularDeletes(boolean dispatchEvent) {
  	for (DeleteOperation delete : deleteOperations) {
      log.debug("executing: {}", delete);

      delete.execute();

      //  fire event for variable delete operation. (BulkDeleteOperation is not taken into account)
      if (dispatchEvent) {
        //  prepare delete event to fire for variable delete operation. (BulkDeleteOperation is not taken into account)
        if (delete instanceof CheckedDeleteOperation) {
          CheckedDeleteOperation checkedDeleteOperation = (CheckedDeleteOperation) delete;
          PersistentObject persistentObject = checkedDeleteOperation.getPersistentObject();
          if (persistentObject instanceof VariableInstanceEntity) {
            VariableInstanceEntity variableInstance = (VariableInstanceEntity) persistentObject;
            Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
              createVariableDeleteEvent(variableInstance)
            );
          }
        } else if (delete instanceof BulkCheckedDeleteOperation) {
        	BulkCheckedDeleteOperation bulkCheckedDeleteOperation = (BulkCheckedDeleteOperation) delete;
        	if (VariableInstanceEntity.class.isAssignableFrom(bulkCheckedDeleteOperation.getPersistentObjectClass())) {
        		for (PersistentObject persistentObject : bulkCheckedDeleteOperation.getPersistentObjects()) {
        			 VariableInstanceEntity variableInstance = (VariableInstanceEntity) persistentObject;
               Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
                 createVariableDeleteEvent(variableInstance)
               );
        		}
        	}
        }
      }
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

  public static String[] JDBC_METADATA_TABLE_TYPES = {"TABLE"};

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
  
  public String getResourceForDbOperation(String directory, String operation, String component) {
    String databaseType = dbSqlSessionFactory.getDatabaseType();
    return "org/activiti/db/" + directory + "/activiti." + databaseType + "." + operation + "."+component+".sql";
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
