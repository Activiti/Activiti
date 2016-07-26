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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.task.Task;


/**
 * @author Tom Baeyens
 */
public class TaskEntityManager extends AbstractManager {

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void deleteTasksByProcessInstanceId(String processInstanceId, String deleteReason, boolean cascade) {
    List<TaskEntity> tasks = (List) getDbSqlSession()
      .createTaskQuery()
      .processInstanceId(processInstanceId)
      .list();
  
    String reason = (deleteReason == null || deleteReason.length() == 0) ? TaskEntity.DELETE_REASON_DELETED : deleteReason;

    CommandContext commandContext = Context.getCommandContext();

    for (TaskEntity task: tasks) {
      if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
        commandContext.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createActivityCancelledEvent(
            task.getExecution().getActivityId(),
            task.getName(),
            task.getExecutionId(),
            task.getProcessInstanceId(),
            task.getProcessDefinitionId(),
            "userTask", UserTaskActivityBehavior.class.getName(), deleteReason));
      }

      deleteTask(task, reason, cascade);
    }
  }

  public void deleteTask(TaskEntity task, String deleteReason, boolean cascade) {
    if (!task.isDeleted()) {
    	 task.fireEvent(TaskListener.EVENTNAME_DELETE);
      task.setDeleted(true);
      
      CommandContext commandContext = Context.getCommandContext();
      String taskId = task.getId();
      
      List<Task> subTasks = findTasksByParentTaskId(taskId);
      for (Task subTask: subTasks) {
        deleteTask((TaskEntity) subTask, deleteReason, cascade);
      }
      
      commandContext
        .getIdentityLinkEntityManager()
        .deleteIdentityLinksByTaskId(taskId);

      commandContext
        .getVariableInstanceEntityManager()
        .deleteVariableInstanceByTask(task);

      if (cascade) {
        commandContext
          .getHistoricTaskInstanceEntityManager()
          .deleteHistoricTaskInstanceById(taskId);
      } else {
        commandContext
          .getHistoryManager()
          .recordTaskEnd(taskId, deleteReason);
      }
        
      getDbSqlSession().delete(task);
      
      if(commandContext.getEventDispatcher().isEnabled()) {
      	commandContext.getEventDispatcher().dispatchEvent(
      			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, task));
      }
    }
  }


  public TaskEntity findTaskById(String id) {
    if (id == null) {
      throw new ActivitiIllegalArgumentException("Invalid task id : null");
    }
    return (TaskEntity) getDbSqlSession().selectById(TaskEntity.class, id);
  }

  @SuppressWarnings("unchecked")
  public List<TaskEntity> findTasksByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectTasksByExecutionId", executionId);
  }
  
  @SuppressWarnings("unchecked")
  public List<TaskEntity> findTasksByProcessInstanceId(String processInstanceId) {
    return getDbSqlSession().selectList("selectTasksByProcessInstanceId", processInstanceId);
  }
  
  @Deprecated
  public List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery, Page page) {
    taskQuery.setFirstResult(page.getFirstResult());
    taskQuery.setMaxResults(page.getMaxResults());
    return findTasksByQueryCriteria(taskQuery);
  }
  
  @SuppressWarnings("unchecked")
  public List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery) {
    final String query = "selectTaskByQueryCriteria";
    return getDbSqlSession().selectList(query, taskQuery);
  }
  
  @SuppressWarnings("unchecked")
  public List<Task> findTasksAndVariablesByQueryCriteria(TaskQueryImpl taskQuery) {
    final String query = "selectTaskWithVariablesByQueryCriteria";
    // paging doesn't work for combining task instances and variables due to an outer join, so doing it in-memory
    if (taskQuery.getFirstResult() < 0 || taskQuery.getMaxResults() <= 0) {
      return Collections.EMPTY_LIST;
    }
    
    int firstResult = taskQuery.getFirstResult();
    int maxResults = taskQuery.getMaxResults();
    
    // setting max results, limit to 20000 results for performance reasons
    if (taskQuery.getTaskVariablesLimit() != null) {
      taskQuery.setMaxResults(taskQuery.getTaskVariablesLimit());
    } else {
      taskQuery.setMaxResults(Context.getProcessEngineConfiguration().getTaskQueryLimit());
    }
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

  public long findTaskCountByQueryCriteria(TaskQueryImpl taskQuery) {
    return (Long) getDbSqlSession().selectOne("selectTaskCountByQueryCriteria", taskQuery);
  }
  
  @SuppressWarnings("unchecked")
  public List<Task> findTasksByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectTaskByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findTaskCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectTaskCountByNativeQuery", parameterMap);
  }

  @SuppressWarnings("unchecked")
  public List<Task> findTasksByParentTaskId(String parentTaskId) {
    return getDbSqlSession().selectList("selectTasksByParentTaskId", parentTaskId);
  }

  public void deleteTask(String taskId, String deleteReason, boolean cascade) {
    TaskEntity task = Context
      .getCommandContext()
      .getTaskEntityManager()
      .findTaskById(taskId);
    
    if (task!=null) {
      if(task.getExecutionId() != null) {
        throw new ActivitiException("The task cannot be deleted because is part of a running process");
      }
      
      String reason = (deleteReason == null || deleteReason.length() == 0) ? TaskEntity.DELETE_REASON_DELETED : deleteReason;
      deleteTask(task, reason, cascade);
    } else if (cascade) {
      Context
        .getCommandContext()
        .getHistoricTaskInstanceEntityManager()
        .deleteHistoricTaskInstanceById(taskId);
    }
  }
  
  public void updateTaskTenantIdForDeployment(String deploymentId, String newTenantId) {
  	HashMap<String, Object> params = new HashMap<String, Object>();
  	params.put("deploymentId", deploymentId);
  	params.put("tenantId", newTenantId);
  	getDbSqlSession().update("updateTaskTenantIdForDeployment", params);
  }
  
}
