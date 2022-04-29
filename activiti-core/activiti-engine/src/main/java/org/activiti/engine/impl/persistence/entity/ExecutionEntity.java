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

import java.util.Date;
import java.util.List;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;

@Internal
public interface ExecutionEntity extends DelegateExecution, Execution, ProcessInstance, Entity, HasRevision {

  void setBusinessKey(String businessKey);

  void setProcessDefinitionId(String processDefinitionId);

  void setProcessDefinitionKey(String processDefinitionKey);

  void setProcessDefinitionName(String processDefinitionName);

  void setProcessDefinitionVersion(Integer processDefinitionVersion);

  void setDeploymentId(String deploymentId);

  ExecutionEntity getProcessInstance();

  void setProcessInstance(ExecutionEntity processInstance);

  ExecutionEntity getParent();

  void setParent(ExecutionEntity parent);

  ExecutionEntity getSuperExecution();

  void setSuperExecution(ExecutionEntity superExecution);

  ExecutionEntity getSubProcessInstance();

  void setSubProcessInstance(ExecutionEntity subProcessInstance);

  void setRootProcessInstanceId(String rootProcessInstanceId);

  public void setParentProcessInstanceId(String parentProcessInstanceId);

  ExecutionEntity getRootProcessInstance();

  void setRootProcessInstance(ExecutionEntity rootProcessInstance);

  List<? extends ExecutionEntity> getExecutions();

  void addChildExecution(ExecutionEntity executionEntity);

  List<TaskEntity> getTasks();

  List<EventSubscriptionEntity> getEventSubscriptions();

  List<JobEntity> getJobs();

  List<TimerJobEntity> getTimerJobs();

  List<IdentityLinkEntity> getIdentityLinks();

  void setProcessInstanceId(String processInstanceId);

  void setParentId(String parentId);

  void setEnded(boolean isEnded);

  void setEventName(String eventName);

  String getDeleteReason();

  void setDeleteReason(String deleteReason);

  int getSuspensionState();

  void setSuspensionState(int suspensionState);

  boolean isEventScope();

  void setEventScope(boolean isEventScope);

  boolean isMultiInstanceRoot();

  void setMultiInstanceRoot(boolean isMultiInstanceRoot);

  void setName(String name);

  void setDescription(String description);

  void setLocalizedName(String localizedName);

  void setLocalizedDescription(String localizedDescription);

  void setTenantId(String tenantId);

  Date getLockTime();

  void setLockTime(Date lockTime);

  boolean isDeleted();

  void setDeleted(boolean isDeleted);

  void forceUpdate();

  String getStartUserId();

  void setStartUserId(String startUserId);

  Date getStartTime();

  void setStartTime(Date startTime);

}
