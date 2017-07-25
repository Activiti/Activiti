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
import java.util.Set;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**



 */
public class ExecutionQueryImpl extends AbstractVariableQueryImpl<ExecutionQuery, Execution> implements ExecutionQuery {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processDefinitionCategory;
  protected String processDefinitionName;
  protected Integer processDefinitionVersion;
  protected String activityId;
  protected String executionId;
  protected String parentId;
  protected boolean onlyChildExecutions;
  protected boolean onlySubProcessExecutions;
  protected boolean onlyProcessInstanceExecutions;
  protected String processInstanceId;
  protected String rootProcessInstanceId;
  protected List<EventSubscriptionQueryValue> eventSubscriptions;

  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;
  protected String locale;
  protected boolean withLocalizationFallback;

  protected Date startedBefore;
  protected Date startedAfter;
  protected String startedBy;

  // Not used by end-users, but needed for dynamic ibatis query
  protected String superProcessInstanceId;
  protected String subProcessInstanceId;
  protected boolean excludeSubprocesses;
  protected SuspensionState suspensionState;
  protected String businessKey;
  protected boolean includeChildExecutionsWithBusinessKeyQuery;
  protected boolean isActive;
  protected String involvedUser;
  protected Set<String> processDefinitionKeys;
  protected Set<String> processDefinitionIds;

