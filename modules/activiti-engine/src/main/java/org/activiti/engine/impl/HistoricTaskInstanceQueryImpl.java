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

package org.activiti.engine.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.variable.VariableTypes;


/**
 * @author Tom Baeyens
 */
public class HistoricTaskInstanceQueryImpl extends AbstractQuery<HistoricTaskInstanceQuery, HistoricTaskInstance> implements HistoricTaskInstanceQuery {
  
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processDefinitionName;
  protected String processInstanceId;
  protected String executionId;
  protected String taskId;
  protected String taskName;
  protected String taskNameLike;
  protected String taskDescription;
  protected String taskDescriptionLike;
  protected String taskDeleteReason;
  protected String taskDeleteReasonLike;
  protected String taskAssignee;
  protected String taskAssigneeLike;
  protected String taskDefinitionKey;
  protected Integer taskPriority;
  protected boolean finished;
  protected boolean unfinished;
  protected boolean processFinished;
  protected boolean processUnfinished;
  protected List<TaskQueryVariableValue> variables = new ArrayList<TaskQueryVariableValue>();
  protected Date dueDate;
  protected Date dueAfter;
  protected Date dueBefore;

  public HistoricTaskInstanceQueryImpl() {
  }

  public HistoricTaskInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext
      .getHistoricTaskInstanceManager()
      .findHistoricTaskInstanceCountByQueryCriteria(this);
  }

  @Override
  public List<HistoricTaskInstance> executeList(CommandContext commandContext, Page page) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext
      .getHistoricTaskInstanceManager()
      .findHistoricTaskInstancesByQueryCriteria(this, page);
  }


  public HistoricTaskInstanceQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricTaskInstanceQueryImpl executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public HistoricTaskInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }
  
  public HistoricTaskInstanceQuery processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }
  
  public HistoricTaskInstanceQuery processDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
    return this;
  }

  public HistoricTaskInstanceQuery taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }
  public HistoricTaskInstanceQueryImpl taskName(String taskName) {
    this.taskName = taskName;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskNameLike(String taskNameLike) {
    this.taskNameLike = taskNameLike;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskDescription(String taskDescription) {
    this.taskDescription = taskDescription;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskDescriptionLike(String taskDescriptionLike) {
    this.taskDescriptionLike = taskDescriptionLike;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskDeleteReason(String taskDeleteReason) {
    this.taskDeleteReason = taskDeleteReason;
    return this;
  }
  
  public HistoricTaskInstanceQueryImpl taskDeleteReasonLike(String taskDeleteReasonLike) {
    this.taskDeleteReasonLike = taskDeleteReasonLike;
    return this;
  }
  
  public HistoricTaskInstanceQueryImpl taskAssignee(String taskAssignee) {
    this.taskAssignee = taskAssignee;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskAssigneeLike(String taskAssigneeLike) {
    this.taskAssigneeLike = taskAssigneeLike;
    return this;
  }
  
  public HistoricTaskInstanceQueryImpl finished() {
    this.finished = true;
    return this;
  }
  
  public HistoricTaskInstanceQueryImpl unfinished() {
    this.unfinished = true;
    return this;
  }
  
  public HistoricTaskInstanceQueryImpl taskVariableValueEquals(String variableName, Object variableValue) {
    variables.add(new TaskQueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, true));
    return this;
  }
  
  public HistoricTaskInstanceQuery processVariableValueEquals(String variableName, Object variableValue) {
    variables.add(new TaskQueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, false));
    return this;
  }
  
  public HistoricTaskInstanceQuery taskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskPriority(Integer taskPriority) {
    this.taskPriority = taskPriority;
    return this;
  }
  
  public HistoricTaskInstanceQuery processFinished() {
    this.processFinished = true;
    return this;
  }
  
  public HistoricTaskInstanceQuery processUnfinished() {
    this.processUnfinished = true;
    return this;
  }
  
  protected void ensureVariablesInitialized() {    
    VariableTypes types = Context.getProcessEngineConfiguration().getVariableTypes();
    for(QueryVariableValue var : variables) {
      var.initialize(types);
    }
  }
  
  public HistoricTaskInstanceQuery taskDueDate(Date dueDate) {
    this.dueDate = dueDate;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskDueAfter(Date dueAfter) {
    this.dueAfter = dueAfter;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskDueBefore(Date dueBefore) {
    this.dueBefore = dueBefore;
    return this;
  }

  // ordering /////////////////////////////////////////////////////////////////

  public HistoricTaskInstanceQueryImpl orderByTaskId() {
    orderBy(HistoricTaskInstanceQueryProperty.HISTORIC_TASK_INSTANCE_ID);
    return this;
  }
  
  public HistoricTaskInstanceQueryImpl orderByHistoricActivityInstanceId() {
    orderBy(HistoricTaskInstanceQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByProcessDefinitionId() {
    orderBy(HistoricTaskInstanceQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByProcessInstanceId() {
    orderBy(HistoricTaskInstanceQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByExecutionId() {
    orderBy(HistoricTaskInstanceQueryProperty.EXECUTION_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByHistoricTaskInstanceDuration() {
    orderBy(HistoricTaskInstanceQueryProperty.DURATION);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByHistoricTaskInstanceEndTime() {
    orderBy(HistoricTaskInstanceQueryProperty.END);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByHistoricActivityInstanceStartTime() {
    orderBy(HistoricTaskInstanceQueryProperty.START);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByTaskName() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_NAME);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByTaskDescription() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_DESCRIPTION);
    return this;
  }
  
  public HistoricTaskInstanceQuery orderByTaskAssignee() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_ASSIGNEE);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByDeleteReason() {
    orderBy(HistoricTaskInstanceQueryProperty.DELETE_REASON);
    return this;
  }
  
  public HistoricTaskInstanceQuery orderByTaskDefinitionKey() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_DEFINITION_KEY);
    return this;
  }
  
  public HistoricTaskInstanceQuery orderByTaskPriority() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_PRIORITY);
    return this;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getExecutionId() {
    return executionId;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public String getOrderBy() {
    return orderBy;
  }
  public boolean isFinished() {
    return finished;
  }
  public boolean isUnfinished() {
    return unfinished;
  }
  public String getTaskName() {
    return taskName;
  }
  public String getTaskNameLike() {
    return taskNameLike;
  }
  public String getTaskDescription() {
    return taskDescription;
  }
  public String getTaskDescriptionLike() {
    return taskDescriptionLike;
  }
  public String getTaskDeleteReason() {
    return taskDeleteReason;
  }
  public String getTaskDeleteReasonLike() {
    return taskDeleteReasonLike;
  }
  public String getTaskAssignee() {
    return taskAssignee;
  }
  public String getTaskAssigneeLike() {
    return taskAssigneeLike;
  }
  public String getTaskId() {
    return taskId;
  }
  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }
  public List<TaskQueryVariableValue> getVariables() {
    return variables;
  }
}
