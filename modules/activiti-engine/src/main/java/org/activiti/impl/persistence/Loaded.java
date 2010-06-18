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
package org.activiti.impl.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.impl.execution.ExecutionDbImpl;
import org.activiti.impl.task.TaskInvolvement;
import org.activiti.impl.task.TaskImpl;
import org.apache.ibatis.session.SqlSession;


/**
 * @author Tom Baeyens
 */
public class Loaded {
  
  private static Logger log = Logger.getLogger(Loaded.class.getName());
  
  protected static Map<Class<?>,String> updateStatementIds = new HashMap<Class<?>, String>();
  static {
    updateStatementIds.put(ExecutionDbImpl.class, "org.activiti.persistence.updateExecution");
    updateStatementIds.put(TaskImpl.class, "org.activiti.persistence.updateTask");
    updateStatementIds.put(TaskInvolvement.class, "org.activiti.persistence.updateTaskInvolvement");
  }
  
  protected Map<Object, LoadedObject> loadedObjects = new HashMap<Object, LoadedObject>(); 
  
  public PersistentObject add(PersistentObject persistentObject) {
    Object key = getKey(persistentObject);
    LoadedObject loadedObject = loadedObjects.get(key);
    if (loadedObject!=null) {
      return loadedObject.getPersistentObject();
    }
    loadedObject = new LoadedObject(persistentObject);
    loadedObjects.put(key, loadedObject);
    return persistentObject;
  }
  
  public List add(List persistentObjects) {
    List<PersistentObject> loadedPersistentObjects = new ArrayList<PersistentObject>();
    for (Object persistentObject: persistentObjects) {
      PersistentObject loadedPersistentObject = add((PersistentObject) persistentObject);
      loadedPersistentObjects.add(loadedPersistentObject);
    }
    return loadedPersistentObjects;

  }

  public void remove(PersistentObject persistentObject) {
    Object key = getKey(persistentObject);
    loadedObjects.remove(key);
  }

  protected Object getKey(PersistentObject persistentObject) {
    List<Object> key = new ArrayList<Object>();
    key.add(persistentObject.getClass());
    key.add(persistentObject.getId());
    return key;
  }

  public void flush(SqlSession sqlSession, List<PersistentObject> updatedObjects) {
    for (PersistentObject updatedObject: updatedObjects) {
      Class<?> updatedObjectClass = updatedObject.getClass();
      String updateStatementId = updateStatementIds.get(updatedObjectClass);
      if (updateStatementId==null) {
        throw new ActivitiException("no update statement id for "+updatedObject.getClass());
      }
      log.fine("updating: "+updatedObjectClass+"["+updatedObject.getId()+"]");
      sqlSession.update(updateStatementId, updatedObject);
    }
  }

  public List<PersistentObject> getUpdatedObjects() {
    List<PersistentObject> updatedObjects = new ArrayList<PersistentObject>();
    for (LoadedObject loadedObject: loadedObjects.values()) {
      PersistentObject persistentObject = (PersistentObject) loadedObject.getPersistentObject();
      Object originalState = loadedObject.getPersistentObjectState();
      if (!originalState.equals(persistentObject.getPersistentState())) {
        updatedObjects.add(persistentObject);
      } else {
        log.finest("persistent object '"+persistentObject+"' was not updated");
      }
    }
    return updatedObjects;
  }
}
