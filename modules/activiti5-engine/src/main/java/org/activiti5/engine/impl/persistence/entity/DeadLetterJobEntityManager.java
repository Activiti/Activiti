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
public class DeadLetterJobEntityManager extends AbstractManager {

  public DeadLetterJobEntity findJobById(String jobId) {
    return (DeadLetterJobEntity) getDbSqlSession().selectOne("selectDeadLetterJob", jobId);
  }
  
  @SuppressWarnings("unchecked")
  public List<DeadLetterJobEntity> findDeadLetterJobsByDuedate(Date duedate, Page page) {
    final String query = "selectDeadLetterJobsByDuedate";
    return getDbSqlSession().selectList(query, duedate, page);
  }

  @SuppressWarnings("unchecked")
  public List<DeadLetterJobEntity> findDeadLetterJobsByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectDeadLetterJobsByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page) {
    final String query = "selectDeadLetterJobByQueryCriteria";
    return getDbSqlSession().selectList(query, jobQuery, page);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findDeadLetterJobsByTypeAndProcessDefinitionKeyNoTenantId(String jobHandlerType, String processDefinitionKey) {
  	 Map<String, String> params = new HashMap<String, String>(2);
     params.put("handlerType", jobHandlerType);
     params.put("processDefinitionKey", processDefinitionKey);
     return getDbSqlSession().selectList("selectDeadLetterJobByTypeAndProcessDefinitionKeyNoTenantId", params);
  }
  
  @SuppressWarnings("unchecked")
  public List<Job> findDeadLetterJobsByTypeAndProcessDefinitionKeyAndTenantId(String jobHandlerType, String processDefinitionKey, String tenantId) {
  	 Map<String, String> params = new HashMap<String, String>(3);
     params.put("handlerType", jobHandlerType);
     params.put("processDefinitionKey", processDefinitionKey);
     params.put("tenantId", tenantId);
     return getDbSqlSession().selectList("selectDeadLetterJobByTypeAndProcessDefinitionKeyAndTenantId", params);
  }
  
  @SuppressWarnings("unchecked")
  public List<Job> findDeadLetterJobsByTypeAndProcessDefinitionId(String jobHandlerType, String processDefinitionId) {
  	 Map<String, String> params = new HashMap<String, String>(2);
     params.put("handlerType", jobHandlerType);
     params.put("processDefinitionId", processDefinitionId);
     return getDbSqlSession().selectList("selectDeadLetterJobByTypeAndProcessDefinitionId", params);
  }

  public long findDeadLetterJobCountByQueryCriteria(JobQueryImpl jobQuery) {
    return (Long) getDbSqlSession().selectOne("selectDeadLetterJobCountByQueryCriteria", jobQuery);
  }
  
  public void updateDeadLetterJobTenantIdForDeployment(String deploymentId, String newTenantId) {
  	HashMap<String, Object> params = new HashMap<String, Object>();
  	params.put("deploymentId", deploymentId);
  	params.put("tenantId", newTenantId);
  	getDbSqlSession().update("updateDeadLetterJobTenantIdForDeployment", params);
  }
  
}
