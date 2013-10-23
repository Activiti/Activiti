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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;


/**
 * @author Tom Baeyens
 */
public class CommentEntityManager extends AbstractManager {
  
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
    return getDbSqlSession().selectList("selectCommentsByTaskId", taskId);
  }
  
  @SuppressWarnings("unchecked")
  public List<Comment> findCommentsByTaskIdAndType(String taskId, String type) {
    checkHistoryEnabled();
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskId", taskId);
    params.put("type", type);
    return getDbSqlSession().selectListWithRawParameter("selectCommentsByTaskIdAndType", params, 0, Integer.MAX_VALUE);
  }
  
  @SuppressWarnings("unchecked")
  public List<Comment> findCommentsByType(String type) {
    checkHistoryEnabled();
    return getDbSqlSession().selectList("selectCommentsByType", type);
  }

  @SuppressWarnings("unchecked")
  public List<Event> findEventsByTaskId(String taskId) {
    checkHistoryEnabled();
    return getDbSqlSession().selectList("selectEventsByTaskId", taskId);
  }

  public void deleteCommentsByTaskId(String taskId) {
    checkHistoryEnabled();
    getDbSqlSession().delete("deleteCommentsByTaskId", taskId);
  }

  @SuppressWarnings("unchecked")
  public List<Comment> findCommentsByProcessInstanceId(String processInstanceId) {
    checkHistoryEnabled();
    return getDbSqlSession().selectList("selectCommentsByProcessInstanceId", processInstanceId);
  }
  
  public Comment findComment(String commentId) {
    return getDbSqlSession().selectById(CommentEntity.class, commentId);
  }
  
  public Event findEvent(String commentId) {
    return getDbSqlSession().selectById(CommentEntity.class, commentId);
  }
  
  protected void checkHistoryEnabled() {
    if(!getHistoryManager().isHistoryEnabled()) {
      throw new ActivitiException("In order to use comments, history should be enabled");
    }
  }
}
