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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.persistence.CachedPersistentObjectMatcher;
import org.activiti.engine.impl.util.Activiti5Util;
import org.activiti.engine.task.Task;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TaskEntityManagerImpl extends AbstractEntityManager<TaskEntity> implements TaskEntityManager {
  
  @Override
  public Class<TaskEntity> getManagedPersistentObject() {
    return TaskEntity.class;
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
  public void update(TaskEntity taskEntity) {
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

    getDbSqlSession().update(taskEntity);

    if (getEventDispatcher().isEnabled()) {
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_UPDATED, taskEntity));
    }
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void deleteTasksByProcessInstanceId(String processInstanceId, String deleteReason, boolean cascade) {
    List<TaskEntity> tasks = (List) getDbSqlSession().createTaskQuery().processInstanceId(processInstanceId).list();

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
        getHistoricTaskInstanceEntityManager().deleteHistoricTaskInstanceById(taskId);
      } else {
        getHistoryManager().recordTaskEnd(taskId, deleteReason);
      }

      getDbSqlSession().delete(task);

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
  public TaskEntity findTaskById(String id) {
    if (id == null) {
      throw new ActivitiIllegalArgumentException("Invalid task id : null");
    }
    return (TaskEntity) getDbSqlSession().selectById(TaskEntity.class, id);
  }

  @Override
  public List<TaskEntity> findTasksByExecutionId(final String executionId) {
    return getList("selectTasksByExecutionId", executionId, new CachedPersistentObjectMatcher<TaskEntity>() {
      
      public boolean isRetained(TaskEntity taskEntity) {
        return taskEntity.getExecutionId() != null && executionId.equals(taskEntity.getExecutionId());
      }
      
    }, true);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<TaskEntity> findTasksByProcessInstanceId(String processInstanceId) {
    return getDbSqlSession().selectList("selectTasksByProcessInstanceId", processInstanceId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery) {
    final String query = "selectTaskByQueryCriteria";
    return getDbSqlSession().selectList(query, taskQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Task> findTasksAndVariablesByQueryCriteria(TaskQueryImpl taskQuery) {
    final String query = "selectTaskWithVariablesByQueryCriteria";
    // paging doesn't work for combining task instances and variables due to
    // an outer join, so doing it in-memory
    if (taskQuery.getFirstResult() < 0 || taskQuery.getMaxResults() <= 0) {
      return Collections.EMPTY_LIST;
    }

    int firstResult = taskQuery.getFirstResult();
    int maxResults = taskQuery.getMaxResults();

    // setting max results, limit to 20000 results for performance reasons
    taskQuery.setMaxResults(20000);
    taskQuery.setFirstResult(0);

    List<Task> instanceList = getDbSqlSession().selectListWithRawParameterWithoutFilter(query, taskQuery, taskQuery.getFirstResult(), taskQuery.getMaxResults());

    if (instanceList != null && !instanceList.isEmpty()) {
      if (firstResult > 0) {
        if (firstResult <= instanceList.size()) {
          int toIndex = firstResult + Math.min(maxResults, instanceList.size() - firstResult);
          return instanceList.subList(firstResult, toIndex);
        } else {
          return Collections.EMPTY_LIST;
        }
      } else {
        int toIndex = Math.min(maxResults, instanceList.size());
        return instanceList.subList(0, toIndex);
      }
    }
    return Collections.EMPTY_LIST;
  }

  @Override
  public long findTaskCountByQueryCriteria(TaskQueryImpl taskQuery) {
    return (Long) getDbSqlSession().selectOne("selectTaskCountByQueryCriteria", taskQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Task> findTasksByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectTaskByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @Override
  public long findTaskCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectTaskCountByNativeQuery", parameterMap);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Task> findTasksByParentTaskId(String parentTaskId) {
    return getDbSqlSession().selectList("selectTasksByParentTaskId", parentTaskId);
  }

  @Override
  public void deleteTask(String taskId, String deleteReason, boolean cascade) {
    
    TaskEntity task = getTaskEntityManager().findTaskById(taskId);

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
      getHistoricTaskInstanceEntityManager().deleteHistoricTaskInstanceById(taskId);
    }
  }

  @Override
  public void updateTaskTenantIdForDeployment(String deploymentId, String newTenantId) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("tenantId", newTenantId);
    getDbSqlSession().update("updateTaskTenantIdForDeployment", params);
  }

}
