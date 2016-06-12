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

package org.activiti5.engine.impl.persistence.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.runtime.Job;
import org.activiti5.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti5.engine.impl.Page;
import org.activiti5.engine.impl.TimerJobQueryImpl;
import org.activiti5.engine.impl.context.Context;
import org.activiti5.engine.impl.persistence.AbstractManager;


/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class TimerJobEntityManager extends AbstractManager {
  
  public void cancelTimers(ExecutionEntity execution) {
    List<TimerJobEntity> timers = Context
      .getCommandContext()
      .getTimerJobEntityManager()
      .findTimersByExecutionId(execution.getId());
    
    for (TimerJobEntity timer: timers) {
      if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
        Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED, timer));
      }
      timer.delete();
    }
  }

  public TimerJobEntity findJobById(String jobId) {
    return (TimerJobEntity) getDbSqlSession().selectOne("selectTimerJob", jobId);
  }
  
  @SuppressWarnings("unchecked")
  public List<TimerJobEntity> findTimerJobsByLockOwner(String lockOwner, int start, int maxNrOfJobs) {
  	return getDbSqlSession().selectList("selectTimerJobsByLockOwner", lockOwner, start, maxNrOfJobs);
  }
  
  @SuppressWarnings("unchecked")
  public List<TimerJobEntity> findTimerJobsByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectTimerJobsByExecutionId", executionId);
  }
  
  @SuppressWarnings("unchecked")
  public List<TimerJobEntity> findTimerJobsByProcessInstanceId(String processInstanceId) {
    return getDbSqlSession().selectList("selectTimerJobsByProcessInstanceId", processInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<TimerJobEntity> findTimersByDuedate(Date duedate, Page page) {
    final String query = "selectTimerJobsByDuedate";
    return getDbSqlSession().selectList(query, duedate, page);
  }

  @SuppressWarnings("unchecked")
  public List<TimerJobEntity> findTimersByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectTimersByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findTimerJobsByQueryCriteria(TimerJobQueryImpl jobQuery, Page page) {
    final String query = "selectTimerJobByQueryCriteria";
    return getDbSqlSession().selectList(query, jobQuery, page);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findTimerJobsByTypeAndProcessDefinitionKeyNoTenantId(String jobHandlerType, String processDefinitionKey) {
  	 Map<String, String> params = new HashMap<String, String>(2);
     params.put("handlerType", jobHandlerType);
     params.put("processDefinitionKey", processDefinitionKey);
     return getDbSqlSession().selectList("selectTimerJobByTypeAndProcessDefinitionKeyNoTenantId", params);
  }
  
  @SuppressWarnings("unchecked")
  public List<Job> findTimerJobsByTypeAndProcessDefinitionKeyAndTenantId(String jobHandlerType, String processDefinitionKey, String tenantId) {
  	 Map<String, String> params = new HashMap<String, String>(3);
     params.put("handlerType", jobHandlerType);
     params.put("processDefinitionKey", processDefinitionKey);
     params.put("tenantId", tenantId);
     return getDbSqlSession().selectList("selectTimerJobByTypeAndProcessDefinitionKeyAndTenantId", params);
  }
  
  @SuppressWarnings("unchecked")
  public List<Job> findTimerJobsByTypeAndProcessDefinitionId(String jobHandlerType, String processDefinitionId) {
  	 Map<String, String> params = new HashMap<String, String>(2);
     params.put("handlerType", jobHandlerType);
     params.put("processDefinitionId", processDefinitionId);
     return getDbSqlSession().selectList("selectTimerJobByTypeAndProcessDefinitionId", params);
  }

  public long findTimerJobCountByQueryCriteria(TimerJobQueryImpl jobQuery) {
    return (Long) getDbSqlSession().selectOne("selectTimerJobCountByQueryCriteria", jobQuery);
  }
  
  public void updateTimerJobTenantIdForDeployment(String deploymentId, String newTenantId) {
  	HashMap<String, Object> params = new HashMap<String, Object>();
  	params.put("deploymentId", deploymentId);
  	params.put("tenantId", newTenantId);
  	getDbSqlSession().update("updateTimerJobTenantIdForDeployment", params);
  }
  
}
