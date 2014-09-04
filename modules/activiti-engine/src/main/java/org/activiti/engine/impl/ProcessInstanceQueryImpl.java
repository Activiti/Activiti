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

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Tijs Rademakers
 * @author Frederik Heremans
 * @author Falko Menge
 * @author Daniel Meyer
 */
public class ProcessInstanceQueryImpl extends AbstractVariableQueryImpl<ProcessInstanceQuery, ProcessInstance> implements ProcessInstanceQuery, Serializable {

  private static final long serialVersionUID = 1L;
  protected String executionId;
  protected String businessKey;
  protected boolean includeChildExecutionsWithBusinessKeyQuery;
  protected String processDefinitionId;
  protected String processDefinitionName;
  protected Set<String> processInstanceIds; 
  protected String processDefinitionKey;
  protected String deploymentId;
  protected List<String> deploymentIds;
  protected String superProcessInstanceId;
  protected String subProcessInstanceId;
  protected boolean excludeSubprocesses;
  protected String involvedUser;
  protected SuspensionState suspensionState;
  protected boolean includeProcessVariables;
  protected String name;
  protected String nameLike;
  protected String nameLikeIgnoreCase;
  
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;
  
  protected ProcessInstanceQueryImpl orQueryObject;
  protected boolean inOrStatement = false;
  
  // Unused, see dynamic query
  protected String activityId;
  protected List<EventSubscriptionQueryValue> eventSubscriptions;
  
  public ProcessInstanceQueryImpl() {
  }
  
