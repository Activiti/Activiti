/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.engine.impl.persistence.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.BulkDeleteable;

/**



 */
public class HistoricProcessInstanceEntityImpl extends HistoricScopeInstanceEntityImpl implements HistoricProcessInstanceEntity, BulkDeleteable {

  private static final long serialVersionUID = 1L;

  protected String endActivityId;
  protected String businessKey;
  protected String startUserId;
  protected String startActivityId;
  protected String superProcessInstanceId;
  protected String tenantId = ProcessEngineConfiguration.NO_TENANT_ID;
  protected String name;
  protected String localizedName;
  protected String description;
  protected String localizedDescription;
  protected String processDefinitionKey;
  protected String processDefinitionName;
  protected Integer processDefinitionVersion;
  protected String deploymentId;
  protected List<HistoricVariableInstanceEntity> queryVariables;

  public HistoricProcessInstanceEntityImpl() {

  }

  public HistoricProcessInstanceEntityImpl(ExecutionEntity processInstance) {
    id = processInstance.getId();
    processInstanceId = processInstance.getId();
    name = processInstance.getName();
    businessKey = processInstance.getBusinessKey();
    processDefinitionId = processInstance.getProcessDefinitionId();
    processDefinitionKey = processInstance.getProcessDefinitionKey();
    processDefinitionName = processInstance.getProcessDefinitionName();
    processDefinitionVersion = processInstance.getProcessDefinitionVersion();
    deploymentId = processInstance.getDeploymentId();
    startTime = processInstance.getStartTime();
    startUserId = processInstance.getStartUserId();
    startActivityId = processInstance.getActivityId();
    superProcessInstanceId = processInstance.getSuperExecution() != null ? processInstance.getSuperExecution().getProcessInstanceId() : null;

    // Inherit tenant id (if applicable)
    if (processInstance.getTenantId() != null) {
      tenantId = processInstance.getTenantId();
    }
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = (Map<String, Object>) new HashMap<String, Object>();
    persistentState.put("endTime", endTime);
    persistentState.put("businessKey", businessKey);
    persistentState.put("name", name);
    persistentState.put("durationInMillis", durationInMillis);
    persistentState.put("deleteReason", deleteReason);
    persistentState.put("endStateName", endActivityId);
    persistentState.put("superProcessInstanceId", superProcessInstanceId);
    persistentState.put("processDefinitionId", processDefinitionId);
    persistentState.put("processDefinitionKey", processDefinitionKey);
    persistentState.put("processDefinitionName", processDefinitionName);
    persistentState.put("processDefinitionVersion", processDefinitionVersion);
    persistentState.put("deploymentId", deploymentId);
    return persistentState;
  }

  // getters and setters ////////////////////////////////////////////////////////

  public String getEndActivityId() {
    return endActivityId;
  }

  public void setEndActivityId(String endActivityId) {
    this.endActivityId = endActivityId;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getStartUserId() {
    return startUserId;
  }

  public void setStartUserId(String startUserId) {
    this.startUserId = startUserId;
  }

  public String getStartActivityId() {
    return startActivityId;
  }

  public void setStartActivityId(String startUserId) {
    this.startActivityId = startUserId;
  }

  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }

  public void setSuperProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getName() {
    if (localizedName != null && localizedName.length() > 0) {
      return localizedName;
    } else {
      return name;
    }
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLocalizedName() {
    return localizedName;
  }

  public void setLocalizedName(String localizedName) {
    this.localizedName = localizedName;
  }

  public String getDescription() {
    if (localizedDescription != null && localizedDescription.length() > 0) {
      return localizedDescription;
    } else {
      return description;
    }
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLocalizedDescription() {
    return localizedDescription;
  }

  public void setLocalizedDescription(String localizedDescription) {
    this.localizedDescription = localizedDescription;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  public void setProcessDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
  }

  public Integer getProcessDefinitionVersion() {
    return processDefinitionVersion;
  }

  public void setProcessDefinitionVersion(Integer processDefinitionVersion) {
    this.processDefinitionVersion = processDefinitionVersion;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public Map<String, Object> getProcessVariables() {
    Map<String, Object> variables = new HashMap<String, Object>();
    if (queryVariables != null) {
      for (HistoricVariableInstanceEntity variableInstance : queryVariables) {
        if (variableInstance.getId() != null && variableInstance.getTaskId() == null) {
          variables.put(variableInstance.getName(), variableInstance.getValue());
        }
      }
    }
    return variables;
  }

  public List<HistoricVariableInstanceEntity> getQueryVariables() {
    if (queryVariables == null && Context.getCommandContext() != null) {
      queryVariables = new HistoricVariableInitializingList();
    }
    return queryVariables;
  }

  public void setQueryVariables(List<HistoricVariableInstanceEntity> queryVariables) {
    this.queryVariables = queryVariables;
  }

  // common methods //////////////////////////////////////////////////////////

  @Override
  public String toString() {
    return "HistoricProcessInstanceEntity[superProcessInstanceId=" + superProcessInstanceId + "]";
  }
}
