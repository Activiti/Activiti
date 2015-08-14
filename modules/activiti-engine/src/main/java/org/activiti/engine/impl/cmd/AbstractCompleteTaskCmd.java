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
package org.activiti.engine.impl.cmd;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.invocation.TaskListenerInvocation;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLinkType;

/**
 * @author Joram Barrez
 */
public abstract class AbstractCompleteTaskCmd extends NeedsActiveTaskCmd<Void> {
  
  private static final long serialVersionUID = 1L;

  public AbstractCompleteTaskCmd(String taskId) {
    super(taskId);
  }

  protected void executeTaskComplete(CommandContext commandContext, TaskEntity taskEntity, Map<String, Object> variables, boolean localScope) {
    // Task complete logic
    
    if (taskEntity.getDelegationState() != null && taskEntity.getDelegationState().equals(DelegationState.PENDING)) {
      throw new ActivitiException("A delegated task cannot be completed, but should be resolved instead.");
    }

    fireEvent(commandContext, taskEntity, TaskListener.EVENTNAME_COMPLETE);
    if (Authentication.getAuthenticatedUserId() != null && taskEntity.getProcessInstanceId() != null) {
      ExecutionEntity processInstanceEntity = commandContext.getExecutionEntityManager().findExecutionById(taskEntity.getProcessInstanceId());
      processInstanceEntity.involveUser(Authentication.getAuthenticatedUserId(),IdentityLinkType.PARTICIPANT);
    }

    ActivitiEventDispatcher eventDispatcher = Context.getProcessEngineConfiguration().getEventDispatcher();
    if (eventDispatcher.isEnabled()) {
      if (variables != null) {
        eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityWithVariablesEvent(ActivitiEventType.TASK_COMPLETED, taskEntity, variables, localScope));
      } else {
        eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_COMPLETED, taskEntity));
      }
    }

    commandContext.getTaskEntityManager().deleteTask(taskEntity, TaskEntity.DELETE_REASON_COMPLETED, false);

    // Continue process (if not a standalone task)
    if (taskEntity.getExecutionId() != null) {
      ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findExecutionById(taskEntity.getExecutionId());
      commandContext.getAgenda().planTriggerExecutionOperation(executionEntity);
    }
  }
  
  // TODO: this needs to be revised
  public void fireEvent(CommandContext commandContext, TaskEntity taskEntity, String taskEventName) {
    TaskDefinition taskDefinition = getTaskDefinition(taskEntity);
    if (taskDefinition != null) {
      List<TaskListener> taskEventListeners = taskDefinition.getTaskListener(taskEventName);
      if (taskEventListeners != null) {
        for (TaskListener taskListener : taskEventListeners) {
          ExecutionEntity execution = commandContext.getExecutionEntityManager().findExecutionById(taskEntity.getExecutionId());
          if (execution != null) {
            taskEntity.setEventName(taskEventName);
          }
          try {
            Context.getProcessEngineConfiguration()
              .getDelegateInterceptor()
              .handleInvocation(new TaskListenerInvocation(taskListener, (DelegateTask)taskEntity));
          }catch (Exception e) {
            throw new ActivitiException("Exception while invoking TaskListener: "+e.getMessage(), e);
          }
        }
      }
    }
  }
  
  public TaskDefinition getTaskDefinition(TaskEntity taskEntity) {
    
    if (taskEntity.getProcessDefinitionId() == null) {
      return null;
    }
    
    // TODO: this has to be rewritten. Should not live on the ProcessDefinition!
    ProcessDefinitionEntity processDefinitionEntity = ProcessDefinitionUtil.getProcessDefinitionEntity(taskEntity.getProcessDefinitionId());
    return processDefinitionEntity.getTaskDefinitions().get(taskEntity.getTaskDefinitionKey());
  }

}
