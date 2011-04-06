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
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.task.TaskEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;


/**
 * @author Tom Baeyens
 */
public class ExecutionManager extends AbstractManager {
  
  public void deleteProcessInstancesByProcessDefinition(ProcessDefinition processDefinition, String deleteReason) {
    List<ProcessInstance> processInstances = getPersistenceSession()
      .createProcessInstanceQuery()
      .processDefinitionId(processDefinition.getId())
      .list();
  
    for (ProcessInstance processInstance: processInstances) {
      deleteProcessInstance(processInstance.getId(), deleteReason);
    }
  }
  
  @SuppressWarnings("unchecked")
  public void deleteProcessInstance(String processInstanceId, String deleteReason) {
    ExecutionEntity execution = findExecutionById(processInstanceId);
    
    if(execution == null) {
      throw new ActivitiException("No process instance found for id '" + processInstanceId + "'");
    }
    
    List<TaskEntity> tasks = (List) getPersistenceSession()
      .createTaskQuery()
      .processInstanceId(processInstanceId)
      .list();
    
    TaskManager taskManager = Context
      .getCommandContext()
      .getTaskManager();
    
    for (TaskEntity task: tasks) {
      taskManager.deleteTask(task, TaskEntity.DELETE_REASON_DELETED);
    }
    
    execution.deleteCascade(deleteReason);
  }

  public ExecutionEntity findSubProcessInstanceBySuperExecutionId(String superExecutionId) {
    return (ExecutionEntity) getPersistenceSession().selectOne("selectSubProcessInstanceBySuperExecutionId", superExecutionId);
  }
  
  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findChildExecutionsByParentExecutionId(String parentExecutionId) {
    return getPersistenceSession().selectList("selectExecutionsByParentExecutionId", parentExecutionId);
  }

  public ExecutionEntity findExecutionById(String executionId) {
    return (ExecutionEntity) getPersistenceSession().selectById(ExecutionEntity.class, executionId);
  }
  
  public long findExecutionCountByQueryCriteria(Object executionQuery) {
    return (Long) getPersistenceSession().selectOne("selectExecutionCountByQueryCriteria", executionQuery);
  }

  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findExecutionsByQueryCriteria(Object executionQuery, Page page) {
    return getPersistenceSession().selectList("selectExecutionsByQueryCriteria", executionQuery, page);
  }

  public long findProcessInstanceCountByQueryCriteria(Object executionQuery) {
    return (Long) getPersistenceSession().selectOne("selectProcessInstanceCountByQueryCriteria", executionQuery);
  }
  
  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstanceByQueryCriteria(Object executionQuery, Page page) {
    return getPersistenceSession().selectList("selectProcessInstanceByQueryCriteria", executionQuery, page);
  }

}
