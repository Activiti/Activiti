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
import org.activiti.engine.impl.runtime.ExecutionQueryProperty;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;


/**
 * @author Joram Barrez
 */
public class ExecutionQueryImpl extends AbstractQuery<Execution> implements ExecutionQuery {

  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String activityId;
  protected String executionId;
  protected String processInstanceId;
  protected ExecutionQueryProperty orderProperty;
  
  // Not used by end-users, but needed for dynamic ibatis query
  protected String superProcessInstanceId;
  protected String subProcessInstanceId;
  
  protected CommandExecutor commandExecutor;
  
  public ExecutionQueryImpl() {
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
    if (activityId == null) {
      throw new ActivitiException("Activity id is null");
    }
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
  
  public ExecutionQueryImpl orderBy(ExecutionQueryProperty property) {
    this.orderProperty = property;
    return this;
  }
  
  public ExecutionQueryImpl asc() {
    return direction(Direction.ASCENDING);
  }
  
  public ExecutionQueryImpl desc() {
    return direction(Direction.DESCENDING);
  }
  
  public ExecutionQueryImpl direction(Direction direction) {
    if (orderProperty==null) {
      throw new ActivitiException("You should call any of the orderBy methods first before specifying a direction");
    }
    addOrder(orderProperty.getName(), direction.getName());
    orderProperty = null;
    return this;
  }
  
  //results ////////////////////////////////////////////////////
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getRuntimeSession()
      .findExecutionCountByQueryCriteria(this);
  }

  @SuppressWarnings("unchecked")
  public List<Execution> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return (List) commandContext
      .getRuntimeSession()
      .findExecutionsByQueryCriteria(this, page);
  }
  
  protected void checkQueryOk() {
    if (orderProperty != null) {
      throw new ActivitiException("Invalid query: please call asc() or desc() after using orderByXX()");
    }
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
