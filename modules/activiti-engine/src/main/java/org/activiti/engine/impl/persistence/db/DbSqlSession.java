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

package org.activiti.engine.impl.persistence.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.PersistentObject;
import org.activiti.engine.impl.util.ClassNameUtil;
import org.activiti.engine.impl.variable.DeserializedObject;
import org.activiti.engine.impl.variable.VariableInstance;
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
    long nextDbid = dbSqlSessionFactory.getIdGenerator().getNextId();
    String id = Long.toString(nextDbid);
    persistentObject.setId(id);
    insertedObjects.add(persistentObject);
    deletedObjects.remove(persistentObject);
    cachePut(persistentObject);
  }
  
  // delete ///////////////////////////////////////////////////////////////////
  
  public void delete(PersistentObject persistentObject) {
    deletedObjects.add(new DeleteById(persistentObject.getClass(), persistentObject.getId()));
    insertedObjects.remove(persistentObject);
  }
  
  public void delete(Class<?> clazz, String id) {
    deletedObjects.add(new DeleteById(clazz, id));
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
      log.fine("deleting: "+persistenceObjectClass+"["+persistentObjectId+"]");
      sqlSession.delete(deleteStatement, persistentObjectId);
    }
    public String toString() {
      return "delete "+persistenceObjectClass.getName()+"["+persistentObjectId+"]";
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

  public Object selectOne(String statement, Object parameter) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    PersistentObject loadedObject = (PersistentObject) sqlSession.selectOne(statement, parameter);
    return cacheGet(loadedObject);
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

  protected CachedObject cachePut(PersistentObject persistentObject) {
    Map<String, CachedObject> classCache = cachedObjects.get(persistentObject.getClass());
    if (classCache==null) {
      classCache = new HashMap<String, CachedObject>();
      cachedObjects.put(persistentObject.getClass(), classCache);
    }
    CachedObject cachedObject = new CachedObject(persistentObject);
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
      cachedObject = cachePut(persistentObject);
    }
    return cachedObject.getPersistentObject();
  }
  
  public static class CachedObject {
    protected PersistentObject persistentObject;
    protected Object persistentObjectState;
    
    public CachedObject(PersistentObject persistentObject) {
      this.persistentObject = persistentObject;
      this.persistentObjectState = persistentObject.getPersistentState();
    }

    public PersistentObject getPersistentObject() {
      return persistentObject;
    }

    public Object getPersistentObjectState() {
      return persistentObjectState;
    }
  }

  // deserialized objects /////////////////////////////////////////////////////
  
  public void addDeserializedObject(Object deserializedObject, byte[] serializedBytes, VariableInstance variableInstance) {
    deserializedObjects.add(new DeserializedObject(deserializedObject, serializedBytes, variableInstance));
  }

  // flush ////////////////////////////////////////////////////////////////////

  public void flush() {
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
      sqlSession.update(updateStatement, updatedObject);
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
}
