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

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;


/**
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class ExecutionQueryImpl extends ExecutionVariableQueryImpl<ExecutionQuery, Execution> 
  implements ExecutionQuery {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String activityId;
  protected String executionId;
  protected String processInstanceId;
  
  // Not used by end-users, but needed for dynamic ibatis query
  protected String superProcessInstanceId;
  protected String subProcessInstanceId;
  
  public ExecutionQueryImpl() {
  }
  
  public ExecutionQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }
  
  public ExecutionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public boolean isProcessInstancesOnly() {
    return false; // see dynamic query
  }

  public ExecutionQueryImpl processDefinitionId(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ActivitiException("Process definition id is null");
    }
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public ExecutionQueryImpl processDefinitionKey(String processDefinitionKey) {
    if (processDefinitionKey == null) {
      throw new ActivitiException("Process definition key is null");
    }
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }
  
  public ExecutionQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiException("Process instance id is null");
    }
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public ExecutionQueryImpl executionId(String executionId) {
    if (executionId == null) {
      throw new ActivitiException("Execution id is null");
    }
    this.executionId = executionId;
    return this;
  }
  
  public ExecutionQueryImpl activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }
  
  
  //ordering ////////////////////////////////////////////////////
  
  public ExecutionQueryImpl orderByProcessInstanceId() {
    this.orderProperty = ExecutionQueryProperty.PROCESS_INSTANCE_ID;
    return this;
  }
  
  public ExecutionQueryImpl orderByProcessDefinitionId() {
    this.orderProperty = ExecutionQueryProperty.PROCESS_DEFINITION_ID;
    return this;
  }
  
  public ExecutionQueryImpl orderByProcessDefinitionKey() {
    this.orderProperty = ExecutionQueryProperty.PROCESS_DEFINITION_KEY;
    return this;
  }
  
  //results ////////////////////////////////////////////////////
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getExecutionManager()
      .findExecutionCountByQueryCriteria(this);
  }

  @SuppressWarnings("unchecked")
  public List<Execution> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    return (List) commandContext
      .getExecutionManager()
      .findExecutionsByQueryCriteria(this, page);
  }
  
  //getters ////////////////////////////////////////////////////

  public boolean getOnlyProcessInstances() {
    return false;
  }
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public String getActivityId() {
    return activityId;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getProcessInstanceIds() {
    return null;
  }
  public String getBusinessKey() {
    return null;
  }
  public String getExecutionId() {
    return executionId;
  }
  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }
  public String getSubProcessInstanceId() {
    return subProcessInstanceId;
  }
}
