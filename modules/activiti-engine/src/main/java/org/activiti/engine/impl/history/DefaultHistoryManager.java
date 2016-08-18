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

package org.activiti.engine.impl.history;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.impl.persistence.entity.CommentEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.impl.persistence.entity.HistoricFormPropertyEntity;
import org.activiti.engine.impl.persistence.entity.HistoricIdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.activiti.engine.task.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager class that centralises recording of all history-related operations
 * that are originated from inside the engine.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class DefaultHistoryManager extends AbstractManager implements HistoryManager {
  
  private static Logger log = LoggerFactory.getLogger(DefaultHistoryManager.class.getName());
  
  private HistoryLevel historyLevel;
  
  public DefaultHistoryManager() {
    this.historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#isHistoryLevelAtLeast(org.activiti.engine.impl.history.HistoryLevel)
   */
  @Override
  public boolean isHistoryLevelAtLeast(HistoryLevel level) {
    if(log.isDebugEnabled()) {
      log.debug("Current history level: {}, level required: {}", historyLevel, level);
    }
    // Comparing enums actually compares the location of values declared in the enum
    return historyLevel.isAtLeast(level);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#isHistoryEnabled()
   */
  @Override
  public boolean isHistoryEnabled() {
    if(log.isDebugEnabled()) {
      log.debug("Current history level: {}", historyLevel);
    }
    return !historyLevel.equals(HistoryLevel.NONE);
  }
  
  // Process related history
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordProcessInstanceEnd(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void recordProcessInstanceEnd(String processInstanceId, String deleteReason, String activityId) {
    
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstanceEntity historicProcessInstance = getHistoricProcessInstanceManager()
              .findHistoricProcessInstance(processInstanceId);
      
      if (historicProcessInstance!=null) {
        historicProcessInstance.markEnded(deleteReason);
        historicProcessInstance.setEndActivityId(activityId);
        
        // Fire event
        ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
    		if (config != null && config.getEventDispatcher().isEnabled()) {
    			config.getEventDispatcher().dispatchEvent(
    					ActivitiEventBuilder.createEntityEvent(ActivitiEventType.HISTORIC_PROCESS_INSTANCE_ENDED, historicProcessInstance));
    		}
      }
    }
  }
  
  @Override
  public void recordProcessInstanceNameChange(String processInstanceId, String newName) {
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstanceEntity historicProcessInstance = getHistoricProcessInstanceManager()
              .findHistoricProcessInstance(processInstanceId);
      
      if (historicProcessInstance!=null) {
        historicProcessInstance.setName(newName);
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordProcessInstanceStart(org.activiti.engine.impl.persistence.entity.ExecutionEntity)
   */
  @Override
  public void recordProcessInstanceStart(ExecutionEntity processInstance) {
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstanceEntity historicProcessInstance = new HistoricProcessInstanceEntity(processInstance);
      
      // Insert historic process-instance
      getDbSqlSession().insert(historicProcessInstance);
      
      // Fire event
      ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
  		if (config != null && config.getEventDispatcher().isEnabled()) {
  			config.getEventDispatcher().dispatchEvent(
  					ActivitiEventBuilder.createEntityEvent(ActivitiEventType.HISTORIC_PROCESS_INSTANCE_CREATED, historicProcessInstance));
  		}
  
      // Also record the start-event manually, as there is no "start" activity history listener for this
      IdGenerator idGenerator = Context.getProcessEngineConfiguration().getIdGenerator();
      
      String processDefinitionId = processInstance.getProcessDefinitionId();
      String processInstanceId = processInstance.getProcessInstanceId();
      String executionId = processInstance.getId();
  
      HistoricActivityInstanceEntity historicActivityInstance = new HistoricActivityInstanceEntity();
      historicActivityInstance.setId(idGenerator.getNextId());
      historicActivityInstance.setProcessDefinitionId(processDefinitionId);
      historicActivityInstance.setProcessInstanceId(processInstanceId);
      historicActivityInstance.setExecutionId(executionId);
      historicActivityInstance.setActivityId(processInstance.getActivityId());
      historicActivityInstance.setActivityName((String) processInstance.getActivity().getProperty("name"));
      historicActivityInstance.setActivityType((String) processInstance.getActivity().getProperty("type"));
      Date now = Context.getProcessEngineConfiguration().getClock().getCurrentTime();
      historicActivityInstance.setStartTime(now);
      
      // Inherit tenant id (if applicable)
      if (processInstance.getTenantId() != null) {
      	historicActivityInstance.setTenantId(processInstance.getTenantId());
      }
      
      getDbSqlSession().insert(historicActivityInstance);
      
      // Fire event
  		if (config != null && config.getEventDispatcher().isEnabled()) {
  			config.getEventDispatcher().dispatchEvent(
  					ActivitiEventBuilder.createEntityEvent(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED, historicActivityInstance));
  		}
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordSubProcessInstanceStart(org.activiti.engine.impl.persistence.entity.ExecutionEntity, org.activiti.engine.impl.persistence.entity.ExecutionEntity)
   */
  @Override
  public void recordSubProcessInstanceStart(ExecutionEntity parentExecution, ExecutionEntity subProcessInstance) {
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      
      HistoricProcessInstanceEntity historicProcessInstance = new HistoricProcessInstanceEntity((ExecutionEntity) subProcessInstance);
     
      ActivityImpl initialActivity = subProcessInstance.getActivity();
      // Fix for ACT-1728: startActivityId not initialized with subprocess-instance
      if(historicProcessInstance.getStartActivityId() == null) {
      	historicProcessInstance.setStartActivityId(subProcessInstance.getProcessDefinition().getInitial().getId());
      	initialActivity = subProcessInstance.getProcessDefinition().getInitial();
      }
      getDbSqlSession().insert(historicProcessInstance);
      
      // Fire event
      ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
  		if (config != null && config.getEventDispatcher().isEnabled()) {
  			config.getEventDispatcher().dispatchEvent(
  					ActivitiEventBuilder.createEntityEvent(ActivitiEventType.HISTORIC_PROCESS_INSTANCE_CREATED, historicProcessInstance));
  		}
      
      
      HistoricActivityInstanceEntity activitiyInstance = findActivityInstance(parentExecution);
      if (activitiyInstance != null) {
        activitiyInstance.setCalledProcessInstanceId(subProcessInstance.getProcessInstanceId());
      }
      
      // Fix for ACT-1728: start-event not recorded for subprocesses
      IdGenerator idGenerator = Context.getProcessEngineConfiguration().getIdGenerator();
      
      // Also record the start-event manually, as there is no "start" activity history listener for this
      HistoricActivityInstanceEntity historicActivityInstance = new HistoricActivityInstanceEntity();
      historicActivityInstance.setId(idGenerator.getNextId());
      historicActivityInstance.setProcessDefinitionId(subProcessInstance.getProcessDefinitionId());
      historicActivityInstance.setProcessInstanceId(subProcessInstance.getProcessInstanceId());
      historicActivityInstance.setExecutionId(subProcessInstance.getId());
      historicActivityInstance.setActivityId(initialActivity.getId());
      historicActivityInstance.setActivityName((String) initialActivity.getProperty("name"));
      historicActivityInstance.setActivityType((String) initialActivity.getProperty("type"));
      Date now = Context.getProcessEngineConfiguration().getClock().getCurrentTime();
      historicActivityInstance.setStartTime(now);
      
      getDbSqlSession().insert(historicActivityInstance);
      
      // Fire event
  		if (config != null && config.getEventDispatcher().isEnabled()) {
  			config.getEventDispatcher().dispatchEvent(
  					ActivitiEventBuilder.createEntityEvent(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED, historicActivityInstance));
  		}
    }
  }
  
  // Activity related history

  /* (non-Javadoc)
 * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordActivityStart(org.activiti.engine.impl.persistence.entity.ExecutionEntity)
 */
  @Override
public void recordActivityStart(ExecutionEntity executionEntity) {
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
    	if(executionEntity.getActivity() != null) {
    		IdGenerator idGenerator = Context.getProcessEngineConfiguration().getIdGenerator();
    		
    		String processDefinitionId = executionEntity.getProcessDefinitionId();
    		String processInstanceId = executionEntity.getProcessInstanceId();
    		String executionId = executionEntity.getId();
    		
    		HistoricActivityInstanceEntity historicActivityInstance = new HistoricActivityInstanceEntity();
    		historicActivityInstance.setId(idGenerator.getNextId());
    		historicActivityInstance.setProcessDefinitionId(processDefinitionId);
    		historicActivityInstance.setProcessInstanceId(processInstanceId);
    		historicActivityInstance.setExecutionId(executionId);
    		historicActivityInstance.setActivityId(executionEntity.getActivityId());
    		historicActivityInstance.setActivityName((String) executionEntity.getActivity().getProperty("name"));
    		historicActivityInstance.setActivityType((String) executionEntity.getActivity().getProperty("type"));
    		historicActivityInstance.setStartTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime());
    		
    	  // Inherit tenant id (if applicable)
        if (executionEntity.getTenantId() != null) {
        	historicActivityInstance.setTenantId(executionEntity.getTenantId());
        }
    		
    		getDbSqlSession().insert(historicActivityInstance);
    		
        // Fire event
        ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
    		if (config != null && config.getEventDispatcher().isEnabled()) {
    			config.getEventDispatcher().dispatchEvent(
    					ActivitiEventBuilder.createEntityEvent(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED, historicActivityInstance));
    		}
    	}
    }
  }
  
  /* (non-Javadoc)
  * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordActivityEnd(org.activiti.engine.impl.persistence.entity.ExecutionEntity)
  */
  @Override
  public void recordActivityEnd(ExecutionEntity executionEntity) {
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(executionEntity);
      if (historicActivityInstance!=null) {
        endHistoricActivityInstance(historicActivityInstance);
      }
    }
  }

  protected void endHistoricActivityInstance(HistoricActivityInstanceEntity historicActivityInstance) {
    historicActivityInstance.markEnded(null);
    
    // Fire event
    ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
    if (config != null && config.getEventDispatcher().isEnabled()) {
    	config.getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED, historicActivityInstance));
    }
  }
  
  /* (non-Javadoc)
  * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordStartEventEnded(java.lang.String, java.lang.String)
  */
  @Override
  public void recordStartEventEnded(ExecutionEntity execution, String activityId) {
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      
      // Interrupted executions might not have an activityId set, skip recording history.
      if(activityId == null) {
        return;
      }
      
      HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(execution, activityId, false); // false -> no need to check the persistent store, as process just started
      if (historicActivityInstance != null) {
        endHistoricActivityInstance(historicActivityInstance);
      }
    }
  }
  
  @Override
  public HistoricActivityInstanceEntity findActivityInstance(ExecutionEntity execution) {
    return findActivityInstance(execution, execution.getActivityId(), true);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.activiti.engine.impl.history.HistoryManagerInterface#findActivityInstance
   * (org.activiti.engine.impl.persistence.entity.ExecutionEntity)
   */
  protected HistoricActivityInstanceEntity findActivityInstance(ExecutionEntity execution, String activityId, boolean checkPersistentStore) {
    
    String executionId = execution.getId();

    // search for the historic activity instance in the dbsqlsession cache
    List<HistoricActivityInstanceEntity> cachedHistoricActivityInstances = getDbSqlSession()
        .findInCache(HistoricActivityInstanceEntity.class);
    for (HistoricActivityInstanceEntity cachedHistoricActivityInstance : cachedHistoricActivityInstances) {
      if (executionId.equals(cachedHistoricActivityInstance.getExecutionId())
          && activityId != null
          && (activityId.equals(cachedHistoricActivityInstance.getActivityId()))
          && (cachedHistoricActivityInstance.getEndTime() == null)) {
        return cachedHistoricActivityInstance;
      }
    }

    List<HistoricActivityInstance> historicActivityInstances = null;
    if (checkPersistentStore) {
      historicActivityInstances = new HistoricActivityInstanceQueryImpl(Context.getCommandContext())
          .executionId(executionId)
          .activityId(activityId)
          .unfinished()
          .listPage(0, 1);
    }

    if (historicActivityInstances != null && !historicActivityInstances.isEmpty()) {
      return (HistoricActivityInstanceEntity) historicActivityInstances.get(0);
    }

    if (execution.getParentId() != null) {
      return findActivityInstance((ExecutionEntity) execution.getParent(), activityId, checkPersistentStore);
    }

    return null;
  }
  
  /* (non-Javadoc)
 * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordExecutionReplacedBy(org.activiti.engine.impl.persistence.entity.ExecutionEntity, org.activiti.engine.impl.pvm.runtime.InterpretableExecution)
 */
  @Override
@SuppressWarnings({ "unchecked", "rawtypes" })
  public void recordExecutionReplacedBy(ExecutionEntity execution, InterpretableExecution replacedBy) {
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      
      // Update the cached historic activity instances that are open
      List<HistoricActivityInstanceEntity> cachedHistoricActivityInstances = getDbSqlSession().findInCache(HistoricActivityInstanceEntity.class);
      for (HistoricActivityInstanceEntity cachedHistoricActivityInstance: cachedHistoricActivityInstances) {
        if ( (cachedHistoricActivityInstance.getEndTime()==null)
             && (execution.getId().equals(cachedHistoricActivityInstance.getExecutionId())) 
           ) {
          cachedHistoricActivityInstance.setExecutionId(replacedBy.getId());
        }
      }
    
      // Update the persisted historic activity instances that are open
      List<HistoricActivityInstanceEntity> historicActivityInstances = (List) new HistoricActivityInstanceQueryImpl(Context.getCommandContext())
        .executionId(execution.getId())
        .unfinished()
        .list();
      for (HistoricActivityInstanceEntity historicActivityInstance: historicActivityInstances) {
        historicActivityInstance.setExecutionId(replacedBy.getId());
      }
    }
  }
  /* (non-Javadoc)
 * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordProcessDefinitionChange(java.lang.String, java.lang.String)
 */
  @Override