  public ProcessInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }
  
  public ProcessInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public ProcessInstanceQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("Process instance id is null");
    }
    if (inOrStatement) {
      this.orQueryObject.executionId = processInstanceId;
    } else {
      this.executionId = processInstanceId;
    }
    return this;
  }
  
  public ProcessInstanceQuery processInstanceIds(Set<String> processInstanceIds) {
    if (processInstanceIds == null) {
      throw new ActivitiIllegalArgumentException("Set of process instance ids is null");
    }
    if (processInstanceIds.isEmpty()) {
      throw new ActivitiIllegalArgumentException("Set of process instance ids is empty");
    }
    
    if (inOrStatement) {
      this.orQueryObject.processInstanceIds = processInstanceIds;
    } else {
      this.processInstanceIds = processInstanceIds;
    }
    return this;
  }

  public ProcessInstanceQuery processInstanceBusinessKey(String businessKey) {
    if (businessKey == null) {
      throw new ActivitiIllegalArgumentException("Business key is null");
    }
    if (inOrStatement) {
      this.orQueryObject.businessKey = businessKey;
    } else {
      this.businessKey = businessKey;
    }
    return this;
  }
  
  public ProcessInstanceQuery processInstanceBusinessKey(String businessKey, String processDefinitionKey) {
    if (businessKey == null) {
      throw new ActivitiIllegalArgumentException("Business key is null");
    }
    if (inOrStatement) {
      throw new ActivitiIllegalArgumentException("This method is not supported in an OR statement");
    }
    
    this.businessKey = businessKey;
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }
  
  public ProcessInstanceQuery processInstanceTenantId(String tenantId) {
  	if (tenantId == null) {
  		throw new ActivitiIllegalArgumentException("process instance tenant id is null");
  	}
  	if (inOrStatement) {
      this.orQueryObject.tenantId = tenantId;
    } else {
      this.tenantId = tenantId;
    }
  	return this;
  }
  
  public ProcessInstanceQuery processInstanceTenantIdLike(String tenantIdLike) {
  	if (tenantIdLike == null) {
  		throw new ActivitiIllegalArgumentException("process instance tenant id is null");
  	}
  	if (inOrStatement) {
      this.orQueryObject.tenantIdLike = tenantIdLike;
    } else {
      this.tenantIdLike = tenantIdLike;
    }
  	return this;
  }
  
  public ProcessInstanceQuery processInstanceWithoutTenantId() {
    if (inOrStatement) {
      this.orQueryObject.withoutTenantId = true;
    } else {
      this.withoutTenantId = true;
    }
  	return this;
  }

  @Override
  public ProcessInstanceQuery processDefinitionName(String processDefinitionName) {
    if (processDefinitionName == null) {
      throw new ActivitiIllegalArgumentException("Process definition name is null");
    }
    
    if (inOrStatement) {
      this.orQueryObject.processDefinitionName = processDefinitionName;
    } else {
      this.processDefinitionName = processDefinitionName;
    }
    return this;
  }

  public ProcessInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ActivitiIllegalArgumentException("Process definition id is null");
    }
    
    if (inOrStatement) {
      this.orQueryObject.processDefinitionId = processDefinitionId;
    } else {
      this.processDefinitionId = processDefinitionId;
    }
    return this;
  }

  public ProcessInstanceQueryImpl processDefinitionKey(String processDefinitionKey) {
    if (processDefinitionKey == null) {
      throw new ActivitiIllegalArgumentException("Process definition key is null");
    }
    
    if (inOrStatement) {
      this.orQueryObject.processDefinitionKey = processDefinitionKey;
    } else {
      this.processDefinitionKey = processDefinitionKey;
    }
    return this;
  }
  
  public ProcessInstanceQueryImpl deploymentId(String deploymentId) {
    if (inOrStatement) {
      this.orQueryObject.deploymentId = deploymentId;
    } else {
      this.deploymentId = deploymentId;
    }
    return this;
  }
  
  public ProcessInstanceQueryImpl deploymentIdIn(List<String> deploymentIds) {
    if (inOrStatement) {
      this.orQueryObject.deploymentIds = deploymentIds;
    } else {
      this.deploymentIds = deploymentIds;
    }
    return this;
  }
  
  public ProcessInstanceQuery superProcessInstanceId(String superProcessInstanceId) {
    if (inOrStatement) {
      this.orQueryObject.superProcessInstanceId = superProcessInstanceId;
    } else {
      this.superProcessInstanceId = superProcessInstanceId;
    }
    return this;
  }
  
  public ProcessInstanceQuery subProcessInstanceId(String subProcessInstanceId) {
    if (inOrStatement) {
      this.orQueryObject.subProcessInstanceId = subProcessInstanceId;
    } else {
      this.subProcessInstanceId = subProcessInstanceId;
    }
    return this;
  }
  
  public ProcessInstanceQuery excludeSubprocesses(boolean excludeSubprocesses) {
    if (inOrStatement) {
      this.orQueryObject.excludeSubprocesses = excludeSubprocesses;
    } else {
      this.excludeSubprocesses = excludeSubprocesses;
    }
    return this;
  }
  
  public ProcessInstanceQuery involvedUser(String involvedUser) {
    if (involvedUser == null) {
      throw new ActivitiIllegalArgumentException("Involved user is null");
    }
    
    if (inOrStatement) {
      this.orQueryObject.involvedUser = involvedUser;
    } else {
      this.involvedUser = involvedUser;
    }
    return this;
  }
  
  public ProcessInstanceQuery active() {
    if (inOrStatement) {
      this.orQueryObject.suspensionState = SuspensionState.ACTIVE;
    } else {
      this.suspensionState = SuspensionState.ACTIVE;
    }
    return this;
  }
  
  public ProcessInstanceQuery suspended() {
    if (inOrStatement) {
      this.orQueryObject.suspensionState = SuspensionState.SUSPENDED;
    } else {
      this.suspensionState = SuspensionState.SUSPENDED;
    }
    return this;
  }
  
  public ProcessInstanceQuery includeProcessVariables() {
    this.includeProcessVariables = true;
    return this;
  }
  
  @Override
  public ProcessInstanceQuery processInstanceName(String name) {
    if (inOrStatement) {
      this.orQueryObject.name = name;
    } else {
      this.name = name;
    }
    return this;
  }
  
  @Override
  public ProcessInstanceQuery processInstanceNameLike(String nameLike) {
    if (inOrStatement) {
      this.orQueryObject.nameLike = nameLike;
    } else {
      this.nameLike = nameLike;
    }
    return this;
  }
  
  public ProcessInstanceQuery processInstanceNameLikeIgnoreCase(String nameLikeIgnoreCase) {
  	if (inOrStatement) {
      this.orQueryObject.nameLikeIgnoreCase = nameLikeIgnoreCase.toLowerCase();
    } else {
      this.nameLikeIgnoreCase = nameLikeIgnoreCase.toLowerCase();
    }
    return this;
  }
  
  public ProcessInstanceQuery or() {
    if (orQueryObject != null) {
      // only one OR statement is allowed
      throw new ActivitiException("Only one OR statement is allowed");
    } else {
      inOrStatement = true;
      orQueryObject = new ProcessInstanceQueryImpl();
    }
    return this;
  }
  
  public ProcessInstanceQuery endOr() {
    if (orQueryObject == null || inOrStatement == false) {
      throw new ActivitiException("OR statement hasn't started, so it can't be ended");
    } else {
      inOrStatement = false;
    }
    return this;
  }
  
  @Override
  public ProcessInstanceQuery variableValueEquals(String variableName, Object variableValue) {
    if (inOrStatement) {
      orQueryObject.variableValueEquals(variableName, variableValue, false);
      return this;
    } else {
      return variableValueEquals(variableName, variableValue, false);
    }
  }

  @Override
  public ProcessInstanceQuery variableValueNotEquals(String variableName, Object variableValue) {
    if (inOrStatement) {
      orQueryObject.variableValueNotEquals(variableName, variableValue, false);
      return this;
    } else {
      return variableValueNotEquals(variableName, variableValue, false);
    }
  }
  
  @Override
  public ProcessInstanceQuery variableValueEquals(Object variableValue) {
    if (inOrStatement) {
      orQueryObject.variableValueEquals(variableValue, false);
      return this;
    } else {
      return variableValueEquals(variableValue, false);
    }
  }
  
  @Override
  public ProcessInstanceQuery variableValueEqualsIgnoreCase(String name, String value) {
    if (inOrStatement) {
      orQueryObject.variableValueEqualsIgnoreCase(name, value, false);
      return this;
    } else {
      return variableValueEqualsIgnoreCase(name, value, false);
    }
  }
  
  @Override
  public ProcessInstanceQuery variableValueNotEqualsIgnoreCase(String name, String value) {
    if (inOrStatement) {
      orQueryObject.variableValueNotEqualsIgnoreCase(name, value, false);
      return this;
    } else {
      return variableValueNotEqualsIgnoreCase(name, value, false);
    }
  }
  
  @Override
  public ProcessInstanceQuery variableValueGreaterThan(String name, Object value) {
    if (inOrStatement) {
      orQueryObject.variableValueGreaterThan(name, value, false);
      return this;
    } else {
      return variableValueGreaterThan(name, value, false);
    } 
  }

  @Override
  public ProcessInstanceQuery variableValueGreaterThanOrEqual(String name, Object value) {
    if (inOrStatement) {
      orQueryObject.variableValueGreaterThanOrEqual(name, value, false);
      return this;
    } else {
      return variableValueGreaterThanOrEqual(name, value, false);
    }
  }

  @Override
  public ProcessInstanceQuery variableValueLessThan(String name, Object value) {
    if (inOrStatement) {
      orQueryObject.variableValueLessThan(name, value, false);
      return this;
    } else {
      return variableValueLessThan(name, value, false);
    }
  }

  @Override
  public ProcessInstanceQuery variableValueLessThanOrEqual(String name, Object value) {
    if (inOrStatement) {
      orQueryObject.variableValueLessThanOrEqual(name, value, false);
      return this;
    } else {
      return variableValueLessThanOrEqual(name, value, false);
    }
  }

  @Override
  public ProcessInstanceQuery variableValueLike(String name, String value) {
    if (inOrStatement) {
      orQueryObject.variableValueLike(name, value, false);
      return this;
    } else {
      return variableValueLike(name, value, false);
    }
  }

  public ProcessInstanceQuery orderByProcessInstanceId() {
    this.orderProperty = ProcessInstanceQueryProperty.PROCESS_INSTANCE_ID;
    return this;
  }
  
  public ProcessInstanceQuery orderByProcessDefinitionId() {
    this.orderProperty = ProcessInstanceQueryProperty.PROCESS_DEFINITION_ID;
    return this;
  }
  
  public ProcessInstanceQuery orderByProcessDefinitionKey() {
    this.orderProperty = ProcessInstanceQueryProperty.PROCESS_DEFINITION_KEY;
    return this;
  }
  
  public ProcessInstanceQuery orderByTenantId() {
    this.orderProperty = ProcessInstanceQueryProperty.TENANT_ID;
    return this;
  }
  
  public String getMssqlOrDB2OrderBy() {
    String specialOrderBy = super.getOrderBy();
    if (specialOrderBy != null && specialOrderBy.length() > 0) {
      specialOrderBy = specialOrderBy.replace("RES.", "TEMPRES_");
      specialOrderBy = specialOrderBy.replace("ProcessDefinitionKey", "TEMPP_KEY_");
      specialOrderBy = specialOrderBy.replace("ProcessDefinitionId", "TEMPP_ID_");
    }
    return specialOrderBy;
  }
  
  //results /////////////////////////////////////////////////////////////////
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getExecutionEntityManager()
      .findProcessInstanceCountByQueryCriteria(this);
  }

  public List<ProcessInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    if (includeProcessVariables) {
      return commandContext
          .getExecutionEntityManager()
          .findProcessInstanceAndVariablesByQueryCriteria(this);
    } else {
      return commandContext
          .getExecutionEntityManager()
          .findProcessInstanceByQueryCriteria(this);
    }
  }
  
  @Override
  protected void ensureVariablesInitialized() {
    super.ensureVariablesInitialized();
    
    if (orQueryObject != null) {
      orQueryObject.ensureVariablesInitialized();
    }
  }
  
  //getters /////////////////////////////////////////////////////////////////
  
  public boolean getOnlyProcessInstances() {
    return true; // See dynamic query in runtime.mapping.xml
  }
  public String getProcessInstanceId() {
    return executionId;
  }
  public Set<String> getProcessInstanceIds() {
    return processInstanceIds;
  }
  public String getBusinessKey() {
    return businessKey;
  }
  public boolean isIncludeChildExecutionsWithBusinessKeyQuery() {
    return includeChildExecutionsWithBusinessKeyQuery;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public String getProcessDefinitionName() {
    return processDefinitionName;
  }
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  public String getActivityId() {
    return null; // Unused, see dynamic query
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
  public String getInvolvedUser() {
    return involvedUser;
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
  public void setEventSubscriptions(List<EventSubscriptionQueryValue> eventSubscriptions) {
    this.eventSubscriptions = eventSubscriptions;
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
  
  public String getExecutionId() {
    return executionId;
  }

  public String getDeploymentId() {
    return deploymentId;
  }
  
  public List<String> getDeploymentIds() {
    return deploymentIds;
  }

  public boolean isIncludeProcessVariables() {
    return includeProcessVariables;
  }
  
  public String getNameLikeIgnoreCase() {
  	return nameLikeIgnoreCase;
  }

  public ProcessInstanceQueryImpl getOrQueryObject() {
    return orQueryObject;
  }

  /**
   * Method needed for ibatis because of re-use of query-xml for executions. ExecutionQuery contains
   * a parentId property.
   */
  public String getParentId() {
    return null;
  }
}
