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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.impl.bytes.ByteArrayImpl;
import org.activiti.impl.db.execution.DbExecutionImpl;
import org.activiti.impl.task.TaskImpl;
import org.activiti.impl.task.TaskInvolvement;
import org.activiti.impl.variable.VariableInstance;
import org.apache.ibatis.session.SqlSession;


/**
 * @author Tom Baeyens
 */
public class Deleted {

  private static Logger log = Logger.getLogger(Deleted.class.getName());
  
  protected static Map<Class<?>,String> deleteStatementIds = new HashMap<Class<?>, String>();
  static {
    deleteStatementIds.put(DbExecutionImpl.class, "org.activiti.persistence.deleteExecution");
    deleteStatementIds.put(TaskImpl.class, "org.activiti.persistence.deleteTask");
    deleteStatementIds.put(TaskInvolvement.class, "org.activiti.persistence.deleteTaskInvolvement");
    deleteStatementIds.put(VariableInstance.class, "org.activiti.persistence.deleteVariableInstance");
    deleteStatementIds.put(ByteArrayImpl.class, "org.activiti.persistence.deleteByteArray");
  }
  
  protected List<PersistentObject> deletedObjects = new ArrayList<PersistentObject>();
  protected Set<PersistentObjectId> deletedObjectIds = new HashSet<PersistentObjectId>();

  public void add(PersistentObject persistentObject) {
    PersistentObjectId deletedObjectId = new PersistentObjectId(persistentObject);
    if (!deletedObjectIds.contains(deletedObjectId)) {
      deletedObjects.add(persistentObject);
      deletedObjectIds.add(deletedObjectId);
    }
  }

  public int size() {
    return deletedObjects.size();
  }

  public List<PersistentObject> getDeletedObjects() {
    return deletedObjects;
  }

  public void flush(SqlSession sqlSession) {
    for (PersistentObject deletedObject: deletedObjects) {
      Class<?> deletedObjectClass = deletedObject.getClass();
      String deleteStatementId = findDeleteStatementId(deletedObjectClass);
      if (deleteStatementId==null) {
        throw new ActivitiException("no delete statement id for "+deletedObject.getClass());
      }
      log.fine("deleting: "+deletedObjectClass+"["+deletedObject.getId()+"]");
      sqlSession.delete(deleteStatementId, deletedObject);
    }
    deletedObjects.clear();
    deletedObjectIds.clear();
  }

  protected String findDeleteStatementId(Class< ? > deletedObjectClass) {
    if (deletedObjectClass==null) {
      return null;
    }
    String deleteStatementId = deleteStatementIds.get(deletedObjectClass);
    if (deleteStatementId==null) { 
      return findDeleteStatementId(deletedObjectClass.getSuperclass());
    }
    return deleteStatementId;
  }
}
