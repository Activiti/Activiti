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
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;


/**
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Daniel Meyer
 */
public class ExecutionQueryImpl extends AbstractVariableQueryImpl<ExecutionQuery, Execution> 
  implements ExecutionQuery {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processDefinitionName;
  protected String activityId;
  protected String executionId;
  protected String parentId;
  protected String processInstanceId;
  protected List<EventSubscriptionQueryValue> eventSubscriptions;
  
  // Not used by end-users, but needed for dynamic ibatis query
  protected String superProcessInstanceId;
  protected String subProcessInstanceId;
  protected boolean excludeSubprocesses;
  protected SuspensionState suspensionState;
  protected String businessKey;
  protected boolean includeChildExecutionsWithBusinessKeyQuery;
  protected boolean isActive;
  protected String involvedUser;
  
  
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
      throw new ActivitiIllegalArgumentException("Process definition id is null");
    }
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public ExecutionQueryImpl processDefinitionKey(String processDefinitionKey) {
    if (processDefinitionKey == null) {
      throw new ActivitiIllegalArgumentException("Process definition key is null");
    }
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  @Override
  public ExecutionQuery processDefinitionName(String processDefinitionName) {
    if (processDefinitionName == null) {
      throw new ActivitiIllegalArgumentException("Process definition name is null");
    }
    this.processDefinitionName = processDefinitionName;
    return this;
  }

  public ExecutionQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("Process instance id is null");
    }
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public ExecutionQuery processInstanceBusinessKey(String businessKey) {
    if (businessKey == null) {
      throw new ActivitiIllegalArgumentException("Business key is null");
    }
    this.businessKey = businessKey;
    return this;
  }
  
  public ExecutionQuery processInstanceBusinessKey(String processInstanceBusinessKey, boolean includeChildExecutions) {
    if (!includeChildExecutions) {
      return processInstanceBusinessKey(processInstanceBusinessKey);
    } else {
      if (processInstanceBusinessKey == null) {
        throw new ActivitiIllegalArgumentException("Business key is null");
      }
      this.businessKey = processInstanceBusinessKey;
      this.includeChildExecutionsWithBusinessKeyQuery = includeChildExecutions;
      return this;
    }
  }
  
  public ExecutionQueryImpl executionId(String executionId) {
    if (executionId == null) {
      throw new ActivitiIllegalArgumentException("Execution id is null");
    }
    this.executionId = executionId;
    return this;
  }
  
  public ExecutionQueryImpl activityId(String activityId) {
    this.activityId = activityId;
    
    if (activityId != null) {
      isActive =  true;
    }
    return this;
  }
  
  public ExecutionQueryImpl parentId(String parentId) {
    if (parentId == null) {
      throw new ActivitiIllegalArgumentException("Parent id is null");
    }
    this.parentId = parentId;
    return this;
  }
  
  public ExecutionQuery signalEventSubscription(String signalName) {    
    return eventSubscription("signal", signalName);
  }
  
  public ExecutionQuery signalEventSubscriptionName(String signalName) {    
    return eventSubscription("signal", signalName);
  }  
  
  public ExecutionQuery messageEventSubscriptionName(String messageName) {    
    return eventSubscription("message", messageName);
  } 
  
  public ExecutionQuery eventSubscription(String eventType, String eventName) {
    if(eventName == null) {
      throw new ActivitiIllegalArgumentException("event name is null");
    }
    if(eventType == null) {
      throw new ActivitiIllegalArgumentException("event type is null");
    }
    if(eventSubscriptions == null) {
      eventSubscriptions = new ArrayList<EventSubscriptionQueryValue>();
    }
    eventSubscriptions.add(new EventSubscriptionQueryValue(eventName, eventType));
    return this;
  }
  
  public ExecutionQuery processVariableValueEquals(String variableName, Object variableValue) {
    return variableValueEquals(variableName, variableValue, false);
  }

  public ExecutionQuery processVariableValueEquals(Object variableValue) {
    return variableValueEquals(variableValue, false);
  }

  public ExecutionQuery processVariableValueNotEquals(String variableName, Object variableValue) {
    return variableValueNotEquals(variableName, variableValue, false);
  }

  public ExecutionQuery processVariableValueEqualsIgnoreCase(String name, String value) {
    return variableValueEqualsIgnoreCase(name, value, false);
  }

  @Override
  public ExecutionQuery processVariableValueNotEqualsIgnoreCase(String name, String value) {
    return variableValueNotEqualsIgnoreCase(name, value, false);
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
      .getExecutionEntityManager()
      .findExecutionCountByQueryCriteria(this);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<Execution> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    return (List) commandContext
      .getExecutionEntityManager()
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
  public String getProcessDefinitionName() {
    return processDefinitionName;
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
    return businessKey;
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
  public boolean isExcludeSubprocesses() {
    return excludeSubprocesses;
  }
  public SuspensionState getSuspensionState() {
    return suspensionState;
  }  
  public void setSuspensionState(SuspensionState suspensionState) {
    this.suspensionState = suspensionState;
  }  
  public List<EventSubscriptionQueryValue> getEventSubscriptions() {
    return eventSubscriptions;
  }
  public boolean isIncludeChildExecutionsWithBusinessKeyQuery() {
    return includeChildExecutionsWithBusinessKeyQuery;
  }

  public void setEventSubscriptions(List<EventSubscriptionQueryValue> eventSubscriptions) {
    this.eventSubscriptions = eventSubscriptions;
  }
  public boolean isActive() {
    return isActive;
  }
  public String getInvolvedUser() {
    return involvedUser;
  }
  public void setInvolvedUser(String involvedUser) {
    this.involvedUser = involvedUser;
  }
  public String getParentId() {
    return parentId;
  }
  
}