  // Not exposed in API, but here for the ProcessInstanceQuery support, since
  // the name lives on the
  // Execution entity/table
  protected String name;
  protected String nameLike;
  protected String nameLikeIgnoreCase;
  protected String deploymentId;
  protected List<String> deploymentIds;
  protected List<ExecutionQueryImpl> orQueryObjects = new ArrayList<ExecutionQueryImpl>();
  
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
  public ExecutionQuery processDefinitionCategory(String processDefinitionCategory) {
    if (processDefinitionCategory == null) {
      throw new ActivitiIllegalArgumentException("Process definition category is null");
    }
    this.processDefinitionCategory = processDefinitionCategory;
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
  
  @Override
  public ExecutionQuery processDefinitionVersion(Integer processDefinitionVersion) {
    if (processDefinitionVersion == null) {
      throw new ActivitiIllegalArgumentException("Process definition version is null");
    }
    this.processDefinitionVersion = processDefinitionVersion;
    return this;
  }

  public ExecutionQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("Process instance id is null");
    }
    this.processInstanceId = processInstanceId;
    return this;
  }

  public ExecutionQueryImpl rootProcessInstanceId(String rootProcessInstanceId) {
    if (rootProcessInstanceId == null) {
      throw new ActivitiIllegalArgumentException("Root process instance id is null");
    }
    this.rootProcessInstanceId = rootProcessInstanceId;
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
  
  public ExecutionQuery processDefinitionKeys(Set<String> processDefinitionKeys) {
    if (processDefinitionKeys == null) {
      throw new ActivitiIllegalArgumentException("Process definition keys is null");
    }
    this.processDefinitionKeys = processDefinitionKeys;
    return this;
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
      isActive = true;
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

  public ExecutionQuery onlyChildExecutions() {
    this.onlyChildExecutions = true;
    return this;
  }

  public ExecutionQuery onlySubProcessExecutions() {
    this.onlySubProcessExecutions = true;
    return this;
  }

  public ExecutionQuery onlyProcessInstanceExecutions() {
    this.onlyProcessInstanceExecutions = true;
    return this;
  }

  public ExecutionQueryImpl executionTenantId(String tenantId) {
    if (tenantId == null) {
      throw new ActivitiIllegalArgumentException("execution tenant id is null");
    }
    this.tenantId = tenantId;
    return this;
  }

  public ExecutionQueryImpl executionTenantIdLike(String tenantIdLike) {
    if (tenantIdLike == null) {
      throw new ActivitiIllegalArgumentException("execution tenant id is null");
    }
    this.tenantIdLike = tenantIdLike;
    return this;
  }

  public ExecutionQueryImpl executionWithoutTenantId() {
    this.withoutTenantId = true;
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
    if (eventName == null) {
      throw new ActivitiIllegalArgumentException("event name is null");
    }
    if (eventType == null) {
      throw new ActivitiIllegalArgumentException("event type is null");
    }
    if (eventSubscriptions == null) {
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

  public ExecutionQuery processVariableValueNotEqualsIgnoreCase(String name, String value) {
    return variableValueNotEqualsIgnoreCase(name, value, false);
  }
  
  public ExecutionQuery processVariableValueLike(String name, String value) {
    return variableValueLike(name, value, false);
  }
  
  public ExecutionQuery processVariableValueLikeIgnoreCase(String name, String value) {
    return variableValueLikeIgnoreCase(name, value, false);
  }

  @Override
  public ExecutionQuery locale(String locale) {
    this.locale = locale;
    return this;
  }

  public ExecutionQuery withLocalizationFallback() {
    withLocalizationFallback = true;
    return this;
  }

  public ExecutionQuery startedBefore(Date beforeTime) {
    if (beforeTime == null) {
      throw new ActivitiIllegalArgumentException("before time is null");
    }
    this.startedBefore = beforeTime;

    return this;
  }

  public ExecutionQuery startedAfter(Date afterTime) {
    if (afterTime == null) {
      throw new ActivitiIllegalArgumentException("after time is null");
    }
    this.startedAfter = afterTime;

    return this;
  }

  public ExecutionQuery startedBy(String userId) {
    if (userId == null) {
      throw new ActivitiIllegalArgumentException("user id is null");
    }
    this.startedBy = userId;

    return this;
  }
  
  // ordering ////////////////////////////////////////////////////

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

  public ExecutionQueryImpl orderByTenantId() {
    this.orderProperty = ExecutionQueryProperty.TENANT_ID;
    return this;
  }

  // results ////////////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext.getExecutionEntityManager().findExecutionCountByQueryCriteria(this);
  }

  @SuppressWarnings({ "unchecked" })
  public List<Execution> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    List<?> executions = commandContext.getExecutionEntityManager().findExecutionsByQueryCriteria(this, page);
    
    if (Context.getProcessEngineConfiguration().getPerformanceSettings().isEnableLocalization()) {
      for (ExecutionEntity execution : (List<ExecutionEntity>) executions) {
        String activityId = null;
        if (execution.getId().equals(execution.getProcessInstanceId())) {
          if (execution.getProcessDefinitionId() != null) {
            ProcessDefinition processDefinition = commandContext.getProcessEngineConfiguration()
                .getDeploymentManager()
                .findDeployedProcessDefinitionById(execution.getProcessDefinitionId());
            activityId = processDefinition.getKey();
          }
          
        } else {
          activityId = execution.getActivityId();
        }
  
        if (activityId != null) {
          localize(execution, activityId);
        }
      }
    }

    return (List<Execution>) executions;
  }
  
  protected void localize(Execution execution, String activityId) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    executionEntity.setLocalizedName(null);
    executionEntity.setLocalizedDescription(null);

    String processDefinitionId = executionEntity.getProcessDefinitionId();
    if (locale != null && processDefinitionId != null) {
      ObjectNode languageNode = Context.getLocalizationElementProperties(locale, activityId, processDefinitionId, withLocalizationFallback);
      if (languageNode != null) {
        JsonNode languageNameNode = languageNode.get(DynamicBpmnConstants.LOCALIZATION_NAME);
        if (languageNameNode != null && !languageNameNode.isNull()) {
          executionEntity.setLocalizedName(languageNameNode.asText());
        }

        JsonNode languageDescriptionNode = languageNode.get(DynamicBpmnConstants.LOCALIZATION_DESCRIPTION);
        if (languageDescriptionNode != null && !languageDescriptionNode.isNull()) {
          executionEntity.setLocalizedDescription(languageDescriptionNode.asText());
        }
      }
    }
  }

  // getters ////////////////////////////////////////////////////

  public boolean getOnlyProcessInstances() {
    return false;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  
  public String getProcessDefinitionCategory() {
    return processDefinitionCategory;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }
  
  public Integer getProcessDefinitionVersion() {
    return processDefinitionVersion;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
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

  public Set<String> getProcessDefinitionIds() {
    return processDefinitionIds;
  }

  public Set<String> getProcessDefinitionKeys() {
    return processDefinitionKeys;
  }

  public String getParentId() {
    return parentId;
  }

  public boolean isOnlyChildExecutions() {
    return onlyChildExecutions;
  }

  public boolean isOnlySubProcessExecutions() {
    return onlySubProcessExecutions;
  }

  public boolean isOnlyProcessInstanceExecutions() {
    return onlyProcessInstanceExecutions;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getTenantIdLike() {
    return tenantIdLike;
  }

  public boolean isWithoutTenantId() {
    return withoutTenantId;
  }

  public String getName() {
    return name;
  }

  public String getNameLike() {
    return nameLike;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNameLike(String nameLike) {
    this.nameLike = nameLike;
  }

  public String getNameLikeIgnoreCase() {
    return nameLikeIgnoreCase;
  }

  public void setNameLikeIgnoreCase(String nameLikeIgnoreCase) {
    this.nameLikeIgnoreCase = nameLikeIgnoreCase;
  }

  public Date getStartedBefore() {
    return startedBefore;
  }

  public void setStartedBefore(Date startedBefore) {
    this.startedBefore = startedBefore;
  }

  public Date getStartedAfter() {
    return startedAfter;
  }

  public void setStartedAfter(Date startedAfter) {
    this.startedAfter = startedAfter;
  }

  public String getStartedBy() {
    return startedBy;
  }

  public void setStartedBy(String startedBy) {
    this.startedBy = startedBy;
  }
}
