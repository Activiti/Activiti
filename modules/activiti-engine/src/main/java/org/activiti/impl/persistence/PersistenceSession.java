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
import org.activiti.engine.impl.persistence.db.IdBlock;
import org.activiti.engine.impl.persistence.identity.GroupImpl;
import org.activiti.engine.impl.persistence.identity.UserImpl;
import org.activiti.engine.impl.persistence.runtime.ByteArrayImpl;
import org.activiti.impl.db.execution.DbExecutionImpl;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.history.HistoricActivityInstanceImpl;
import org.activiti.impl.history.HistoricProcessInstanceImpl;
import org.activiti.impl.job.JobImpl;
import org.activiti.impl.job.TimerImpl;
import org.activiti.impl.task.TaskImpl;
import org.activiti.impl.task.TaskInvolvement;
import org.activiti.impl.variable.VariableInstance;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface PersistenceSession extends Session {
  
  void commit();
  void rollback();

  byte[] getByteArrayBytes(String byteArrayId);
  ByteArrayImpl findByteArrayById(String byteArrayValueId);
  
  void insert(PersistentObject persistentObject);
  void delete(PersistentObject persistentObject);

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

  /* Task */
  TaskImpl findTask(String taskId);
  List<TaskImpl> findTasksByExecution(String executionId);
  List<Task> findTasksByAssignee(String assignee);
  List<Task> findCandidateTasks(String userId, List<String> groupIds);
  
  List<Task> dynamicFindTasks(Map<String, Object> params, Page page);
  long dynamicFindTaskCount(Map<String, Object> params);

  /* TaskInvolvement */
  List<TaskInvolvement> findTaskInvolvementsByTask(String taskId);
  
  /* Job */
  JobImpl findJobById(String jobId);
  List<JobImpl> findJobs();
  List<JobImpl> findNextJobsToExecute(int maxNrOfJobs);
  List<TimerImpl> findUnlockedTimersByDuedate(Date duedate, int nrOfTimers);
  List<TimerImpl> findTimersByExecutionId(String executionId);
  List<JobImpl> findLockedJobs();
  
  List<Job> dynamicFindJobs(Map<String, Object> params, Page page);
  long dynamicJobCount(Map<String, Object> params);

  /* User */
  void saveUser(UserImpl user);
  UserImpl findUser(String userId);
  List<UserImpl> findUsersByGroup(String groupId);
  void deleteUser(String userId);
  boolean isValidUser(String userId);
  
  /* Group */
  void saveGroup(GroupImpl group);
  GroupImpl findGroup(String groupId);
  List<GroupImpl> findGroupsByUser(String userId);
  List<GroupImpl> findGroupsByUserAndType(String userId, String groupType);
  void deleteGroup(String groupId);

  /* Membership */
  void createMembership(String userId, String groupId);
  void deleteMembership(String userId, String groupId);

  /* Management */
  Map<String, Long> getTableCount();
  TablePage getTablePage(String tableName, int offset, int maxResults, String sortColumn, SortOrder sortOrder);
  TableMetaData getTableMetaData(String tableName);

  /* History */
  void saveHistoricProcessInstance(HistoricProcessInstanceImpl historicProcessInstance);
  HistoricProcessInstanceImpl findHistoricProcessInstance(String processInstanceId);
  void deleteHistoricProcessInstance(String processInstanceId);
  void saveHistoricActivityInstance(HistoricActivityInstanceImpl historicActivityInstance);
  HistoricActivityInstanceImpl findHistoricActivityInstance(String activityId, String processInstanceId);
  void deleteHistoricActivityInstance(String activityId, String processInstanceId);
  }
