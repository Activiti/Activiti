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
public class Inserted {

  private static Logger log = Logger.getLogger(Inserted.class.getName());
  
  protected static Map<Class<?>,String> insertStatementIds = new HashMap<Class<?>, String>();
  static {
    insertStatementIds.put(ExecutionDbImpl.class, "org.activiti.persistence.insertExecution");
    insertStatementIds.put(TaskImpl.class, "org.activiti.persistence.insertTask");
    insertStatementIds.put(TaskInvolvement.class, "org.activiti.persistence.insertTaskInvolvement");
  }
  
  List<PersistentObject> insertedObjects = new ArrayList<PersistentObject>();

  public void add(PersistentObject persistentObject) {
    insertedObjects.add(persistentObject);
  }

  public void remove(PersistentObject persistentObject) {
    insertedObjects.remove(persistentObject);
  }

  public List<PersistentObject> getInsertedObjects() {
    return insertedObjects;
  }
  
  public void flush(SqlSession sqlSession) {
    for (PersistentObject insertedObject: insertedObjects) {
      Class<?> insertedObjectClass = insertedObject.getClass();
      String insertStatementId = insertStatementIds.get(insertedObjectClass);
      if (insertStatementId==null) {
        throw new ActivitiException("no insert statement id for "+insertedObject.getClass());
      }
      log.fine("inserting: "+insertedObjectClass+"["+insertedObject.getId()+"]");
      sqlSession.insert(insertStatementId, insertedObject);
    }
  }

  public int size() {
    return insertedObjects.size();
  }
}
