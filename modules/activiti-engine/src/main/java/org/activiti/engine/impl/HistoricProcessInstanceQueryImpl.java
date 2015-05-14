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

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * @author Tom Baeyens
 * @author Tijs Rademakers
 * @author Falko Menge
 * @author Bernd Ruecker
 * @author Joram Barrez
 */
public class HistoricProcessInstanceQueryImpl extends AbstractVariableQueryImpl<HistoricProcessInstanceQuery, HistoricProcessInstance> implements HistoricProcessInstanceQuery {

  private static final long serialVersionUID = 1L;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String businessKey;
  protected String deploymentId;
  protected List<String> deploymentIds;
  protected boolean finished = false;
  protected boolean unfinished = false;
  protected boolean deleted = false;
  protected boolean notDeleted = false;
  protected String startedBy;
  protected String superProcessInstanceId;
  protected boolean excludeSubprocesses;
  protected List<String> processDefinitionKeyIn;
  protected List<String> processKeyNotIn;
  protected Date startedBefore;
  protected Date startedAfter;
  protected Date finishedBefore;
  protected Date finishedAfter;
  protected String processDefinitionKey;
  protected Set<String> processInstanceIds;
  protected String involvedUser;
  protected boolean includeProcessVariables;
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;
  protected String name;
  protected String nameLike;
  protected String nameLikeIgnoreCase;
  protected HistoricProcessInstanceQueryImpl orQueryObject;
  protected boolean inOrStatement = false;
  
  public HistoricProcessInstanceQueryImpl() {
  }
  
