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
package org.activiti.engine.impl.persistence.entity.data;

import java.util.List;

import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.runtime.Job;

/**
 * @author Joram Barrez
 */
public interface JobDataManager extends DataManager<JobEntity> {
  
  List<JobEntity> findNextJobsToExecute(Page page);

  List<JobEntity> findJobsByExecutionId(final String executionId);

  List<JobEntity> findExclusiveJobsToExecute(String processInstanceId);

  List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page);
  
  List<Job> findJobsByTypeAndProcessDefinitionIds(String jobHandlerType, List<String> processDefinitionIds);
  
  List<Job> findJobsByTypeAndProcessDefinitionKeyNoTenantId(String jobHandlerType, String processDefinitionKey);
  
  List<Job> findJobsByTypeAndProcessDefinitionKeyAndTenantId(String jobHandlerType, String processDefinitionKey, String tenantId);
  
  List<Job> findJobsByTypeAndProcessDefinitionId(String jobHandlerType, String processDefinitionId);
  
  long findJobCountByQueryCriteria(JobQueryImpl jobQuery);

  void updateJobTenantIdForDeployment(String deploymentId, String newTenantId);
}
