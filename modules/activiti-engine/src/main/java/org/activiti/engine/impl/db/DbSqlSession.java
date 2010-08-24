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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.Page;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.runtime.VariableInstanceEntity;
import org.activiti.engine.impl.variable.DeserializedObject;
import org.activiti.pvm.impl.util.ClassNameUtil;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;


/** responsibilities:
 *   - delayed flushing of inserts updates and deletes
 *   - optional dirty checking
 *   - db specific statement name mapping
 *   
 * @author Tom Baeyens
 */
public class DbSqlSession implements Session {
  
  private static Logger log = Logger.getLogger(DbSqlSession.class.getName());

  protected SqlSession sqlSession;
  protected DbSqlSessionFactory dbSqlSessionFactory;
  protected List<PersistentObject> insertedObjects = new ArrayList<PersistentObject>();
  protected Map<Class<?>, Map<String, CachedObject>> cachedObjects = new HashMap<Class<?>, Map<String,CachedObject>>();
  protected List<DeleteOperation> deletedObjects = new ArrayList<DeleteOperation>();
  protected List<DeserializedObject> deserializedObjects = new ArrayList<DeserializedObject>();

  public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    this.sqlSession = dbSqlSessionFactory
      .getSqlSessionFactory()
      .openSession();
  }

  // insert ///////////////////////////////////////////////////////////////////
  
  public void insert(PersistentObject persistentObject) {
    if (persistentObject.getId()==null) {
      long nextDbid = dbSqlSessionFactory.getIdGenerator().getNextId();
      String id = Long.toString(nextDbid);
      persistentObject.setId(id);
    }
    insertedObjects.add(persistentObject);
    cachePut(persistentObject, false);
  }
  
  // delete ///////////////////////////////////////////////////////////////////
  
  public void delete(Class<?> persistentObjectClass, String persistentObjectId) {
    for (DeleteOperation deleteOperation: deletedObjects) {
      if (deleteOperation instanceof DeleteById) {
        DeleteById deleteById = (DeleteById) deleteOperation;
        if ( persistentObjectClass.equals(deleteById.persistenceObjectClass)
             && persistentObjectId.equals(deleteById.persistentObjectId)
           ) {
          // skip this delete
          return;
        }
      }
    }
    deletedObjects.add(new DeleteById(persistentObjectClass, persistentObjectId));
  }
  
  public interface DeleteOperation {
    void execute();
  }

  public class DeleteById implements DeleteOperation {
    Class<?> persistenceObjectClass;
    String persistentObjectId;
    public DeleteById(Class< ? > clazz, String id) {
      this.persistenceObjectClass = clazz;
      this.persistentObjectId = id;
    }
    public void execute() {
      String deleteStatement = dbSqlSessionFactory.getDeleteStatement(persistenceObjectClass);
      deleteStatement = dbSqlSessionFactory.mapStatement(deleteStatement);
      if (deleteStatement==null) {
        throw new ActivitiException("no delete statement for "+persistenceObjectClass+" in the ibatis mapping files");
      }
      log.fine("deleting: "+ClassNameUtil.getClassNameWithoutPackage(persistenceObjectClass)+"["+persistentObjectId+"]");
      sqlSession.delete(deleteStatement, persistentObjectId);
    }
    public String toString() {
      return "delete "+ClassNameUtil.getClassNameWithoutPackage(persistenceObjectClass)+"["+persistentObjectId+"]";
    }
  }
  
  public void delete(String statement, Object parameter) {
    deletedObjects.add(new DeleteBulk(statement, parameter));
  }
  
  public class DeleteBulk implements DeleteOperation {
    String statement;
    Object parameter;
    public DeleteBulk(String statement, Object parameter) {
      this.statement = dbSqlSessionFactory.mapStatement(statement);
      this.parameter = parameter;
    }
    public void execute() {
      sqlSession.delete(statement, parameter);
    }
    public String toString() {
      return "bulk delete: "+statement;
    }
  }
  
  // select ///////////////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public List selectList(String statement) {
    return selectList(statement, null);
  }

  @SuppressWarnings("unchecked")
  public List selectList(String statement, Object parameter) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    List loadedObjects = sqlSession.selectList(statement, parameter);
    return filterLoadedObjects(loadedObjects);
  }
  
  @SuppressWarnings("unchecked")
  public List selectList(String statement, Object parameter, Page page) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    List loadedObjects;
    if (page!=null) {
      loadedObjects = sqlSession.selectList(statement, parameter, new RowBounds(page.getFirstResult(), page.getMaxResults()));
    } else {
      loadedObjects = sqlSession.selectList(statement, parameter);
    }
    return filterLoadedObjects(loadedObjects);
  }


  public Object selectOne(String statement, Object parameter) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    Object result = sqlSession.selectOne(statement, parameter);
    if (result instanceof PersistentObject) {
      PersistentObject loadedObject = (PersistentObject) result;
      result = cacheGet(loadedObject);
    }
    return result;
  }

  // internal session cache ///////////////////////////////////////////////////
  
  @SuppressWarnings("unchecked")
  protected List filterLoadedObjects(List<PersistentObject> loadedObjects) {
    List<PersistentObject> filteredObjects = new ArrayList<PersistentObject>(loadedObjects.size());
    for (PersistentObject loadedObject: loadedObjects) {
      PersistentObject cachedPersistentObject = cacheGet(loadedObject);
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
  protected PersistentObject cacheGet(PersistentObject persistentObject) {
    CachedObject cachedObject = null;
    Map<String, CachedObject> classCache = cachedObjects.get(persistentObject.getClass());
    if (classCache!=null) {
      cachedObject = classCache.get(persistentObject.getId());
    }
    if (cachedObject==null) {
      cachedObject = cachePut(persistentObject, true);
    }
    return cachedObject.getPersistentObject();
  }
  
  protected void cacheRemove(Class<?> persistentObjectClass, String persistentObjectId) {
    Map<String, CachedObject> classCache = cachedObjects.get(persistentObjectClass);
    if (classCache==null) {
      return;
    }
    classCache.remove(persistentObjectId);
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
  
  public void addDeserializedObject(Object deserializedObject, byte[] serializedBytes, VariableInstanceEntity variableInstanceEntity) {
    deserializedObjects.add(new DeserializedObject(deserializedObject, serializedBytes, variableInstanceEntity));
  }

  // flush ////////////////////////////////////////////////////////////////////

  public void flush() {
    removeUnnecessaryOperations();
    flushDeserializedObjects();
    List<PersistentObject> updatedObjects = getUpdatedObjects();
    
    if (log.isLoggable(Level.FINE)) {
      log.fine("flush summary:");
      for (PersistentObject insertedObject: insertedObjects) {
        log.fine("  insert "+toString(insertedObject));
      }
      for (PersistentObject updatedObject: updatedObjects) {
        log.fine("  update "+toString(updatedObject));
      }
      for (Object deleteOperation: deletedObjects) {
        log.fine("  "+deleteOperation);
      }
      log.fine("now executing flush...");
    }

    flushInserts();
    flushUpdates(updatedObjects);
    flushDeletes();
  }

  protected void removeUnnecessaryOperations() {
    List<DeleteOperation> deletedObjectsCopy = new ArrayList<DeleteOperation>(deletedObjects);
    // for all deleted objects
    for (DeleteOperation deleteOperation: deletedObjectsCopy) {
      if (deleteOperation instanceof DeleteById) {
        DeleteById deleteById = (DeleteById) deleteOperation;
        PersistentObject insertedObject = findInsertedObject(deleteById.persistenceObjectClass, deleteById.persistentObjectId);
        // if the deleted object is inserted,
        if (insertedObject!=null) {
          // remove the insert and the delete
          insertedObjects.remove(insertedObject);
          deletedObjects.remove(deleteOperation);
        }
        // in any case, remove the deleted object from the cache
        cacheRemove(deleteById.persistenceObjectClass, deleteById.persistentObjectId);
      }
    }
    for (PersistentObject insertedObject: insertedObjects) {
      cacheRemove(insertedObject.getClass(), insertedObject.getId());
    }
  }

  protected PersistentObject findInsertedObject(Class< ? > persistenceObjectClass, String persistentObjectId) {
    for (PersistentObject insertedObject: insertedObjects) {
      if ( insertedObject.getClass().equals(persistenceObjectClass)
           && insertedObject.getId().equals(persistentObjectId)
         ) {
        return insertedObject;
      }
    }
    return null;
  }

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
        PersistentObject persistentObject = (PersistentObject) cachedObject.getPersistentObject();
        if (!deletedObjects.contains(persistentObject)) {
          Object originalState = cachedObject.getPersistentObjectState();
          if (!originalState.equals(persistentObject.getPersistentState())) {
            updatedObjects.add(persistentObject);
          } else {
            log.finest("loaded object '"+persistentObject+"' was not updated");
          }
        }
      }
    }
    return updatedObjects;
  }

  protected void flushInserts() {
    for (PersistentObject insertedObject: insertedObjects) {
      String insertStatement = dbSqlSessionFactory.getInsertStatement(insertedObject);
      insertStatement = dbSqlSessionFactory.mapStatement(insertStatement);

      if (insertStatement==null) {
        throw new ActivitiException("no insert statement for "+insertedObject.getClass()+" in the ibatis mapping files");
      }
      
      log.fine("inserting: "+toString(insertedObject));
      sqlSession.insert(insertStatement, insertedObject);
    }
    insertedObjects.clear();
  }

  protected void flushUpdates(List<PersistentObject> updatedObjects) {
    for (PersistentObject updatedObject: updatedObjects) {
      String updateStatement = dbSqlSessionFactory.getUpdateStatement(updatedObject);
      updateStatement = dbSqlSessionFactory.mapStatement(updateStatement);
      if (updateStatement==null) {
        throw new ActivitiException("no update statement for "+updatedObject.getClass()+" in the ibatis mapping files");
      }
      log.fine("updating: "+toString(updatedObject)+"]");
      int updatedRecords = sqlSession.update(updateStatement, updatedObject);
      if (updatedRecords!=1) {
        throw new OptimisticLockingException(toString(updatedObject)+" was updated by another transaction concurrently");
      }
    }
    updatedObjects.clear();
  }

  protected void flushDeletes() {
    for (DeleteOperation delete: deletedObjects) {
      log.fine("executing: "+delete);
      delete.execute();
    }
    deletedObjects.clear();
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

  protected String toString(PersistentObject persistentObject) {
    if (persistentObject==null) {
      return "null";
    }
    return ClassNameUtil.getClassNameWithoutPackage(persistentObject)+"["+persistentObject.getId()+"]";
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public SqlSession getSqlSession() {
    return sqlSession;
  }
  public DbSqlSessionFactory getDbSqlSessionFactory() {
    return dbSqlSessionFactory;
  }

}