public void recordProcessDefinitionChange(String processInstanceId, String processDefinitionId) {
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstanceEntity historicProcessInstance = getHistoricProcessInstanceManager().findHistoricProcessInstance(processInstanceId);
      if(historicProcessInstance != null) {
        historicProcessInstance.setProcessDefinitionId(processDefinitionId);
      }
    }
  }
  
  
  // Task related history 
  
  /* (non-Javadoc)
 * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskCreated(org.activiti.engine.impl.persistence.entity.TaskEntity, org.activiti.engine.impl.persistence.entity.ExecutionEntity)
 */
  @Override
public void recordTaskCreated(TaskEntity task, ExecutionEntity execution) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = new HistoricTaskInstanceEntity(task, execution);
      getDbSqlSession().insert(historicTaskInstance);
    }
  }
  
  /* (non-Javadoc)
 * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskAssignment(org.activiti.engine.impl.persistence.entity.TaskEntity)
 */
  @Override
public void recordTaskAssignment(TaskEntity task) {
    ExecutionEntity executionEntity = task.getExecution();
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      if (executionEntity != null) {
        HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(executionEntity);
        if(historicActivityInstance != null) {
          historicActivityInstance.setAssignee(task.getAssignee());
        }
      }
    }
  }
  
  /* (non-Javadoc)
 * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskClaim(java.lang.String)
 */

  @Override
