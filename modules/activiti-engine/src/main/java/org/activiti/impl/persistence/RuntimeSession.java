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
package org.activiti.impl.persistence;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.Job;
import org.activiti.engine.Page;
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.SortOrder;
import org.activiti.engine.TableMetaData;
import org.activiti.engine.TablePage;
import org.activiti.engine.Task;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.PersistentObject;
import org.activiti.engine.impl.persistence.db.IdBlock;
import org.activiti.engine.impl.persistence.identity.GroupImpl;
import org.activiti.engine.impl.persistence.identity.UserImpl;
import org.activiti.engine.impl.persistence.runtime.ByteArrayImpl;
import org.activiti.engine.impl.persistence.runtime.JobImpl;
import org.activiti.engine.impl.persistence.runtime.TimerImpl;
import org.activiti.engine.impl.persistence.task.TaskEntity;
import org.activiti.engine.impl.persistence.task.TaskInvolvement;
import org.activiti.engine.impl.variable.VariableInstance;
import org.activiti.impl.db.execution.DbExecutionImpl;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.history.HistoricActivityInstanceImpl;
import org.activiti.impl.history.HistoricProcessInstanceImpl;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface RuntimeSession extends Session {
  
  byte[] getByteArrayBytes(String byteArrayId);
  ByteArrayImpl findByteArrayById(String byteArrayValueId);
  
  /* Execution */
  DbExecutionImpl findExecution(String executionId);
  List<DbExecutionImpl> findProcessInstancesByProcessDefintionId(String processDefinitionId);
  List<ExecutionImpl> findChildExecutions(String parentExecutionid);
  void deleteExecution(String executionId);
  DbExecutionImpl findSubProcessInstance(String superExecutionId);
  
  List<ProcessInstance> findProcessInstancesByDynamicCriteria(Map<String, Object> params);
  long findProcessInstanceCountByDynamicCriteria(Map<String, Object> params);
  
  /* Variables */
  List<VariableInstance> findVariablesByExecutionId(String id);
  List<VariableInstance> findVariablesByTaskId(String id);
  void addDeserializedObject(Object deserializedObject, byte[] bytes, VariableInstance variableInstance);

  /* Job */
  JobImpl findJobById(String jobId);
  List<JobImpl> findJobs();
  List<JobImpl> findNextJobsToExecute(int maxNrOfJobs);
  List<TimerImpl> findUnlockedTimersByDuedate(Date duedate, int nrOfTimers);
  List<TimerImpl> findTimersByExecutionId(String executionId);
  List<JobImpl> findLockedJobs();
  
  List<Job> dynamicFindJobs(Map<String, Object> params, Page page);
  long dynamicJobCount(Map<String, Object> params);

}
