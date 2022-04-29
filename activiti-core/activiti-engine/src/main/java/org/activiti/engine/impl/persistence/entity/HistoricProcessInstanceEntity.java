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

import java.util.List;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.history.HistoricProcessInstance;


@Internal
public interface HistoricProcessInstanceEntity extends HistoricScopeInstanceEntity, HistoricProcessInstance {

  void setEndActivityId(String endActivityId);

  void setBusinessKey(String businessKey);

  void setStartUserId(String startUserId);

  void setStartActivityId(String startUserId);

  void setSuperProcessInstanceId(String superProcessInstanceId);

  void setTenantId(String tenantId);

  void setName(String name);

  void setLocalizedName(String localizedName);

  void setDescription(String description);

  void setLocalizedDescription(String localizedDescription);

  String getProcessDefinitionKey();

  void setProcessDefinitionKey(String processDefinitionKey);

  String getProcessDefinitionName();

  void setProcessDefinitionName(String processDefinitionName);

  Integer getProcessDefinitionVersion();

  void setProcessDefinitionVersion(Integer processDefinitionVersion);

  String getDeploymentId();

  void setDeploymentId(String deploymentId);

  List<HistoricVariableInstanceEntity> getQueryVariables();

  void setQueryVariables(List<HistoricVariableInstanceEntity> queryVariables);

}
