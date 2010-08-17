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

package org.activiti.engine.impl.cfg;

import java.util.Date;
import java.util.List;

import org.activiti.engine.Job;
import org.activiti.engine.Page;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.persistence.runtime.ByteArrayEntity;
import org.activiti.engine.impl.persistence.runtime.ExecutionEntity;
import org.activiti.engine.impl.persistence.runtime.JobEntity;
import org.activiti.engine.impl.persistence.runtime.TimerEntity;
import org.activiti.engine.impl.persistence.runtime.VariableInstanceEntity;


/**
 * @author Tom Baeyens
 */
public interface RuntimeSession {
  
  void deleteProcessInstance(String processInstanceId, String deleteReason);
  ExecutionEntity findSubProcessInstanceBySuperExecutionId(String superExecutionId);
  long findExecutionCountByQueryCriteria(Object executionQuery);
  List<ExecutionEntity> findExecutionsByQueryCriteria(Object executionQuery);
  List<ExecutionEntity> findChildExecutionsByParentExecutionId(String executionId);
  ExecutionEntity findExecutionById(String activityInstanceId);
  
  List<VariableInstanceEntity> findVariableInstancesByExecutionId(String executionId);
  List<VariableInstanceEntity> findVariablesByTaskId(String taskId);

  byte[] getByteArrayBytes(String byteArrayId);
  ByteArrayEntity findByteArrayById(String byteArrayId);

  JobEntity findJobById(String jobId);
  List<JobEntity> findJobs();
  List<JobEntity> findNextJobsToExecute(int maxNrOfJobs);
  List<JobEntity> findLockedJobs();
  List<TimerEntity> findUnlockedTimersByDuedate(Date duedate, int nrOfTimers);
  List<TimerEntity> findTimersByExecutionId(String executionId);
  List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page);
  long findJobCountByQueryCriteria(JobQueryImpl jobQuery);
}
