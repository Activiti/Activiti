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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.TaskDataManager;
import org.activiti.engine.impl.util.Activiti5Util;
import org.activiti.engine.task.Task;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TaskEntityManagerImpl extends AbstractEntityManager<TaskEntity> implements TaskEntityManager {
  
  protected TaskDataManager taskDataManager;
  
  public TaskEntityManagerImpl() {
    
  }
  
  public TaskEntityManagerImpl(TaskDataManager taskDataManager) {
    this.taskDataManager = taskDataManager;
  }
  
  @Override
  protected DataManager<TaskEntity> getDataManager() {
    return taskDataManager;
  }
  
  /**
   * Creates a new task. This task still will have to be persisted. See {@link #insert(ExecutionEntity))}.
   */
  @Override
  public TaskEntity create(Date createTime) {
    TaskEntity task = new TaskEntity();
    task.isIdentityLinksInitialized = true;
    task.createTime = createTime;
    return task;
  }
  
  /** creates and initializes a new persistent task. */
  @Override
  public TaskEntity createAndInsert(DelegateExecution execution) {
    TaskEntity task = create(getClock().getCurrentTime());
    insert(task, (ExecutionEntity) execution);
    return task;
  }
  
  @Override
  public void insert(TaskEntity entity, boolean fireCreateEvent) {
    super.insert(entity, fireCreateEvent);
    getHistoryManager().recordTaskId(entity);
  }
  
  @Override
  public void insert(TaskEntity taskEntity, ExecutionEntity execution) {

    // Inherit tenant id (if applicable)
    if (execution != null && execution.getTenantId() != null) {
      taskEntity.setTenantId(execution.getTenantId());
    }

    if (execution != null) {
      execution.getTasks().add(taskEntity);
      taskEntity.setExecutionId(execution.getId());
      taskEntity.setProcessInstanceId(execution.getProcessInstanceId());
      taskEntity.setProcessDefinitionId(execution.getProcessDefinitionId());
      
      getHistoryManager().recordTaskExecutionIdChange(taskEntity.getId(), taskEntity.getExecutionId());
    }
    
    super.insert(taskEntity, true);

    getHistoryManager().recordTaskCreated(taskEntity, execution);
  }
  
  @Override
  public TaskEntity update(TaskEntity taskEntity) {
    // Needed to make history work: the setter will also update the historic task
    taskEntity.setOwner(taskEntity.getOwner());
    taskEntity.setAssignee(taskEntity.getAssignee(), true, false);
    taskEntity.setDelegationState(taskEntity.getDelegationState());
    taskEntity.setName(taskEntity.getName());
    taskEntity.setDescription(taskEntity.getDescription());
    taskEntity.setPriority(taskEntity.getPriority());
    taskEntity.setCategory(taskEntity.getCategory());
    taskEntity.setCreateTime(taskEntity.getCreateTime());
    taskEntity.setDueDate(taskEntity.getDueDate());
    taskEntity.setParentTaskId(taskEntity.getParentTaskId());
    taskEntity.setFormKey(taskEntity.getFormKey());

    return super.update(taskEntity);
  }

  @Override
  public void deleteTasksByProcessInstanceId(String processInstanceId, String deleteReason, boolean cascade) {
    List<TaskEntity> tasks = findTasksByProcessInstanceId(processInstanceId);

    String reason = (deleteReason == null || deleteReason.length() == 0) ? TaskEntity.DELETE_REASON_DELETED : deleteReason;

    for (TaskEntity task : tasks) {
      if (getEventDispatcher().isEnabled()) {
        getEventDispatcher().dispatchEvent(
                ActivitiEventBuilder.createActivityCancelledEvent(task.getExecution().getActivityId(), task.getName(), task.getExecutionId(), task.getProcessInstanceId(),
                    task.getProcessDefinitionId(), "userTask", UserTaskActivityBehavior.class.getName(), deleteReason));
      }

      deleteTask(task, reason, cascade, false);
    }
  }
  
  @Override
  public void deleteTask(TaskEntity task, String deleteReason, boolean cascade, boolean cancel) {
    if (!task.isDeleted()) {
      task.fireEvent(TaskListener.EVENTNAME_DELETE);
      task.setDeleted(true);

      String taskId = task.getId();

      List<Task> subTasks = findTasksByParentTaskId(taskId);
      for (Task subTask : subTasks) {
        deleteTask((TaskEntity) subTask, deleteReason, cascade, cancel);
      }

      getIdentityLinkEntityManager().deleteIdentityLinksByTaskId(taskId);
      getVariableInstanceEntityManager().deleteVariableInstanceByTask(task);

      if (cascade) {
        getHistoricTaskInstanceEntityManager().delete(taskId);
      } else {
        getHistoryManager().recordTaskEnd(taskId, deleteReason);
      }

      delete(task, false);

      if (getEventDispatcher().isEnabled()) {
        if (cancel) {
          getEventDispatcher().dispatchEvent(
                  ActivitiEventBuilder.createActivityCancelledEvent(task.getExecution().getActivityId(), task.getName(), task.getExecutionId(), task.getProcessInstanceId(),
                      task.getProcessDefinitionId(), "userTask", UserTaskActivityBehavior.class.getName(), deleteReason));
        }
        
        getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, task));
      }
    }
  }

  @Override
  public List<TaskEntity> findTasksByExecutionId(String executionId) {
    return taskDataManager.findTasksByExecutionId(executionId);
  }

  @Override
  public List<TaskEntity> findTasksByProcessInstanceId(String processInstanceId) {
    return taskDataManager.findTasksByProcessInstanceId(processInstanceId);
  }

  @Override
  public List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery) {
    return taskDataManager.findTasksByQueryCriteria(taskQuery);
  }

  @Override
  public List<Task> findTasksAndVariablesByQueryCriteria(TaskQueryImpl taskQuery) {
    return taskDataManager.findTasksAndVariablesByQueryCriteria(taskQuery);
  }

  @Override
  public long findTaskCountByQueryCriteria(TaskQueryImpl taskQuery) {
    return taskDataManager.findTaskCountByQueryCriteria(taskQuery);
  }

  @Override
  public List<Task> findTasksByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return taskDataManager.findTasksByNativeQuery(parameterMap, firstResult, maxResults);
  }

  @Override
  public long findTaskCountByNativeQuery(Map<String, Object> parameterMap) {
    return taskDataManager.findTaskCountByNativeQuery(parameterMap);
  }

  @Override
  public List<Task> findTasksByParentTaskId(String parentTaskId) {
    return taskDataManager.findTasksByParentTaskId(parentTaskId);
  }

  @Override
  public void deleteTask(String taskId, String deleteReason, boolean cascade) {
    
    TaskEntity task = findById(taskId);

    if (task != null) {
      if (task.getExecutionId() != null) {
        throw new ActivitiException("The task cannot be deleted because is part of a running process");
      }
      
      if (Activiti5Util.isActiviti5ProcessDefinitionId(getCommandContext(), task.getProcessDefinitionId())) {
        Activiti5CompatibilityHandler activiti5CompatibilityHandler = Activiti5Util.getActiviti5CompatibilityHandler(); 
        activiti5CompatibilityHandler.deleteTask(taskId, deleteReason, cascade);
        return;
      }

      String reason = (deleteReason == null || deleteReason.length() == 0) ? TaskEntity.DELETE_REASON_DELETED : deleteReason;
      deleteTask(task, reason, cascade, false);
    } else if (cascade) {
      getHistoricTaskInstanceEntityManager().delete(taskId);
    }
  }

  @Override
  public void updateTaskTenantIdForDeployment(String deploymentId, String newTenantId) {
    taskDataManager.updateTaskTenantIdForDeployment(deploymentId, newTenantId);
  }

  public TaskDataManager getTaskDataManager() {
    return taskDataManager;
  }

  public void setTaskDataManager(TaskDataManager taskDataManager) {
    this.taskDataManager = taskDataManager;
  }
  
}
