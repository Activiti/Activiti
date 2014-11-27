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
    
    Comment comment = (Comment) persistentObject;
    if(getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
    	// Forced to fetch the process-instance to associate the right process definition
    	String processDefinitionId = null;
    	String processInstanceId = comment.getProcessInstanceId();
    	if(comment.getProcessInstanceId() != null) {
    		ExecutionEntity process = getProcessInstanceManager().findExecutionById(comment.getProcessInstanceId());
    		if(process != null) {
    			processDefinitionId = process.getProcessDefinitionId();
    		}
    	}
    	getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, persistentObject, processInstanceId, processInstanceId, processDefinitionId));
    }
  }

  public void insert(PersistentObject persistentObject) {
    checkHistoryEnabled();
    super.insert(persistentObject);
    
    Comment comment = (Comment) persistentObject;
    if(getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
    	// Forced to fetch the process-instance to associate the right process definition
    	String processDefinitionId = null;
    	String processInstanceId = comment.getProcessInstanceId();
    	if(comment.getProcessInstanceId() != null) {
    		ExecutionEntity process = getProcessInstanceManager().findExecutionById(comment.getProcessInstanceId());
    		if(process != null) {
    			processDefinitionId = process.getProcessDefinitionId();
    		}
    	}
    	getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, persistentObject, processInstanceId, processInstanceId, processDefinitionId));
    	getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, persistentObject, processInstanceId, processInstanceId, processDefinitionId));
    }
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
  
  @SuppressWarnings("unchecked")
  public List<Event> findEventsByProcessInstanceId(String processInstanceId) {
    checkHistoryEnabled();
    return getDbSqlSession().selectList("selectEventsByProcessInstanceId", processInstanceId);
  }

  public void deleteCommentsByTaskId(String taskId) {
    checkHistoryEnabled();
    getDbSqlSession().delete("deleteCommentsByTaskId", taskId);
  }
  
  public void deleteCommentsByProcessInstanceId(String processInstanceId) {
    checkHistoryEnabled();
    getDbSqlSession().delete("deleteCommentsByProcessInstanceId", processInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<Comment> findCommentsByProcessInstanceId(String processInstanceId) {
    checkHistoryEnabled();
    return getDbSqlSession().selectList("selectCommentsByProcessInstanceId", processInstanceId);
  }

  public List<Comment> findCommentsByProcessInstanceId(String processInstanceId, String type) {
    checkHistoryEnabled();
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("processInstanceId", processInstanceId);
    params.put("type", type);
    return getDbSqlSession().selectListWithRawParameter("selectCommentsByProcessInstanceIdAndType", params, 0, Integer.MAX_VALUE);
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
