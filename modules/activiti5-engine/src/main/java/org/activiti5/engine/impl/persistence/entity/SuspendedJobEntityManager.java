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

import org.activiti.engine.runtime.Job;
import org.activiti5.engine.impl.JobQueryImpl;
import org.activiti5.engine.impl.Page;
import org.activiti5.engine.impl.persistence.AbstractManager;


/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class SuspendedJobEntityManager extends AbstractManager {

  public SuspendedJobEntity findJobById(String jobId) {
    return (SuspendedJobEntity) getDbSqlSession().selectOne("selectSuspendedJob", jobId);
  }
  
  @SuppressWarnings("unchecked")
  public List<SuspendedJobEntity> findSuspendedJobsByDuedate(Date duedate, Page page) {
    final String query = "selectSuspendedJobsByDuedate";
    return getDbSqlSession().selectList(query, duedate, page);
  }

  @SuppressWarnings("unchecked")
  public List<SuspendedJobEntity> findSuspendedJobsByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectSuspendedJobsByExecutionId", executionId);
  }
  
  @SuppressWarnings("unchecked")
  public List<SuspendedJobEntity> findSuspendedJobsByProcessInstanceId(String processInstanceId) {
    return getDbSqlSession().selectList("selectSuspendedJobsByProcessInstanceId", processInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page) {
    final String query = "selectSuspendedJobByQueryCriteria";
    return getDbSqlSession().selectList(query, jobQuery, page);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findSuspendedJobsByTypeAndProcessDefinitionKeyNoTenantId(String jobHandlerType, String processDefinitionKey) {
  	 Map<String, String> params = new HashMap<String, String>(2);
     params.put("handlerType", jobHandlerType);
     params.put("processDefinitionKey", processDefinitionKey);
     return getDbSqlSession().selectList("selectSuspendedJobByTypeAndProcessDefinitionKeyNoTenantId", params);
  }
  
  @SuppressWarnings("unchecked")
  public List<Job> findSuspendedJobsByTypeAndProcessDefinitionKeyAndTenantId(String jobHandlerType, String processDefinitionKey, String tenantId) {
  	 Map<String, String> params = new HashMap<String, String>(3);
     params.put("handlerType", jobHandlerType);
     params.put("processDefinitionKey", processDefinitionKey);
     params.put("tenantId", tenantId);
     return getDbSqlSession().selectList("selectSuspendedJobByTypeAndProcessDefinitionKeyAndTenantId", params);
  }
  
  @SuppressWarnings("unchecked")
  public List<Job> findSuspendedJobsByTypeAndProcessDefinitionId(String jobHandlerType, String processDefinitionId) {
  	 Map<String, String> params = new HashMap<String, String>(2);
     params.put("handlerType", jobHandlerType);
     params.put("processDefinitionId", processDefinitionId);
     return getDbSqlSession().selectList("selectSuspendedJobByTypeAndProcessDefinitionId", params);
  }

  public long findSuspendedJobCountByQueryCriteria(JobQueryImpl jobQuery) {
    return (Long) getDbSqlSession().selectOne("selectSuspendedJobCountByQueryCriteria", jobQuery);
  }
  
  public void updateSuspendedJobTenantIdForDeployment(String deploymentId, String newTenantId) {
  	HashMap<String, Object> params = new HashMap<String, Object>();
  	params.put("deploymentId", deploymentId);
  	params.put("tenantId", newTenantId);
  	getDbSqlSession().update("updateSuspendedJobTenantIdForDeployment", params);
  }
  
}
