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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


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
  protected String processDefinitionCategory;
  protected String processDefinitionName;
  protected Integer processDefinitionVersion;
  protected Set<String> processInstanceIds;
  protected String involvedUser;
  protected boolean includeProcessVariables;
  protected Integer processInstanceVariablesLimit;
  protected boolean withJobException;
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;
  protected String name;
  protected String nameLike;
  protected String nameLikeIgnoreCase;
  protected String locale;
  protected boolean withLocalizationFallback;
  protected List<HistoricProcessInstanceQueryImpl> orQueryObjects = new ArrayList<HistoricProcessInstanceQueryImpl>();
  protected HistoricProcessInstanceQueryImpl currentOrQueryObject = null;
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
      this.currentOrQueryObject.processInstanceId = processInstanceId;
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
      this.currentOrQueryObject.processInstanceIds = processInstanceIds;
    } else {
      this.processInstanceIds = processInstanceIds;
    }
    return this;
  }

  public HistoricProcessInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    if (inOrStatement) {
      this.currentOrQueryObject.processDefinitionId = processDefinitionId;
    } else {
      this.processDefinitionId = processDefinitionId;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery processDefinitionKey(String processDefinitionKey) {
    if (inOrStatement) {
      this.currentOrQueryObject.processDefinitionKey = processDefinitionKey;
    } else {
      this.processDefinitionKey = processDefinitionKey;
    }
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery processDefinitionKeyIn(List<String> processDefinitionKeys) {
    if (inOrStatement) {
      currentOrQueryObject.processDefinitionKeyIn = processDefinitionKeys;
    } else {
      this.processDefinitionKeyIn = processDefinitionKeys;
    }
    return this;
  }

  public HistoricProcessInstanceQuery processDefinitionCategory(String processDefinitionCategory) {
    if (inOrStatement) {
      this.currentOrQueryObject.processDefinitionCategory = processDefinitionCategory;
    } else {
      this.processDefinitionCategory = processDefinitionCategory;
    }
    return this;
  }

  public HistoricProcessInstanceQuery processDefinitionName(String processDefinitionName) {
    if (inOrStatement) {
      this.currentOrQueryObject.processDefinitionName = processDefinitionName;
    } else {
      this.processDefinitionName = processDefinitionName;
    }
    return this;
  }

  public HistoricProcessInstanceQuery processDefinitionVersion(Integer processDefinitionVersion) {
    if (inOrStatement) {
      this.currentOrQueryObject.processDefinitionVersion = processDefinitionVersion;
    } else {
      this.processDefinitionVersion = processDefinitionVersion;
    }
    return this;
  }

  public HistoricProcessInstanceQuery processInstanceBusinessKey(String businessKey) {
    if (inOrStatement) {
      this.currentOrQueryObject.businessKey = businessKey;
    } else {
      this.businessKey = businessKey;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery deploymentId(String deploymentId) {
    if (inOrStatement) {
      this.currentOrQueryObject.deploymentId = deploymentId;
    } else {
      this.deploymentId = deploymentId;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery deploymentIdIn(List<String> deploymentIds) {
    if (inOrStatement) {
      currentOrQueryObject.deploymentIds = deploymentIds;
    } else {
      this.deploymentIds = deploymentIds;
    }
    return this;
  }

  public HistoricProcessInstanceQuery finished() {
    if (inOrStatement) {
      this.currentOrQueryObject.finished = true;
    } else {
      this.finished = true;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery unfinished() {
    if (inOrStatement) {
      this.currentOrQueryObject.unfinished = true;
    } else {
      this.unfinished = true;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery deleted() {
    if (inOrStatement) {
      this.currentOrQueryObject.deleted = true;
    } else {
      this.deleted = true;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery notDeleted() {
    if (inOrStatement) {
      this.currentOrQueryObject.notDeleted = true;
    } else {
      this.notDeleted = true;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery startedBy(String startedBy) {
    if (inOrStatement) {
      this.currentOrQueryObject.startedBy = startedBy;
    } else {
      this.startedBy = startedBy;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery processDefinitionKeyNotIn(List<String> processDefinitionKeys) {
    if (inOrStatement) {
      this.currentOrQueryObject.processKeyNotIn = processDefinitionKeys;
    } else {
      this.processKeyNotIn = processDefinitionKeys;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery startedAfter(Date startedAfter) {
    if (inOrStatement) {
      this.currentOrQueryObject.startedAfter = startedAfter;
    } else {
      this.startedAfter = startedAfter;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery startedBefore(Date startedBefore) {
    if (inOrStatement) {
      this.currentOrQueryObject.startedBefore = startedBefore;
    } else {
      this.startedBefore = startedBefore;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery finishedAfter(Date finishedAfter) {
    if (inOrStatement) {
      this.currentOrQueryObject.finishedAfter = finishedAfter;
    } else {
      this.finishedAfter = finishedAfter;
      this.finished = true;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery finishedBefore(Date finishedBefore) {
    if (inOrStatement) {
      this.currentOrQueryObject.finishedBefore = finishedBefore;
    } else {
      this.finishedBefore = finishedBefore;
      this.finished = true;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery superProcessInstanceId(String superProcessInstanceId) {
    if (inOrStatement) {
      this.currentOrQueryObject.superProcessInstanceId = superProcessInstanceId;
    } else {
      this.superProcessInstanceId = superProcessInstanceId;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery excludeSubprocesses(boolean excludeSubprocesses) {
    if (inOrStatement) {
      this.currentOrQueryObject.excludeSubprocesses = excludeSubprocesses;
    } else {
      this.excludeSubprocesses = excludeSubprocesses;
    }
    return this;
  }
  
  @Override
  public HistoricProcessInstanceQuery involvedUser(String involvedUser) {
    if (inOrStatement) {
      this.currentOrQueryObject.involvedUser = involvedUser;
    } else {
      this.involvedUser = involvedUser;
    }
    return this;
  }
  
  public HistoricProcessInstanceQuery includeProcessVariables() {
    this.includeProcessVariables = true;
    return this;
  }
  
  public HistoricProcessInstanceQuery limitProcessInstanceVariables(Integer processInstanceVariablesLimit) {
    this.processInstanceVariablesLimit = processInstanceVariablesLimit;
    return this;
  }

  public Integer getProcessInstanceVariablesLimit() {
    return processInstanceVariablesLimit;
  }
  
  public HistoricProcessInstanceQuery withJobException() {
    this.withJobException = true;
    return this;
  }
  
  public HistoricProcessInstanceQuery processInstanceTenantId(String tenantId) {
  	if (tenantId == null) {
  		throw new ActivitiIllegalArgumentException("process instance tenant id is null");
  	}
  	if (inOrStatement) {
      this.currentOrQueryObject.tenantId = tenantId;
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
      this.currentOrQueryObject.tenantIdLike = tenantIdLike;
    } else {
      this.tenantIdLike = tenantIdLike;
    }
  	return this;
  }
  
  public HistoricProcessInstanceQuery processInstanceWithoutTenantId() {
    if (inOrStatement) {
      this.currentOrQueryObject.withoutTenantId = true;
    } else {
      this.withoutTenantId = true;
    }
  	return this;
  }
  
  @Override
  public HistoricProcessInstanceQuery processInstanceName(String name) {
    if (inOrStatement) {
      this.currentOrQueryObject.name = name;
    } else {
      this.name = name;
    }
    return this;
  }
  
  @Override
  public HistoricProcessInstanceQuery processInstanceNameLike(String nameLike) {
    if (inOrStatement) {
      this.currentOrQueryObject.nameLike = nameLike;
    } else {
      this.nameLike = nameLike;
    }
    return this;
  }
  
  @Override
  public HistoricProcessInstanceQuery processInstanceNameLikeIgnoreCase(String nameLikeIgnoreCase) {
    if (inOrStatement) {
      this.currentOrQueryObject.nameLikeIgnoreCase = nameLikeIgnoreCase.toLowerCase();
    } else {
      this.nameLikeIgnoreCase = nameLikeIgnoreCase.toLowerCase();
    }
    return this;
  }
  
  @Override
  public HistoricProcessInstanceQuery variableValueEquals(String variableName, Object variableValue) {
    if (inOrStatement) {
      currentOrQueryObject.variableValueEquals(variableName, variableValue, true);
      return this;
    } else {
      return variableValueEquals(variableName, variableValue, true);
    }
  }

  @Override
  public HistoricProcessInstanceQuery variableValueNotEquals(String variableName, Object variableValue) {
    if (inOrStatement) {
      currentOrQueryObject.variableValueNotEquals(variableName, variableValue, true);
      return this;
    } else {
      return variableValueNotEquals(variableName, variableValue, true);
    }
  }
  
  @Override
  public HistoricProcessInstanceQuery variableValueEquals(Object variableValue) {
    if (inOrStatement) {
      currentOrQueryObject.variableValueEquals(variableValue, true);
      return this;
    } else {
      return variableValueEquals(variableValue, true);
    }
  }
  
  @Override
  public HistoricProcessInstanceQuery variableValueEqualsIgnoreCase(String name, String value) {
    if (inOrStatement) {
      currentOrQueryObject.variableValueEqualsIgnoreCase(name, value, true);
      return this;
    } else {
      return variableValueEqualsIgnoreCase(name, value, true);
    }
  }
  
  @Override
  public HistoricProcessInstanceQuery variableValueNotEqualsIgnoreCase(String name, String value) {
    if (inOrStatement) {
      currentOrQueryObject.variableValueNotEqualsIgnoreCase(name, value, true);
      return this;
    } else {
      return variableValueNotEqualsIgnoreCase(name, value, true);
    }
  }
  
  @Override
  public HistoricProcessInstanceQuery variableValueGreaterThan(String name, Object value) {
    if (inOrStatement) {
      currentOrQueryObject.variableValueGreaterThan(name, value, true);
      return this;
    } else {
      return variableValueGreaterThan(name, value, true);
    } 
  }

  @Override
  public HistoricProcessInstanceQuery variableValueGreaterThanOrEqual(String name, Object value) {
    if (inOrStatement) {
      currentOrQueryObject.variableValueGreaterThanOrEqual(name, value, true);
      return this;
    } else {
      return variableValueGreaterThanOrEqual(name, value, true);
    }
  }

  @Override
  public HistoricProcessInstanceQuery variableValueLessThan(String name, Object value) {
    if (inOrStatement) {
      currentOrQueryObject.variableValueLessThan(name, value, true);
      return this;
    } else {
      return variableValueLessThan(name, value, true);
    }
  }

  @Override
  public HistoricProcessInstanceQuery variableValueLessThanOrEqual(String name, Object value) {
    if (inOrStatement) {
      currentOrQueryObject.variableValueLessThanOrEqual(name, value, true);
      return this;
    } else {
      return variableValueLessThanOrEqual(name, value, true);
    }
  }

  @Override
  public HistoricProcessInstanceQuery variableValueLike(String name, String value) {
    if (inOrStatement) {
      currentOrQueryObject.variableValueLike(name, value, true);
      return this;
    } else {
      return variableValueLike(name, value, true);
    }
  }
  
  @Override
  public HistoricProcessInstanceQuery variableValueLikeIgnoreCase(String name, String value) {
    if (inOrStatement) {
      currentOrQueryObject.variableValueLikeIgnoreCase(name, value, true);
      return this;
    } else {
      return variableValueLikeIgnoreCase(name, value, true);
    }
  }
  
  public HistoricProcessInstanceQuery locale(String locale) {
    this.locale = locale;
    return this;
  }
  
  public HistoricProcessInstanceQuery withLocalizationFallback() {
    withLocalizationFallback = true;
    return this;
  }
  
  public HistoricProcessInstanceQuery or() {
    if (inOrStatement) {
      throw new ActivitiException("the query is already in an or statement");
    }
    
    inOrStatement = true;
    currentOrQueryObject = new HistoricProcessInstanceQueryImpl();
    orQueryObjects.add(currentOrQueryObject);
    return this;
  }
  
  public HistoricProcessInstanceQuery endOr() {
    if (!inOrStatement) {
      throw new ActivitiException("endOr() can only be called after calling or()");
    }
    
    inOrStatement = false;
    currentOrQueryObject = null;
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
    List<HistoricProcessInstance> results = null;
    if (includeProcessVariables) {
      results = commandContext.getHistoricProcessInstanceEntityManager().findHistoricProcessInstancesAndVariablesByQueryCriteria(this);
    } else {
      results = commandContext.getHistoricProcessInstanceEntityManager().findHistoricProcessInstancesByQueryCriteria(this);
    }
    
    for (HistoricProcessInstance processInstance : results) {
      localize(processInstance, commandContext);
    }
    
    return results;
  }

  protected void localize(HistoricProcessInstance processInstance, CommandContext commandContext) {
    processInstance.setLocalizedName(null);
    processInstance.setLocalizedDescription(null);

    if (locale != null && processInstance.getProcessDefinitionId() != null) {
      ProcessDefinitionEntity processDefinition = commandContext.getProcessEngineConfiguration().getDeploymentManager().findDeployedProcessDefinitionById(processInstance.getProcessDefinitionId());
      ObjectNode languageNode = Context.getLocalizationElementProperties(locale, processDefinition.getKey(), 
          processInstance.getProcessDefinitionId(), withLocalizationFallback);
      
      if (languageNode != null) {
        JsonNode languageNameNode = languageNode.get(DynamicBpmnConstants.LOCALIZATION_NAME);
        if (languageNameNode != null && languageNameNode.isNull() == false) {
          processInstance.setLocalizedName(languageNameNode.asText());
        }

        JsonNode languageDescriptionNode = languageNode.get(DynamicBpmnConstants.LOCALIZATION_DESCRIPTION);
        if (languageDescriptionNode != null && languageDescriptionNode.isNull() == false) {
          processInstance.setLocalizedDescription(languageDescriptionNode.asText());
        }
      }
    }
  }
  
  @Override
  protected void ensureVariablesInitialized() {
    super.ensureVariablesInitialized();
    
    for (HistoricProcessInstanceQueryImpl orQueryObject : orQueryObjects) {
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
  public String getProcessDefinitionName() {
    return processDefinitionName;
  }
  public String getProcessDefinitionCategory() {
    return processDefinitionCategory;
  }
  public Integer getProcessDefinitionVersion() {
    return processDefinitionVersion;
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
  
  public boolean isWithException() {
    return withJobException;
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

  public List<HistoricProcessInstanceQueryImpl> getOrQueryObjects() {
    return orQueryObjects;
  }
}