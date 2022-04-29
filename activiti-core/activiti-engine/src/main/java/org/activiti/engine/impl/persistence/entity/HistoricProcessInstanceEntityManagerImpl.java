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

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.HistoricProcessInstanceQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.HistoricProcessInstanceDataManager;

/**
 */
public class HistoricProcessInstanceEntityManagerImpl extends AbstractEntityManager<HistoricProcessInstanceEntity> implements HistoricProcessInstanceEntityManager {

  protected HistoricProcessInstanceDataManager historicProcessInstanceDataManager;

  public HistoricProcessInstanceEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration, HistoricProcessInstanceDataManager historicProcessInstanceDataManager) {
    super(processEngineConfiguration);
    this.historicProcessInstanceDataManager = historicProcessInstanceDataManager;
  }

  @Override
  protected DataManager<HistoricProcessInstanceEntity> getDataManager() {
    return historicProcessInstanceDataManager;
  }

  @Override
  public HistoricProcessInstanceEntity create(ExecutionEntity processInstanceExecutionEntity) {
    return historicProcessInstanceDataManager.create(processInstanceExecutionEntity);
  }

  @Override
  public void deleteHistoricProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    if (getHistoryManager().isHistoryEnabled()) {
      List<String> historicProcessInstanceIds = historicProcessInstanceDataManager.findHistoricProcessInstanceIdsByProcessDefinitionId(processDefinitionId);
      for (String historicProcessInstanceId : historicProcessInstanceIds) {
        delete(historicProcessInstanceId);
      }
    }
  }

  @Override
  public void delete(String historicProcessInstanceId) {
    if (getHistoryManager().isHistoryEnabled()) {
      HistoricProcessInstanceEntity historicProcessInstance = findById(historicProcessInstanceId);

      getHistoricDetailEntityManager().deleteHistoricDetailsByProcessInstanceId(historicProcessInstanceId);
      getHistoricVariableInstanceEntityManager().deleteHistoricVariableInstanceByProcessInstanceId(historicProcessInstanceId);
      getHistoricActivityInstanceEntityManager().deleteHistoricActivityInstancesByProcessInstanceId(historicProcessInstanceId);
      getHistoricTaskInstanceEntityManager().deleteHistoricTaskInstancesByProcessInstanceId(historicProcessInstanceId);
      getHistoricIdentityLinkEntityManager().deleteHistoricIdentityLinksByProcInstance(historicProcessInstanceId);
      getCommentEntityManager().deleteCommentsByProcessInstanceId(historicProcessInstanceId);

      delete(historicProcessInstance, false);

      // Also delete any sub-processes that may be active (ACT-821)

      List<HistoricProcessInstanceEntity> selectList = historicProcessInstanceDataManager.findHistoricProcessInstancesBySuperProcessInstanceId(historicProcessInstanceId);
      for (HistoricProcessInstanceEntity child : selectList) {
        delete(child.getId()); // NEEDS to be by id, to come again through this method!
      }
    }
  }

  @Override
  public long findHistoricProcessInstanceCountByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery) {
    if (getHistoryManager().isHistoryEnabled()) {
      return historicProcessInstanceDataManager.findHistoricProcessInstanceCountByQueryCriteria(historicProcessInstanceQuery);
    }
    return 0;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> findHistoricProcessInstancesByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery) {
    if (getHistoryManager().isHistoryEnabled()) {
      return historicProcessInstanceDataManager.findHistoricProcessInstancesByQueryCriteria(historicProcessInstanceQuery);
    }
    return emptyList();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> findHistoricProcessInstancesAndVariablesByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery) {
    if (getHistoryManager().isHistoryEnabled()) {
      return historicProcessInstanceDataManager.findHistoricProcessInstancesAndVariablesByQueryCriteria(historicProcessInstanceQuery);
    }
    return emptyList();
  }

  @Override
  public List<HistoricProcessInstance> findHistoricProcessInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return historicProcessInstanceDataManager.findHistoricProcessInstancesByNativeQuery(parameterMap, firstResult, maxResults);
  }

  @Override
  public long findHistoricProcessInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return historicProcessInstanceDataManager.findHistoricProcessInstanceCountByNativeQuery(parameterMap);
  }

  public HistoricProcessInstanceDataManager getHistoricProcessInstanceDataManager() {
    return historicProcessInstanceDataManager;
  }

  public void setHistoricProcessInstanceDataManager(HistoricProcessInstanceDataManager historicProcessInstanceDataManager) {
    this.historicProcessInstanceDataManager = historicProcessInstanceDataManager;
  }

}