public void recordTaskClaim(String taskId) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.setClaimTime( Context.getProcessEngineConfiguration().getClock().getCurrentTime());
      }
    }    
  }

  
  /* (non-Javadoc)
  * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskId(org.activiti.engine.impl.persistence.entity.TaskEntity)
  */
  @Override
  public void recordTaskId(TaskEntity task) {
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      ExecutionEntity execution = task.getExecution();
      if (execution != null) {
        HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(execution);
        if(historicActivityInstance != null) {
          historicActivityInstance.setTaskId(task.getId());
        }
      }
    }
  }
  
  /* (non-Javadoc)
  * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskEnd(java.lang.String, java.lang.String)
  */
  @Override
  public void recordTaskEnd(String taskId, String deleteReason) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.markEnded(deleteReason);
      }
    }
  }
  
  /* (non-Javadoc)
  * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskAssigneeChange(java.lang.String, java.lang.String)
  */
  @Override
  public void recordTaskAssigneeChange(String taskId, String assignee) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setAssignee(assignee);
      }
    }
  }
  
  /* (non-Javadoc)
  * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskOwnerChange(java.lang.String, java.lang.String)
  */
  @Override
  public void recordTaskOwnerChange(String taskId, String owner) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setOwner(owner);
      }
    }
  }

  /* (non-Javadoc)
  * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskNameChange(java.lang.String, java.lang.String)
  */
  @Override
  public void recordTaskNameChange(String taskId, String taskName) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setName(taskName);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskDescriptionChange(java.lang.String, java.lang.String)
   */
  @Override
  public void recordTaskDescriptionChange(String taskId, String description) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setDescription(description);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskDueDateChange(java.lang.String, java.util.Date)
   */
  @Override
  public void recordTaskDueDateChange(String taskId, Date dueDate) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setDueDate(dueDate);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskPriorityChange(java.lang.String, int)
   */
  @Override
  public void recordTaskPriorityChange(String taskId, int priority) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setPriority(priority);
      }
    }
  }
  
 /* (non-Javadoc)
  * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskCategoryChange(java.lang.String, java.lang.String)
  */
  @Override
  public void recordTaskCategoryChange(String taskId, String category) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setCategory(category);
      }
    }
  }
  
  @Override
  public void recordTaskFormKeyChange(String taskId, String formKey) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setFormKey(formKey);
      }
    }	
  }


  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskParentTaskIdChange(java.lang.String, java.lang.String)
   */
  @Override
  public void recordTaskParentTaskIdChange(String taskId, String parentTaskId) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setParentTaskId(parentTaskId);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskExecutionIdChange(java.lang.String, java.lang.String)
   */
  @Override
  public void recordTaskExecutionIdChange(String taskId, String executionId) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setExecutionId(executionId);
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskDefinitionKeyChange(org.activiti.engine.impl.persistence.entity.TaskEntity, java.lang.String)
   */
  @Override
  public void recordTaskDefinitionKeyChange(TaskEntity task, String taskDefinitionKey) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, task.getId());
      if (historicTaskInstance != null) {
        historicTaskInstance.setTaskDefinitionKey(taskDefinitionKey);
        
        if (taskDefinitionKey != null) {
          Expression taskFormExpression = task.getTaskDefinition().getFormKeyExpression();
          if (taskFormExpression != null) {
            Object formValue = taskFormExpression.getValue(task.getExecution());
            if (formValue != null) {
              historicTaskInstance.setFormKey(formValue.toString());
            }
          }
        }
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskProcessDefinitionChange(java.lang.String, java.lang.String)
   */
  @Override
  public void recordTaskProcessDefinitionChange(String taskId, String processDefinitionId) {
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.setProcessDefinitionId(processDefinitionId);
      }
    }
  }
 
  // Variables related history
  
  /* (non-Javadoc)
  * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordVariableCreate(org.activiti.engine.impl.persistence.entity.VariableInstanceEntity)
  */
  @Override
  public void recordVariableCreate(VariableInstanceEntity variable) {
    // Historic variables
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricVariableInstanceEntity.copyAndInsert(variable);
    }
  }
  
  /* (non-Javadoc)
  * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordHistoricDetailVariableCreate(org.activiti.engine.impl.persistence.entity.VariableInstanceEntity, org.activiti.engine.impl.persistence.entity.ExecutionEntity, boolean)
  */
  @Override
  public void recordHistoricDetailVariableCreate(VariableInstanceEntity variable, ExecutionEntity sourceActivityExecution, boolean useActivityId) {
    if (isHistoryLevelAtLeast(HistoryLevel.FULL)) {
      
      HistoricDetailVariableInstanceUpdateEntity historicVariableUpdate = 
          HistoricDetailVariableInstanceUpdateEntity.copyAndInsert(variable);
      
      if (useActivityId && sourceActivityExecution != null) {
        HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(sourceActivityExecution); 
        if (historicActivityInstance!=null) {
          historicVariableUpdate.setActivityInstanceId(historicActivityInstance.getId());
        }
      }
    }
  }
  
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.activiti.engine.impl.history.HistoryManagerInterface#recordVariableUpdate
	 * (org.activiti.engine.impl.persistence.entity.VariableInstanceEntity)
	 */
	@Override
	public void recordVariableUpdate(VariableInstanceEntity variable) {
		if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
			HistoricVariableInstanceEntity historicProcessVariable = getDbSqlSession()
			    .findInCache(HistoricVariableInstanceEntity.class, variable.getId());
			if (historicProcessVariable == null) {
				historicProcessVariable = Context.getCommandContext()
				    .getHistoricVariableInstanceEntityManager()
				    .findHistoricVariableInstanceByVariableInstanceId(variable.getId());
			}

			if (historicProcessVariable != null) {
				historicProcessVariable.copyValue(variable);
			} else {
				HistoricVariableInstanceEntity.copyAndInsert(variable);
			}
		}
	}

	@Override
	public void recordVariableRemoved(VariableInstanceEntity variable) {
		if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
			HistoricVariableInstanceEntity historicProcessVariable = getDbSqlSession()
			    .findInCache(HistoricVariableInstanceEntity.class, variable.getId());
			if (historicProcessVariable == null) {
				historicProcessVariable = Context.getCommandContext()
				    .getHistoricVariableInstanceEntityManager()
				    .findHistoricVariableInstanceByVariableInstanceId(variable.getId());
			}

			if (historicProcessVariable != null) {
				Context.getCommandContext()
		    .getHistoricVariableInstanceEntityManager()
		    .delete(historicProcessVariable);
			} 
		}
	}
  
  // Comment related history
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#createIdentityLinkComment(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public void createIdentityLinkComment(String taskId, String userId, String groupId, String type, boolean create) {
    createIdentityLinkComment(taskId, userId, groupId, type, create, false);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#createIdentityLinkComment(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, boolean)
   */
  @Override
  public void createIdentityLinkComment(String taskId, String userId, String groupId, String type, boolean create, boolean forceNullUserId) {
    if(isHistoryEnabled()) {
      String authenticatedUserId = Authentication.getAuthenticatedUserId();
      CommentEntity comment = new CommentEntity();
      comment.setUserId(authenticatedUserId);
      comment.setType(CommentEntity.TYPE_EVENT);
      comment.setTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime());
      comment.setTaskId(taskId);
      if (userId!=null || forceNullUserId) {
        if(create) {
          comment.setAction(Event.ACTION_ADD_USER_LINK);
        } else {
          comment.setAction(Event.ACTION_DELETE_USER_LINK);
        }
        comment.setMessage(new String[]{userId, type});
      } else {
        if(create) {
          comment.setAction(Event.ACTION_ADD_GROUP_LINK);
        } else {
          comment.setAction(Event.ACTION_DELETE_GROUP_LINK);
        }
        comment.setMessage(new String[]{groupId, type});
      }
      getSession(CommentEntityManager.class).insert(comment);
    }
  }
  
  @Override
  public void createProcessInstanceIdentityLinkComment(String processInstanceId, String userId, String groupId, String type, boolean create) {
    createProcessInstanceIdentityLinkComment(processInstanceId, userId, groupId, type, create, false);
  }

  @Override
  public void createProcessInstanceIdentityLinkComment(String processInstanceId, String userId, String groupId, String type, boolean create, boolean forceNullUserId) {
    if(isHistoryEnabled()) {
      String authenticatedUserId = Authentication.getAuthenticatedUserId();
      CommentEntity comment = new CommentEntity();
      comment.setUserId(authenticatedUserId);
      comment.setType(CommentEntity.TYPE_EVENT);
      comment.setTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime());
      comment.setProcessInstanceId(processInstanceId);
      if (userId!=null || forceNullUserId) {
        if(create) {
          comment.setAction(Event.ACTION_ADD_USER_LINK);
        } else {
          comment.setAction(Event.ACTION_DELETE_USER_LINK);
        }
        comment.setMessage(new String[]{userId, type});
      } else {
        if(create) {
          comment.setAction(Event.ACTION_ADD_GROUP_LINK);
        } else {
          comment.setAction(Event.ACTION_DELETE_GROUP_LINK);
        }
        comment.setMessage(new String[]{groupId, type});
      }
      getSession(CommentEntityManager.class).insert(comment);
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#createAttachmentComment(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public void createAttachmentComment(String taskId, String processInstanceId, String attachmentName, boolean create) {
    if (isHistoryEnabled()) {
      String userId = Authentication.getAuthenticatedUserId();
      CommentEntity comment = new CommentEntity();
      comment.setUserId(userId);
      comment.setType(CommentEntity.TYPE_EVENT);
      comment.setTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime());
      comment.setTaskId(taskId);
      comment.setProcessInstanceId(processInstanceId);
      if(create) {
        comment.setAction(Event.ACTION_ADD_ATTACHMENT);
      } else {
        comment.setAction(Event.ACTION_DELETE_ATTACHMENT);
      }
      comment.setMessage(attachmentName);
      getSession(CommentEntityManager.class).insert(comment);
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#reportFormPropertiesSubmitted(org.activiti.engine.impl.persistence.entity.ExecutionEntity, java.util.Map, java.lang.String)
   */
  @Override
  public void reportFormPropertiesSubmitted(ExecutionEntity processInstance, Map<String, String> properties, String taskId) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      for (String propertyId: properties.keySet()) {
        String propertyValue = properties.get(propertyId);
        HistoricFormPropertyEntity historicFormProperty = new HistoricFormPropertyEntity(processInstance, propertyId, propertyValue, taskId);
        getDbSqlSession().insert(historicFormProperty);
      }
    }
  }
  
  // Identity link related history
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordIdentityLinkCreated(org.activiti.engine.impl.persistence.entity.IdentityLinkEntity)
   */
  @Override
  public void recordIdentityLinkCreated(IdentityLinkEntity identityLink) {
    // It makes no sense storing historic counterpart for an identity-link that is related
    // to a process-definition only as this is never kept in history
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT) && (identityLink.getProcessInstanceId() != null || identityLink.getTaskId() != null)) {
      HistoricIdentityLinkEntity historicIdentityLinkEntity = new HistoricIdentityLinkEntity(identityLink);
      getDbSqlSession().insert(historicIdentityLinkEntity);
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#deleteHistoricIdentityLink(java.lang.String)
   */
  @Override
  public void deleteHistoricIdentityLink(String id) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      getHistoricIdentityLinkEntityManager().deleteHistoricIdentityLink(id);
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#updateProcessBusinessKeyInHistory(org.activiti.engine.impl.persistence.entity.ExecutionEntity)
   */
  @Override
	public void updateProcessBusinessKeyInHistory(ExecutionEntity processInstance) {
		if (isHistoryEnabled()) {
			if (log.isDebugEnabled()) {
				log.debug("updateProcessBusinessKeyInHistory : {}",processInstance.getId());
			}
			if (processInstance != null) {
				HistoricProcessInstanceEntity historicProcessInstance = getDbSqlSession()
				    .selectById(HistoricProcessInstanceEntity.class, processInstance.getId());
				if (historicProcessInstance != null) {
					historicProcessInstance.setBusinessKey(processInstance.getProcessBusinessKey());
					getDbSqlSession().update(historicProcessInstance);
				}
			}
		}
	}
  
}
