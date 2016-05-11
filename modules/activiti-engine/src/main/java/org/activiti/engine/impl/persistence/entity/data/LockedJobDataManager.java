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
import org.activiti.engine.impl.persistence.entity.LockedJobEntity;

/**
 * @author Tijs Rademakers
 * @author Vasile Dirla
 */
public interface LockedJobDataManager extends DataManager<LockedJobEntity> {

  List<LockedJobEntity> findJobsByLockOwner(String lockOwner, int start, int maxNrOfJobs);

  List<LockedJobEntity> findJobsByExecutionId(final String executionId);

  List<LockedJobEntity> findExclusiveJobsToExecute(String processInstanceId);

  List<LockedJobEntity> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page);
  
  List<LockedJobEntity> findJobsByTypeAndProcessDefinitionIds(String jobHandlerType, List<String> processDefinitionIds);

  List<LockedJobEntity> findJobsByTypeAndProcessDefinitionId(String jobHandlerType, String processDefinitionId);
  
  List<LockedJobEntity> findJobsByTypeAndProcessDefinitionKeyNoTenantId(String jobHandlerType, String processDefinitionKey);
  
  List<LockedJobEntity> findJobsByTypeAndProcessDefinitionKeyAndTenantId(String jobHandlerType, String processDefinitionKey, String tenantId);
  
  long findJobCountByQueryCriteria(JobQueryImpl jobQuery);
  
  void updateJobTenantIdForDeployment(String deploymentId, String newTenantId);

  List<LockedJobEntity> selectExpiredJobs(long maxLockDuration, Page page);
}
