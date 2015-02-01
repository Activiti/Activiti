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
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;


/**
 * @author Tom Baeyens
 */
public class AttachmentEntityManager extends AbstractManager {

  @SuppressWarnings("unchecked")
  public List<Attachment> findAttachmentsByProcessInstanceId(String processInstanceId) {
    checkHistoryEnabled();
    return getDbSqlSession().selectList("selectAttachmentsByProcessInstanceId", processInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<Attachment> findAttachmentsByTaskId(String taskId) {
    checkHistoryEnabled();
    return getDbSqlSession().selectList("selectAttachmentsByTaskId", taskId);
  }

  @SuppressWarnings("unchecked")
  public void deleteAttachmentsByTaskId(String taskId) {
    checkHistoryEnabled();
    List<AttachmentEntity> attachments = getDbSqlSession().selectList("selectAttachmentsByTaskId", taskId);
    boolean dispatchEvents = getProcessEngineConfiguration().getEventDispatcher().isEnabled();

    String processInstanceId = null;
    String processDefinitionId = null;
    String executionId = null;
    
    if(dispatchEvents && attachments != null && !attachments.isEmpty()) {
    	// Forced to fetch the task to get hold of the process definition for event-dispatching, if available
    	Task task = getTaskManager().findTaskById(taskId);
    	if(task != null) {
    		processDefinitionId = task.getProcessDefinitionId();
    		processInstanceId = task.getProcessInstanceId();
    		executionId = task.getExecutionId();
    	}
    }
    
    for (AttachmentEntity attachment: attachments) {
      String contentId = attachment.getContentId();
      if (contentId!=null) {
        getByteArrayManager().deleteByteArrayById(contentId);
      }
      getDbSqlSession().delete(attachment);
      if(dispatchEvents) {
      	getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
      			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, attachment, executionId, processInstanceId, processDefinitionId));
      }
    }
  }
  
  protected void checkHistoryEnabled() {
    if(!getHistoryManager().isHistoryEnabled()) {
      throw new ActivitiException("In order to use attachments, history should be enabled");
    }
  }
}

