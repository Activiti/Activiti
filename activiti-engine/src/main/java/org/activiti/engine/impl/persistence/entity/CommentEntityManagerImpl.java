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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.data.CommentDataManager;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;

/**


 */
@Internal
@Deprecated
public class CommentEntityManagerImpl extends AbstractEntityManager<CommentEntity> implements CommentEntityManager {
  
  protected CommentDataManager commentDataManager;
  
  public CommentEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration, CommentDataManager commentDataManager) {
    super(processEngineConfiguration);
    this.commentDataManager = commentDataManager;
  }

  @Override
  protected DataManager<CommentEntity> getDataManager() {
    return commentDataManager;
  }
  
  @Override
  public void insert(CommentEntity commentEntity) {
    checkHistoryEnabled();
    
    insert(commentEntity, false);

    Comment comment = (Comment) commentEntity;
    if (getEventDispatcher().isEnabled()) {
      // Forced to fetch the process-instance to associate the right
      // process definition
      String processDefinitionId = null;
      String processInstanceId = comment.getProcessInstanceId();
      if (comment.getProcessInstanceId() != null) {
        ExecutionEntity process = getExecutionEntityManager().findById(comment.getProcessInstanceId());
        if (process != null) {
          processDefinitionId = process.getProcessDefinitionId();
        }
      }
      getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, commentEntity, processInstanceId, processInstanceId, processDefinitionId));
      getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, commentEntity, processInstanceId, processInstanceId, processDefinitionId));
    }
  }

  @Override
  public List<Comment> findCommentsByTaskId(String taskId) {
    checkHistoryEnabled();
    return commentDataManager.findCommentsByTaskId(taskId);
  }

  @Override
  public List<Comment> findCommentsByTaskIdAndType(String taskId, String type) {
    checkHistoryEnabled();
    return commentDataManager.findCommentsByTaskIdAndType(taskId, type);
  }

  @Override
  public List<Comment> findCommentsByType(String type) {
    checkHistoryEnabled();
    return commentDataManager.findCommentsByType(type);
  }

  @Override
  public List<Event> findEventsByTaskId(String taskId) {
    checkHistoryEnabled();
    return commentDataManager.findEventsByTaskId(taskId);
  }

  @Override
  public List<Event> findEventsByProcessInstanceId(String processInstanceId) {
    checkHistoryEnabled();
    return commentDataManager.findEventsByProcessInstanceId(processInstanceId);
  }

  @Override
  public void deleteCommentsByTaskId(String taskId) {
    checkHistoryEnabled();
    commentDataManager.deleteCommentsByTaskId(taskId);
  }

  @Override
  public void deleteCommentsByProcessInstanceId(String processInstanceId) {
    checkHistoryEnabled();
    commentDataManager.deleteCommentsByProcessInstanceId(processInstanceId);
  }

  @Override
  public List<Comment> findCommentsByProcessInstanceId(String processInstanceId) {
    checkHistoryEnabled();
    return commentDataManager.findCommentsByProcessInstanceId(processInstanceId);
  }

  @Override
  public List<Comment> findCommentsByProcessInstanceId(String processInstanceId, String type) {
    checkHistoryEnabled();
    return commentDataManager.findCommentsByProcessInstanceId(processInstanceId, type);
  }

  @Override
  public Comment findComment(String commentId) {
    return commentDataManager.findComment(commentId);
  }

  @Override
  public Event findEvent(String commentId) {
    return commentDataManager.findEvent(commentId);
  }
  
  @Override
  public void delete(CommentEntity commentEntity) {
    checkHistoryEnabled();
    
    delete(commentEntity, false);

    Comment comment = (Comment) commentEntity;
    if (getEventDispatcher().isEnabled()) {
      // Forced to fetch the process-instance to associate the right
      // process definition
      String processDefinitionId = null;
      String processInstanceId = comment.getProcessInstanceId();
      if (comment.getProcessInstanceId() != null) {
        ExecutionEntity process = getExecutionEntityManager().findById(comment.getProcessInstanceId());
        if (process != null) {
          processDefinitionId = process.getProcessDefinitionId();
        }
      }
      getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, commentEntity, processInstanceId, processInstanceId, processDefinitionId));
    }
  }

  protected void checkHistoryEnabled() {
    if (!getHistoryManager().isHistoryEnabled()) {
      throw new ActivitiException("In order to use comments, history should be enabled");
    }
  }

  public CommentDataManager getCommentDataManager() {
    return commentDataManager;
  }

  public void setCommentDataManager(CommentDataManager commentDataManager) {
    this.commentDataManager = commentDataManager;
  }
  
}
