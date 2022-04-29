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

package org.activiti.engine.impl.persistence.entity.data;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;


public interface VariableInstanceDataManager extends DataManager<VariableInstanceEntity> {

  List<VariableInstanceEntity> findVariableInstancesByTaskId(String taskId);

  List<VariableInstanceEntity> findVariableInstancesByTaskIds(Set<String> taskIds);

  List<VariableInstanceEntity> findVariableInstancesByExecutionId(String executionId);

  List<VariableInstanceEntity> findVariableInstancesByExecutionIds(Set<String> executionIds);

  VariableInstanceEntity findVariableInstanceByExecutionAndName(String executionId, String variableName);

  List<VariableInstanceEntity> findVariableInstancesByExecutionAndNames(String executionId, Collection<String> names);

  VariableInstanceEntity findVariableInstanceByTaskAndName(String taskId, String variableName);

  List<VariableInstanceEntity> findVariableInstancesByTaskAndNames(String taskId, Collection<String> names);

}
