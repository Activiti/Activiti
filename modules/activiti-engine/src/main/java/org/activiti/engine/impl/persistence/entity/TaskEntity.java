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
package org.activiti.engine.impl.persistence.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.db.Entity;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.task.Task;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 * @author Tijs Rademakers
 */
public interface TaskEntity extends VariableScope, Task, DelegateTask, Entity, HasRevision {

  String DELETE_REASON_COMPLETED = "completed";
  String DELETE_REASON_DELETED = "deleted";

  void resolve();

  void forceUpdate();
  
  ExecutionEntity getExecution();

  List<IdentityLinkEntity> getIdentityLinks();

  void setExecutionVariables(Map<String, Object> parameters);

  void setAssignee(String assignee, boolean dispatchAssignmentEvent, boolean dispatchUpdateEvent);

  void setOwner(String owner, boolean dispatchUpdateEvent);

  void setDueDate(Date dueDate, boolean dispatchUpdateEvent);

  void setPriority(int priority, boolean dispatchUpdateEvent);

  void setCategoryWithoutCascade(String category);

  void setParentTaskIdWithoutCascade(String parentTaskId);

  void setFormKeyWithoutCascade(String formKey);

  void setTaskDefinition(TaskDefinition taskDefinition);

  TaskDefinition getTaskDefinition();

  void setCreateTime(Date createTime);

  void setProcessDefinitionId(String processDefinitionId);

  String getInitialAssignee();

  void setTaskDefinitionKey(String taskDefinitionKey);

  void setEventName(String eventName);

  void setExecutionId(String executionId);

  ExecutionEntity getProcessInstance();

  void setProcessInstance(ExecutionEntity processInstance);

  void setExecution(ExecutionEntity execution);

  void setProcessInstanceId(String processInstanceId);

  String getDelegationStateString();

  void setDelegationStateString(String delegationStateString);

  boolean isDeleted();

  void setDeleted(boolean isDeleted);

  Map<String, VariableInstanceEntity> getVariableInstances();

  int getSuspensionState();

  void setSuspensionState(int suspensionState);

  List<VariableInstanceEntity> getQueryVariables();

  void setQueryVariables(List<VariableInstanceEntity> queryVariables);
  
  void fireEvent(String taskEventName);
  
}
