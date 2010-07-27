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

import org.activiti.ActivitiException;
import org.activiti.impl.persistence.PersistentObject;
import org.activiti.impl.tx.Session;
import org.activiti.impl.variable.DeserializedObject;
import org.activiti.impl.variable.VariableInstance;
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
  protected List<PersistentObject> deletedObjects = new ArrayList<PersistentObject>();
  protected List<DeserializedObject> deserializedObjects = new ArrayList<DeserializedObject>();

  public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    this.sqlSession = dbSqlSessionFactory
      .getSqlSessionFactory()
      .openSession();
  }

  // insert ///////////////////////////////////////////////////////////////////
  
  public void insert(PersistentObject persistentObject) {
    long nextDbid = dbSqlSessionFactory.getIdGenerator().getNextDbid();
    String id = Long.toString(nextDbid);
    persistentObject.setId(id);
    cachePut(persistentObject);
  }
  
  // select ///////////////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public List selectList(String statement, Object parameter) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    List loadedObjects = sqlSession.selectList(statement, parameter);
    return filterLoadedObjects(loadedObjects);
  }

  // delete ///////////////////////////////////////////////////////////////////
  
  public void delete(PersistentObject persistentObject) {
    deletedObjects.add(persistentObject);
  }

  // internal session cache ///////////////////////////////////////////////////
  
  @SuppressWarnings("unchecked")
  protected List filterLoadedObjects(List<PersistentObject> loadedObjects) {
    List<PersistentObject> filteredObjects = new ArrayList<PersistentObject>(loadedObjects.size());
    for (PersistentObject loadedObject: loadedObjects) {
      PersistentObject cachedObject = cacheGet(loadedObject);
      filteredObjects.add(cachedObject);
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
      log.fine("flushing...");
      for (PersistentObject insertedObject: insertedObjects) {
        log.fine("  insert "+toString(insertedObject));
      }
      for (PersistentObject updatedObject: updatedObjects) {
        log.fine("  update "+toString(updatedObject));
      }
      for (PersistentObject deletedObject: deletedObjects) {
        log.fine("  delete "+toString(deletedObject));
      }
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
        Object originalState = cachedObject.getPersistentObjectState();
        if (!originalState.equals(persistentObject.getPersistentState())) {
          updatedObjects.add(persistentObject);
        } else {
          log.finest("loaded object '"+persistentObject+"' was not updated");
        }
      }
    }
    return updatedObjects;
  }

  protected void flushInserts() {
    for (PersistentObject insertedObject: insertedObjects) {
      Class<?> insertObjectClass = insertedObject.getClass();
      String insertStatement = dbSqlSessionFactory
        .getInsertStatements()
        .get(insertObjectClass);
      insertStatement = dbSqlSessionFactory.mapStatement(insertStatement);

      if (insertStatement==null) {
        throw new ActivitiException("no insert statement id for "+insertedObject.getClass());
      }
      
      log.fine("inserting: "+toString(insertedObject));
      sqlSession.insert(insertStatement, insertedObject);
    }
    insertedObjects.clear();
  }

  protected void flushUpdates(List<PersistentObject> updatedObjects) {
    for (PersistentObject updatedObject: updatedObjects) {
      Class<?> updatedObjectClass = updatedObject.getClass();
      String updateStatement = dbSqlSessionFactory
        .getUpdateStatements()
        .get(updatedObjectClass);
      updateStatement = dbSqlSessionFactory.mapStatement(updateStatement);
      if (updateStatement==null) {
        throw new ActivitiException("no update statement id for "+updatedObject.getClass());
      }
      log.fine("updating: "+toString(updatedObject)+"]");
      sqlSession.update(updateStatement, updatedObject);
    }
    updatedObjects.clear();
  }

  protected void flushDeletes() {
    for (PersistentObject deletedObject: deletedObjects) {
      Class<?> deletedObjectClass = deletedObject.getClass();
      String deleteStatement = dbSqlSessionFactory
        .getDeleteStatements()
        .get(deletedObjectClass);
      deleteStatement = dbSqlSessionFactory.mapStatement(deleteStatement);
      if (deleteStatement==null) {
        throw new ActivitiException("no delete statement id for "+deletedObject.getClass());
      }
      log.fine("deleting: "+toString(deletedObject));
      sqlSession.delete(deleteStatement, deletedObject);
    }
    deletedObjects.clear();
  }

  public void close() {
    sqlSession.close();
  }

  protected String toString(PersistentObject persistentObject) {
    if (persistentObject==null) {
      return "null";
    }
    return persistentObject.getClass().getName()+"["+persistentObject.getId()+"]";
  }
}
