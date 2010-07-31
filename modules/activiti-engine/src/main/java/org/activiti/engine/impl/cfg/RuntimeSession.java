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
import java.util.Map;

import org.activiti.engine.Job;
import org.activiti.engine.Page;
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.impl.persistence.runtime.ActivityInstanceEntity;
import org.activiti.engine.impl.persistence.runtime.ByteArrayImpl;
import org.activiti.engine.impl.persistence.runtime.JobImpl;
import org.activiti.engine.impl.persistence.runtime.ProcessInstanceEntity;
import org.activiti.engine.impl.persistence.runtime.TimerImpl;
import org.activiti.engine.impl.variable.VariableInstance;


/**
 * @author Tom Baeyens
 */
public interface RuntimeSession {
  
  void insertProcessInstance(ProcessInstanceEntity processInstance);
  void deleteProcessInstance(ProcessInstanceEntity processInstance);

  void insertActivityInstance(ActivityInstanceEntity activityInstance);
  void deleteActivityInstance(ActivityInstanceEntity activityInstance);
  
//  DbExecutionEntity findExecution(String executionId);
//  List<ExecutionImpl> findChildExecutions(String parentExecutionid);
  List<ProcessInstanceEntity> findProcessInstancesByProcessDefintionId(String processDefinitionId);
  ProcessInstanceEntity findSubProcessInstance(String superExecutionId);
  long findProcessInstanceCountByDynamicCriteria(Map<String, Object> params);
  List<ProcessInstance> findProcessInstancesByDynamicCriteria(Map<String, Object> params);
  
  List<VariableInstance> findVariablesByExecutionId(String executionId);
  List<VariableInstance> findVariablesByTaskId(String taskId);
  byte[] getByteArrayBytes(String byteArrayId);
  ByteArrayImpl findByteArrayById(String byteArrayId);

  JobImpl findJobById(String jobId);
  List<JobImpl> findJobs();
  List<JobImpl> findNextJobsToExecute(int maxNrOfJobs);
  List<JobImpl> findLockedJobs();
  List<TimerImpl> findUnlockedTimersByDuedate(Date duedate, int nrOfTimers);
  List<TimerImpl> findTimersByExecutionId(String executionId);
  List<Job> dynamicFindJobs(Map<String, Object> params, Page page);
  long dynamicJobCount(Map<String, Object> params);
}
