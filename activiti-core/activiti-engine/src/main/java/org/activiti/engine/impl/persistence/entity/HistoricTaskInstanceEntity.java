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
import org.activiti.engine.history.HistoricTaskInstance;

/**


 */
@Internal
public interface HistoricTaskInstanceEntity extends HistoricScopeInstanceEntity, HistoricTaskInstance, Entity {

  void setExecutionId(String executionId);

  void setName(String name);

  /** Sets an optional localized name for the task. */
  void setLocalizedName(String name);

  void setDescription(String description);

  /** Sets an optional localized description for the task. */
  void setLocalizedDescription(String description);

  void setAssignee(String assignee);

  void setTaskDefinitionKey(String taskDefinitionKey);

  void setFormKey(String formKey);

  void setPriority(int priority);

  void setDueDate(Date dueDate);

  void setCategory(String category);

  void setOwner(String owner);

  void setParentTaskId(String parentTaskId);

  void setClaimTime(Date claimTime);

  void setTenantId(String tenantId);

  List<HistoricVariableInstanceEntity> getQueryVariables();

  void setQueryVariables(List<HistoricVariableInstanceEntity> queryVariables);

}