  public HistoricProcessInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }
  
  public HistoricProcessInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricProcessInstanceQueryImpl processInstanceId(String processInstanceId) {
    if (inOrStatement) {
      this.orQueryObject.processInstanceId = processInstanceId;
    } else {
      this.processInstanceId = processInstanceId;
    }
    return this;
  }

  public HistoricProcessInstanceQuery processInstanceIds(Set<String> processInstanceIds) {
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

  public HistoricProcessInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    if (inOrStatement) {
      this.orQueryObject.processDefinitionId = processDefinitionId;
    } else {
      this.processDefinitionId = processDefinitionId;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery processDefinitionKey(String processDefinitionKey) {
    if (inOrStatement) {
      this.orQueryObject.processDefinitionKey = processDefinitionKey;
    } else {
      this.processDefinitionKey = processDefinitionKey;
    }
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery processDefinitionKeyIn(List<String> processDefinitionKeys) {
    if (inOrStatement) {
      orQueryObject.processDefinitionKeyIn = processDefinitionKeys;
    } else {
      this.processDefinitionKeyIn = processDefinitionKeys;
    }
    return this;
  }

  public HistoricProcessInstanceQuery processInstanceBusinessKey(String businessKey) {
    if (inOrStatement) {
      this.orQueryObject.businessKey = businessKey;
    } else {
      this.businessKey = businessKey;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery deploymentId(String deploymentId) {
    if (inOrStatement) {
      this.orQueryObject.deploymentId = deploymentId;
    } else {
      this.deploymentId = deploymentId;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery deploymentIdIn(List<String> deploymentIds) {
    if (inOrStatement) {
      orQueryObject.deploymentIds = deploymentIds;
    } else {
      this.deploymentIds = deploymentIds;
    }
    return this;
  }

  public HistoricProcessInstanceQuery finished() {
    if (inOrStatement) {
      this.orQueryObject.finished = true;
    } else {
      this.finished = true;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery unfinished() {
    if (inOrStatement) {
      this.orQueryObject.unfinished = true;
    } else {
      this.unfinished = true;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery deleted() {
    if (inOrStatement) {
      this.orQueryObject.deleted = true;
    } else {
      this.deleted = true;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery notDeleted() {
    if (inOrStatement) {
      this.orQueryObject.notDeleted = true;
    } else {
      this.notDeleted = true;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery startedBy(String startedBy) {
    if (inOrStatement) {
      this.orQueryObject.startedBy = startedBy;
    } else {
      this.startedBy = startedBy;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery processDefinitionKeyNotIn(List<String> processDefinitionKeys) {
    if (inOrStatement) {
      this.orQueryObject.processKeyNotIn = processDefinitionKeys;
    } else {
      this.processKeyNotIn = processDefinitionKeys;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery startedAfter(Date startedAfter) {
    if (inOrStatement) {
      this.orQueryObject.startedAfter = startedAfter;
    } else {
      this.startedAfter = startedAfter;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery startedBefore(Date startedBefore) {
    if (inOrStatement) {
      this.orQueryObject.startedBefore = startedBefore;
    } else {
      this.startedBefore = startedBefore;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery finishedAfter(Date finishedAfter) {
    if (inOrStatement) {
      this.orQueryObject.finishedAfter = finishedAfter;
    } else {
      this.finishedAfter = finishedAfter;
      this.finished = true;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery finishedBefore(Date finishedBefore) {
    if (inOrStatement) {
      this.orQueryObject.finishedBefore = finishedBefore;
    } else {
      this.finishedBefore = finishedBefore;
      this.finished = true;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery superProcessInstanceId(String superProcessInstanceId) {
    if (inOrStatement) {
      this.orQueryObject.superProcessInstanceId = superProcessInstanceId;
    } else {
      this.superProcessInstanceId = superProcessInstanceId;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery excludeSubprocesses(boolean excludeSubprocesses) {
    if (inOrStatement) {
      this.orQueryObject.excludeSubprocesses = excludeSubprocesses;
    } else {
      this.excludeSubprocesses = excludeSubprocesses;
    }
    return this;
  }
  
  @Override
  public HistoricProcessInstanceQuery involvedUser(String involvedUser) {
    if (inOrStatement) {
      this.orQueryObject.involvedUser = involvedUser;
    } else {
      this.involvedUser = involvedUser;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery includeProcessVariables() {
    this.includeProcessVariables = true;
    return this;
  }
  
  public HistoricProcessInstanceQuery processInstanceTenantId(String tenantId) {
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
  
  public HistoricProcessInstanceQuery processInstanceTenantIdLike(String tenantIdLike) {
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
  
  public HistoricProcessInstanceQuery processInstanceWithoutTenantId() {
    if (inOrStatement) {
      this.orQueryObject.withoutTenantId = true;
    } else {
      this.withoutTenantId = true;
    }
  	return this;
  }
  
  @Override
  public HistoricProcessInstanceQuery processInstanceName(String name) {
    if (inOrStatement) {
      this.orQueryObject.name = name;
    } else {
      this.name = name;
    }
    return this;
  }
  
  @Override
  public HistoricProcessInstanceQuery processInstanceNameLike(String nameLike) {
    if (inOrStatement) {
      this.orQueryObject.nameLike = nameLike;
    } else {
      this.nameLike = nameLike;
    }
    return this;
  }
  
  @Override
  public HistoricProcessInstanceQuery processInstanceNameLikeIgnoreCase(String nameLikeIgnoreCase) {
    if (inOrStatement) {
      this.orQueryObject.nameLikeIgnoreCase = nameLikeIgnoreCase.toLowerCase();
    } else {
      this.nameLikeIgnoreCase = nameLikeIgnoreCase.toLowerCase();
    }
    return this;
  }
  
  @Override
  public HistoricProcessInstanceQuery variableValueEquals(String variableName, Object variableValue) {
    if (inOrStatement) {
      orQueryObject.variableValueEquals(variableName, variableValue, true);
      return this;
    } else {
      return variableValueEquals(variableName, variableValue, true);
    }
  }

  @Override
  public HistoricProcessInstanceQuery variableValueNotEquals(String variableName, Object variableValue) {
    if (inOrStatement) {
      orQueryObject.variableValueNotEquals(variableName, variableValue, true);
      return this;
    } else {
      return variableValueNotEquals(variableName, variableValue, true);
    }
  }
  
  @Override
  public HistoricProcessInstanceQuery variableValueEquals(Object variableValue) {
    if (inOrStatement) {
      orQueryObject.variableValueEquals(variableValue, true);
      return this;
    } else {
      return variableValueEquals(variableValue, true);
    }
  }
  
  @Override
  public HistoricProcessInstanceQuery variableValueEqualsIgnoreCase(String name, String value) {
    if (inOrStatement) {
      orQueryObject.variableValueEqualsIgnoreCase(name, value, true);
      return this;
    } else {
      return variableValueEqualsIgnoreCase(name, value, true);
    }
  }
  
  @Override
  public HistoricProcessInstanceQuery variableValueNotEqualsIgnoreCase(String name, String value) {
    if (inOrStatement) {
      orQueryObject.variableValueNotEqualsIgnoreCase(name, value, true);
      return this;
    } else {
      return variableValueNotEqualsIgnoreCase(name, value, true);
    }
  }
  
  @Override
  public HistoricProcessInstanceQuery variableValueGreaterThan(String name, Object value) {
    if (inOrStatement) {
      orQueryObject.variableValueGreaterThan(name, value, true);
      return this;
    } else {
      return variableValueGreaterThan(name, value, true);
    } 
  }

  @Override
  public HistoricProcessInstanceQuery variableValueGreaterThanOrEqual(String name, Object value) {
    if (inOrStatement) {
      orQueryObject.variableValueGreaterThanOrEqual(name, value, true);
      return this;
    } else {
      return variableValueGreaterThanOrEqual(name, value, true);
    }
  }

  @Override
  public HistoricProcessInstanceQuery variableValueLessThan(String name, Object value) {
    if (inOrStatement) {
      orQueryObject.variableValueLessThan(name, value, true);
      return this;
    } else {
      return variableValueLessThan(name, value, true);
    }
  }

  @Override
  public HistoricProcessInstanceQuery variableValueLessThanOrEqual(String name, Object value) {
    if (inOrStatement) {
      orQueryObject.variableValueLessThanOrEqual(name, value, true);
      return this;
    } else {
      return variableValueLessThanOrEqual(name, value, true);
    }
  }

  @Override
  public HistoricProcessInstanceQuery variableValueLike(String name, String value) {
    if (inOrStatement) {
      orQueryObject.variableValueLike(name, value, true);
      return this;
    } else {
      return variableValueLike(name, value, true);
    }
  }
  
  public HistoricProcessInstanceQuery or() {
    if (orQueryObject != null) {
      // only one OR statement is allowed
      throw new ActivitiException("Only one OR statement is allowed");
    } else {
      inOrStatement = true;
      orQueryObject = new HistoricProcessInstanceQueryImpl();
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery endOr() {
    if (orQueryObject == null || inOrStatement == false) {
      throw new ActivitiException("OR statement hasn't started, so it can't be ended");
    } else {
      inOrStatement = false;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery orderByProcessInstanceBusinessKey() {
    return orderBy(HistoricProcessInstanceQueryProperty.BUSINESS_KEY);
  }
  
  public HistoricProcessInstanceQuery orderByProcessInstanceDuration() {
    return orderBy(HistoricProcessInstanceQueryProperty.DURATION);
  }
  
  public HistoricProcessInstanceQuery orderByProcessInstanceStartTime() {
    return orderBy(HistoricProcessInstanceQueryProperty.START_TIME);
  }
  
  public HistoricProcessInstanceQuery orderByProcessInstanceEndTime() {
    return orderBy(HistoricProcessInstanceQueryProperty.END_TIME);
  }
  
  public HistoricProcessInstanceQuery orderByProcessDefinitionId() {
    return orderBy(HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
  }
  
  public HistoricProcessInstanceQuery orderByProcessInstanceId() {
    return orderBy(HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_);
  }
  
  public HistoricProcessInstanceQuery orderByTenantId() {
  	return orderBy(HistoricProcessInstanceQueryProperty.TENANT_ID);
  }
  
  public String getMssqlOrDB2OrderBy() {
    String specialOrderBy = super.getOrderBy();
    if (specialOrderBy != null && specialOrderBy.length() > 0) {
      specialOrderBy = specialOrderBy.replace("RES.", "TEMPRES_");
      specialOrderBy = specialOrderBy.replace("VAR.", "TEMPVAR_");
    }
    return specialOrderBy;
  }
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getHistoricProcessInstanceEntityManager()
      .findHistoricProcessInstanceCountByQueryCriteria(this);
  }

  public List<HistoricProcessInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    if (includeProcessVariables) {
      return commandContext
          .getHistoricProcessInstanceEntityManager()
          .findHistoricProcessInstancesAndVariablesByQueryCriteria(this);
    } else {
      return commandContext
          .getHistoricProcessInstanceEntityManager()
          .findHistoricProcessInstancesByQueryCriteria(this);
    }
  }
  
  @Override
  protected void ensureVariablesInitialized() {
    super.ensureVariablesInitialized();
    
    if (orQueryObject != null) {
      orQueryObject.ensureVariablesInitialized();
    }
  }

  @Override
  protected void checkQueryOk() {
    super.checkQueryOk();
    
    if(includeProcessVariables) {
    	this.orderBy(HistoricProcessInstanceQueryProperty.INCLUDED_VARIABLE_TIME).asc();
    }
  }
  
  public String getBusinessKey() {
    return businessKey;
  }
  public boolean isOpen() {
    return unfinished;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  public List<String> getProcessDefinitionKeyIn() {
    return processDefinitionKeyIn;
  }
  public String getProcessDefinitionIdLike() {
    return processDefinitionKey + ":%:%";
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public Set<String> getProcessInstanceIds() {
    return processInstanceIds;
  }
  public String getStartedBy() {
    return startedBy;
  }
  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }
  public boolean isExcludeSubprocesses() {
    return excludeSubprocesses;
  }
  public List<String> getProcessKeyNotIn() {
    return processKeyNotIn;
  }
  public Date getStartedAfter() {
    return startedAfter;
  }
  public Date getStartedBefore() {
    return startedBefore;
  }
  public Date getFinishedAfter() {
    return finishedAfter;
  }
  public Date getFinishedBefore() {
    return finishedBefore;
  }
  public String getInvolvedUser() {
    return involvedUser;
  }
  public String getName() {
    return name;
  }
  public String getNameLike() {
    return nameLike;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public String getDeploymentId() {
    return deploymentId;
  }
  
  public List<String> getDeploymentIds() {
    return deploymentIds;
  }

  public boolean isFinished() {
    return finished;
  }

  public boolean isUnfinished() {
    return unfinished;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public boolean isNotDeleted() {
    return notDeleted;
  }

  public boolean isIncludeProcessVariables() {
    return includeProcessVariables;
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
  
  public String getNameLikeIgnoreCase() {
		return nameLikeIgnoreCase;
	}

	public HistoricProcessInstanceQueryImpl getOrQueryObject() {
    return orQueryObject;
  }
}