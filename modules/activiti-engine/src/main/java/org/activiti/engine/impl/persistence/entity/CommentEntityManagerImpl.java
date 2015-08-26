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
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class CommentEntityManagerImpl extends AbstractEntityManager<CommentEntity> implements CommentEntityManager {

  @Override
  public void insert(CommentEntity commentEntity) {
    checkHistoryEnabled();
    super.insert(commentEntity, false);

    Comment comment = (Comment) commentEntity;
    if (getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      // Forced to fetch the process-instance to associate the right
      // process definition
      String processDefinitionId = null;
      String processInstanceId = comment.getProcessInstanceId();
      if (comment.getProcessInstanceId() != null) {
        ExecutionEntity process = getProcessInstanceManager().findExecutionById(comment.getProcessInstanceId());
        if (process != null) {
          processDefinitionId = process.getProcessDefinitionId();
        }
      }
      getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, commentEntity, processInstanceId, processInstanceId, processDefinitionId));
      getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, commentEntity, processInstanceId, processInstanceId, processDefinitionId));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Comment> findCommentsByTaskId(String taskId) {
    checkHistoryEnabled();
    return getDbSqlSession().selectList("selectCommentsByTaskId", taskId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Comment> findCommentsByTaskIdAndType(String taskId, String type) {
    checkHistoryEnabled();
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskId", taskId);
    params.put("type", type);
    return getDbSqlSession().selectListWithRawParameter("selectCommentsByTaskIdAndType", params, 0, Integer.MAX_VALUE);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Comment> findCommentsByType(String type) {
    checkHistoryEnabled();
    return getDbSqlSession().selectList("selectCommentsByType", type);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Event> findEventsByTaskId(String taskId) {
    checkHistoryEnabled();
    return getDbSqlSession().selectList("selectEventsByTaskId", taskId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Event> findEventsByProcessInstanceId(String processInstanceId) {
    checkHistoryEnabled();
    return getDbSqlSession().selectList("selectEventsByProcessInstanceId", processInstanceId);
  }

  @Override
  public void deleteCommentsByTaskId(String taskId) {
    checkHistoryEnabled();
    getDbSqlSession().delete("deleteCommentsByTaskId", taskId);
  }

  @Override
  public void deleteCommentsByProcessInstanceId(String processInstanceId) {
    checkHistoryEnabled();
    getDbSqlSession().delete("deleteCommentsByProcessInstanceId", processInstanceId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Comment> findCommentsByProcessInstanceId(String processInstanceId) {
    checkHistoryEnabled();
    return getDbSqlSession().selectList("selectCommentsByProcessInstanceId", processInstanceId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Comment> findCommentsByProcessInstanceId(String processInstanceId, String type) {
    checkHistoryEnabled();
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("processInstanceId", processInstanceId);
    params.put("type", type);
    return getDbSqlSession().selectListWithRawParameter("selectCommentsByProcessInstanceIdAndType", params, 0, Integer.MAX_VALUE);
  }

  @Override
  public Comment findComment(String commentId) {
    return getDbSqlSession().selectById(CommentEntity.class, commentId);
  }

  @Override
  public Event findEvent(String commentId) {
    return getDbSqlSession().selectById(CommentEntity.class, commentId);
  }
  
  @Override
  public void delete(CommentEntity commentEntity) {
    checkHistoryEnabled();
    super.delete(commentEntity, false);

    Comment comment = (Comment) commentEntity;
    if (getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      // Forced to fetch the process-instance to associate the right
      // process definition
      String processDefinitionId = null;
      String processInstanceId = comment.getProcessInstanceId();
      if (comment.getProcessInstanceId() != null) {
        ExecutionEntity process = getProcessInstanceManager().findExecutionById(comment.getProcessInstanceId());
        if (process != null) {
          processDefinitionId = process.getProcessDefinitionId();
        }
      }
      getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, commentEntity, processInstanceId, processInstanceId, processDefinitionId));
    }
  }

  protected void checkHistoryEnabled() {
    if (!getHistoryManager().isHistoryEnabled()) {
      throw new ActivitiException("In order to use comments, history should be enabled");
    }
  }
}
