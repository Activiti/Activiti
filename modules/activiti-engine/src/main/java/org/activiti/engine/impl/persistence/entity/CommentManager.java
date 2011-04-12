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

package org.activiti.engine.impl.persistence.entity;

import java.util.List;

import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.persistence.AbstractHistoricManager;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;


/**
 * @author Tom Baeyens
 */
public class CommentManager extends AbstractHistoricManager {
  
  public void delete(PersistentObject persistentObject) {
    checkHistoryEnabled();
    super.delete(persistentObject);
  }

  public void insert(PersistentObject persistentObject) {
    checkHistoryEnabled();
    super.insert(persistentObject);
  }

  @SuppressWarnings("unchecked")
  public List<Comment> findCommentsByTaskId(String taskId) {
    checkHistoryEnabled();
    return getPersistenceSession().selectList("selectCommentsByTaskId", taskId);
  }

  @SuppressWarnings("unchecked")
  public List<Event> findEventsByTaskId(String taskId) {
    checkHistoryEnabled();
    return getPersistenceSession().selectList("selectEventsByTaskId", taskId);
  }

  public void deleteCommentsByTaskId(String taskId) {
    checkHistoryEnabled();
    getPersistenceSession().delete("deleteCommentsByTaskId", taskId);
  }

  @SuppressWarnings("unchecked")
  public List<Comment> findCommentsByProcessInstanceId(String processInstanceId) {
    checkHistoryEnabled();
    return getPersistenceSession().selectList("selectCommentsByProcessInstanceId", processInstanceId);
  }

}
