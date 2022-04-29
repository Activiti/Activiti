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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiVariableEvent;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.CountingExecutionEntity;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.VariableInstanceDataManager;
import org.activiti.engine.impl.variable.VariableType;

/**



 */
public class VariableInstanceEntityManagerImpl extends AbstractEntityManager<VariableInstanceEntity> implements VariableInstanceEntityManager {

  protected VariableInstanceDataManager variableInstanceDataManager;

  public VariableInstanceEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration, VariableInstanceDataManager variableInstanceDataManager) {
    super(processEngineConfiguration);
    this.variableInstanceDataManager = variableInstanceDataManager;
  }

  @Override
  protected DataManager<VariableInstanceEntity> getDataManager() {
    return variableInstanceDataManager;
  }

  @Override
  public VariableInstanceEntity create(String name, VariableType type, Object value) {
    VariableInstanceEntity variableInstance = create();
    variableInstance.setName(name);
    variableInstance.setType(type);
    variableInstance.setTypeName(type.getTypeName());
    variableInstance.setValue(value);
    return variableInstance;
  }

  @Override
  public void insert(VariableInstanceEntity entity, boolean fireCreateEvent) {
    super.insert(entity, fireCreateEvent);

    if (entity.getExecutionId() != null && isExecutionRelatedEntityCountEnabledGlobally()) {
      CountingExecutionEntity executionEntity = (CountingExecutionEntity) getExecutionEntityManager().findById(entity.getExecutionId());
      if (isExecutionRelatedEntityCountEnabled(executionEntity)) {
        executionEntity.setVariableCount(executionEntity.getVariableCount() + 1);
      }
    }
  }

  @Override
  public List<VariableInstanceEntity> findVariableInstancesByTaskId(String taskId) {
    return variableInstanceDataManager.findVariableInstancesByTaskId(taskId);
  }

  @Override
  public List<VariableInstanceEntity> findVariableInstancesByTaskIds(Set<String> taskIds) {
    return variableInstanceDataManager.findVariableInstancesByTaskIds(taskIds);
  }

  @Override
  public List<VariableInstanceEntity> findVariableInstancesByExecutionId(final String executionId) {
    return variableInstanceDataManager.findVariableInstancesByExecutionId(executionId);
  }

  @Override
  public List<VariableInstanceEntity> findVariableInstancesByExecutionIds(Set<String> executionIds) {
    return variableInstanceDataManager.findVariableInstancesByExecutionIds(executionIds);
  }

  @Override
  public VariableInstanceEntity findVariableInstanceByExecutionAndName(String executionId, String variableName) {
    return variableInstanceDataManager.findVariableInstanceByExecutionAndName(executionId, variableName);
  }

  @Override
  public List<VariableInstanceEntity> findVariableInstancesByExecutionAndNames(String executionId, Collection<String> names) {
    return variableInstanceDataManager.findVariableInstancesByExecutionAndNames(executionId, names);
  }

  @Override
  public VariableInstanceEntity findVariableInstanceByTaskAndName(String taskId, String variableName) {
    return variableInstanceDataManager.findVariableInstanceByTaskAndName(taskId, variableName);
  }

  @Override
  public List<VariableInstanceEntity> findVariableInstancesByTaskAndNames(String taskId, Collection<String> names) {
    return variableInstanceDataManager.findVariableInstancesByTaskAndNames(taskId, names);
  }

  @Override
  public void delete(VariableInstanceEntity entity, boolean fireDeleteEvent) {
    super.delete(entity, false);
    ByteArrayRef byteArrayRef = entity.getByteArrayRef();
    if (byteArrayRef != null) {
      byteArrayRef.delete();
    }
    entity.setDeleted(true);

    if (entity.getExecutionId() != null && isExecutionRelatedEntityCountEnabledGlobally()) {
      CountingExecutionEntity executionEntity = (CountingExecutionEntity) getExecutionEntityManager().findById(entity.getExecutionId());
      if (isExecutionRelatedEntityCountEnabled(executionEntity)) {
        executionEntity.setVariableCount(executionEntity.getVariableCount() - 1);
      }
    }

    ActivitiEventDispatcher eventDispatcher =  getEventDispatcher();
    if (fireDeleteEvent && eventDispatcher.isEnabled()) {
      eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, entity));

      eventDispatcher.dispatchEvent(createVariableDeleteEvent(entity));
    }

  }

  protected ActivitiVariableEvent createVariableDeleteEvent(VariableInstanceEntity variableInstance) {

    String processDefinitionId = null;
    if (variableInstance.getProcessInstanceId() != null) {
      ExecutionEntity executionEntity = getExecutionEntityManager().findById(variableInstance.getProcessInstanceId());
      if (executionEntity != null) {
        processDefinitionId = executionEntity.getProcessDefinitionId();
      }
    }

    Object variableValue=null;
    boolean getValue=true;

    if (variableInstance.getType().getTypeName().equals("jpa-entity")) {
        getValue=false;
    }

    if (getValue) variableValue=variableInstance.getValue();

    return ActivitiEventBuilder.createVariableEvent(ActivitiEventType.VARIABLE_DELETED,
        variableInstance.getName(),
        variableValue,
        variableInstance.getType(),
        variableInstance.getTaskId(),
        variableInstance.getExecutionId(),
        variableInstance.getProcessInstanceId(),
        processDefinitionId);
  }

  @Override
  public void deleteVariableInstanceByTask(TaskEntity task) {
    Map<String, VariableInstanceEntity> variableInstances = task.getVariableInstanceEntities();
    if (variableInstances != null) {
      for (VariableInstanceEntity variableInstance : variableInstances.values()) {
        delete(variableInstance);
      }
    }
  }

  public VariableInstanceDataManager getVariableInstanceDataManager() {
    return variableInstanceDataManager;
  }

  public void setVariableInstanceDataManager(VariableInstanceDataManager variableInstanceDataManager) {
    this.variableInstanceDataManager = variableInstanceDataManager;
  }

}
