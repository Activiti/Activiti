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

package org.activiti.engine.impl.persistence.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.HistoricVariableInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.HistoricVariableInstanceDataManager;

/**


 */
public class HistoricVariableInstanceEntityManagerImpl extends AbstractEntityManager<HistoricVariableInstanceEntity> implements HistoricVariableInstanceEntityManager {
  
  protected HistoricVariableInstanceDataManager historicVariableInstanceDataManager;
  
  public HistoricVariableInstanceEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration, HistoricVariableInstanceDataManager historicVariableInstanceDataManager) {
    super(processEngineConfiguration);
    this.historicVariableInstanceDataManager = historicVariableInstanceDataManager;
  }
  
  @Override
  protected DataManager<HistoricVariableInstanceEntity> getDataManager() {
    return historicVariableInstanceDataManager;
  }
  
  @Override
  public HistoricVariableInstanceEntity copyAndInsert(VariableInstanceEntity variableInstance) {
    HistoricVariableInstanceEntity historicVariableInstance = historicVariableInstanceDataManager.create();
    historicVariableInstance.setId(variableInstance.getId());
    historicVariableInstance.setProcessInstanceId(variableInstance.getProcessInstanceId());
    historicVariableInstance.setExecutionId(variableInstance.getExecutionId());
    historicVariableInstance.setTaskId(variableInstance.getTaskId());
    historicVariableInstance.setRevision(variableInstance.getRevision());
    historicVariableInstance.setName(variableInstance.getName());
    historicVariableInstance.setVariableType(variableInstance.getType());

    copyVariableValue(historicVariableInstance, variableInstance);

    Date time = getClock().getCurrentTime();
    historicVariableInstance.setCreateTime(time);
    historicVariableInstance.setLastUpdatedTime(time);

    insert(historicVariableInstance);

    return historicVariableInstance;
  }
  
  @Override
  public void copyVariableValue(HistoricVariableInstanceEntity historicVariableInstance, VariableInstanceEntity variableInstance) {
    historicVariableInstance.setTextValue(variableInstance.getTextValue());
    historicVariableInstance.setTextValue2(variableInstance.getTextValue2());
    historicVariableInstance.setDoubleValue(variableInstance.getDoubleValue());
    historicVariableInstance.setLongValue(variableInstance.getLongValue());

    historicVariableInstance.setVariableType(variableInstance.getType());
    if (variableInstance.getByteArrayRef() != null) {
      historicVariableInstance.setBytes(variableInstance.getBytes());
    }

    historicVariableInstance.setLastUpdatedTime(getClock().getCurrentTime());
  }
  
  @Override
  public void delete(HistoricVariableInstanceEntity entity, boolean fireDeleteEvent) {
    super.delete(entity, fireDeleteEvent);
    
    if (entity.getByteArrayRef() != null) {
      entity.getByteArrayRef().delete();
    }
  }

  @Override
  public void deleteHistoricVariableInstanceByProcessInstanceId(final String historicProcessInstanceId) {
    if (getHistoryManager().isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricVariableInstanceEntity> historicProcessVariables = historicVariableInstanceDataManager.findHistoricVariableInstancesByProcessInstanceId(historicProcessInstanceId);
      for (HistoricVariableInstanceEntity historicProcessVariable : historicProcessVariables) {
        delete(historicProcessVariable);
      }
    }
  }

  @Override
  public long findHistoricVariableInstanceCountByQueryCriteria(HistoricVariableInstanceQueryImpl historicProcessVariableQuery) {
    return historicVariableInstanceDataManager.findHistoricVariableInstanceCountByQueryCriteria(historicProcessVariableQuery);
  }

  @Override
  public List<HistoricVariableInstance> findHistoricVariableInstancesByQueryCriteria(HistoricVariableInstanceQueryImpl historicProcessVariableQuery, Page page) {
    return historicVariableInstanceDataManager.findHistoricVariableInstancesByQueryCriteria(historicProcessVariableQuery, page);
  }

  @Override
  public HistoricVariableInstanceEntity findHistoricVariableInstanceByVariableInstanceId(String variableInstanceId) {
    return historicVariableInstanceDataManager.findHistoricVariableInstanceByVariableInstanceId(variableInstanceId);
  }

  @Override
  public void deleteHistoricVariableInstancesByTaskId(String taskId) {
    if (getHistoryManager().isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricVariableInstanceEntity> historicProcessVariables = historicVariableInstanceDataManager.findHistoricVariableInstancesByTaskId(taskId);
      for (HistoricVariableInstanceEntity historicProcessVariable : historicProcessVariables) {
        delete((HistoricVariableInstanceEntity) historicProcessVariable);
      }
    }
  }

  @Override
  public List<HistoricVariableInstance> findHistoricVariableInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return historicVariableInstanceDataManager.findHistoricVariableInstancesByNativeQuery(parameterMap, firstResult, maxResults);
  }

  @Override
  public long findHistoricVariableInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return historicVariableInstanceDataManager.findHistoricVariableInstanceCountByNativeQuery(parameterMap);
  }


  public HistoricVariableInstanceDataManager getHistoricVariableInstanceDataManager() {
    return historicVariableInstanceDataManager;
  }


  public void setHistoricVariableInstanceDataManager(HistoricVariableInstanceDataManager historicVariableInstanceDataManager) {
    this.historicVariableInstanceDataManager = historicVariableInstanceDataManager;
  }
  
}
