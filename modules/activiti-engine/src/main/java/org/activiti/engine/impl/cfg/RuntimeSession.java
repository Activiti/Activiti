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

import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.runtime.ByteArrayEntity;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.runtime.JobEntity;
import org.activiti.engine.impl.runtime.TimerEntity;
import org.activiti.engine.impl.runtime.VariableInstanceEntity;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;


/**
 * @author Tom Baeyens
 */
public interface RuntimeSession {
  
  void deleteProcessInstance(String processInstanceId, String deleteReason);
  ExecutionEntity findSubProcessInstanceBySuperExecutionId(String superExecutionId);
  long findExecutionCountByQueryCriteria(Object executionQuery);
  List<ExecutionEntity> findExecutionsByQueryCriteria(Object executionQuery, Page page);
  long findProcessInstanceCountByQueryCriteria(Object executionQuery);
  List<ProcessInstance> findProcessInstanceByQueryCriteria(Object executionQuery, Page page);
  List<ExecutionEntity> findChildExecutionsByParentExecutionId(String executionId);
  ExecutionEntity findExecutionById(String executionId);
  
  List<VariableInstanceEntity> findVariableInstancesByExecutionId(String executionId);
  List<VariableInstanceEntity> findVariablesByTaskId(String taskId);

  byte[] getByteArrayBytes(String byteArrayId);
  ByteArrayEntity findByteArrayById(String byteArrayId);

  JobEntity findJobById(String jobId);
  List<JobEntity> findNextJobsToExecute(Page page);
  List<TimerEntity> findUnlockedTimersByDuedate(Date duedate, Page page);
  List<TimerEntity> findTimersByExecutionId(String executionId);
  List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page);
  long findJobCountByQueryCriteria(JobQueryImpl jobQuery);
}
