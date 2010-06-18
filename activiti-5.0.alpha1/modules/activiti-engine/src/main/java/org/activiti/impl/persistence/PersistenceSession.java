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

import java.util.List;
import java.util.Map;

import org.activiti.Page;
import org.activiti.ProcessInstance;
import org.activiti.Task;
import org.activiti.impl.bytes.ByteArrayImpl;
import org.activiti.impl.db.DbidBlock;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.execution.ExecutionDbImpl;
import org.activiti.impl.execution.JobImpl;
import org.activiti.impl.identity.GroupImpl;
import org.activiti.impl.identity.UserImpl;
import org.activiti.impl.repository.DeploymentImpl;
import org.activiti.impl.task.TaskImpl;
import org.activiti.impl.task.TaskInvolvement;
import org.activiti.impl.tx.Session;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface PersistenceSession extends Session {
  
  /* Deployment */
  List<DeploymentImpl> findDeployments();
  DeploymentImpl findDeployment(String deploymentId);
  DeploymentImpl findDeploymentByProcessDefinitionId(String processDefinitionId);
  List<ByteArrayImpl> findDeploymentResources(String deploymentId);
  List<String> findDeploymentResourceNames(String deploymentId);
  ByteArrayImpl findDeploymentResource(String deploymentId, String resourceName);
  byte[] getDeploymentResourceBytes(String resourceId);
  void insertDeployment(DeploymentImpl deployment);
  void deleteDeployment(String deploymentId);
  
  /* Process definition */
  ProcessDefinitionImpl findProcessDefinitionById(String processDefinitionId);
  ProcessDefinitionImpl findLatestProcessDefinitionByKey(String processDefinitionKey);
  List<ProcessDefinitionImpl> findProcessDefinitions();
  List<ProcessDefinitionImpl> findProcessDefinitionsByDeployment(String deploymentId);
  ProcessDefinitionImpl findProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey);
  void insertProcessDefinition(ProcessDefinitionImpl processDefinition);

  void insert(PersistentObject persistentObject);
  void delete(PersistentObject persistentObject);

  /* Execution */
  ExecutionDbImpl findExecution(String executionId);
  List<ExecutionDbImpl> findExecutionsByProcessDefintion(String processDefinitionId);
  
  List<ProcessInstance> dynamicFindProcessInstances(Map<String, Object> params);
  long dynamicFindProcessInstanceCount(Map<String, Object> params);
  
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
  JobImpl findJob(long jobId);
  /**
   * Returns something like:
   * [
   *    [JobA]
   *    [JobB1,JobB2,JobB3],
   *    [JobC]
   * ]
   * Where jobs with IDs A and C can be run on their own,
   *  but the B ones must be run one after another
   *  on the same Thread.
   * The jobs should be claimed in the process of
   *  being fetched.
   */
  List<List<Long>> findPendingJobs(int limit);

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

  DbidBlock getNextDbidBlock();
}
