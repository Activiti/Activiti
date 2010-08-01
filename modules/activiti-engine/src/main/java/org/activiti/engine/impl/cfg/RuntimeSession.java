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
import org.activiti.engine.impl.persistence.runtime.ByteArrayEntity;
import org.activiti.engine.impl.persistence.runtime.JobEntity;
import org.activiti.engine.impl.persistence.runtime.ProcessInstanceEntity;
import org.activiti.engine.impl.persistence.runtime.TimerEntity;
import org.activiti.engine.impl.persistence.runtime.VariableInstanceEntity;
import org.activiti.engine.impl.persistence.task.TaskInvolvementEntity;


/**
 * @author Tom Baeyens
 */
public interface RuntimeSession {
  
  void insertProcessInstance(ProcessInstanceEntity processInstance);
  void deleteProcessInstance(String processInstanceId);
  ProcessInstanceEntity findProcessInstanceById(String processInstanceId);
  List<ProcessInstanceEntity> findProcessInstancesByProcessDefintionId(String processDefinitionId);
  ProcessInstanceEntity findSubProcessInstance(String superExecutionId);
  long findProcessInstanceCountByDynamicCriteria(Map<String, Object> params);
  List<ProcessInstance> findProcessInstancesByDynamicCriteria(Map<String, Object> params);

  void insertActivityInstance(ActivityInstanceEntity activityInstance);
  void deleteActivityInstance(String activityInstanceId);
  ActivityInstanceEntity findActivityInstanceById(String activityInstanceId);
  ActivityInstanceEntity findActivityInstanceByProcessInstanceIdAndActivityId(String processInstanceId, String activityId);
  
  void insertVariableInstance(VariableInstanceEntity variableInstanceEntity);
  void deleteVariableInstance(String variableInstanceId);
  List<VariableInstanceEntity> findVariableInstancessByExecutionId(String executionId);
  List<VariableInstanceEntity> findVariablesByTaskId(String taskId);

  void insertByteArray(ByteArrayEntity byteArrayEntity);
  void deleteByteArray(String byteArrayId);
  byte[] getByteArrayBytes(String byteArrayId);
  ByteArrayEntity findByteArrayById(String byteArrayId);

  JobEntity findJobById(String jobId);
  List<JobEntity> findJobs();
  List<JobEntity> findNextJobsToExecute(int maxNrOfJobs);
  List<JobEntity> findLockedJobs();
  List<TimerEntity> findUnlockedTimersByDuedate(Date duedate, int nrOfTimers);
  List<TimerEntity> findTimersByExecutionId(String executionId);
  List<Job> dynamicFindJobs(Map<String, Object> params, Page page);
  long dynamicJobCount(Map<String, Object> params);
  void deleteJob(String jobId);
}
