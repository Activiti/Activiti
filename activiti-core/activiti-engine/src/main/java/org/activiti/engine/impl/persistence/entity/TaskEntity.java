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
import java.util.Map;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.task.Task;

/**
 */
@Internal
public interface TaskEntity extends VariableScope, Task, DelegateTask, Entity, HasRevision {

  ExecutionEntity getExecution();

  void setExecutionId(String executionId);

  void setExecution(ExecutionEntity execution);

  List<IdentityLinkEntity> getIdentityLinks();

  void setExecutionVariables(Map<String, Object> parameters);

  void setCreateTime(Date createTime);

  void setProcessDefinitionId(String processDefinitionId);

  void setEventName(String eventName);

  void setCurrentActivitiListener(ActivitiListener currentActivitiListener);

  ExecutionEntity getProcessInstance();

  void setProcessInstanceId(String processInstanceId);

  int getSuspensionState();

  void setSuspensionState(int suspensionState);

  void setTaskDefinitionKey(String taskDefinitionKey);

  Map<String, VariableInstanceEntity> getVariableInstanceEntities();

  void forceUpdate();

  boolean isDeleted();

  void setDeleted(boolean isDeleted);

  Date getClaimTime();

  void setClaimTime(Date claimTime);

  boolean isCanceled();

  void setCanceled(boolean isCanceled);

  void setBusinessKey(String businessKey);
}
