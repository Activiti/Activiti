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

package org.activiti.engine.impl.persistence.entity.data.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityImpl;
import org.activiti.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.engine.impl.persistence.entity.data.VariableInstanceDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.VariableByExecutionIdMatcher;


public class MybatisVariableInstanceDataManager extends AbstractDataManager<VariableInstanceEntity> implements VariableInstanceDataManager {

  protected CachedEntityMatcher<VariableInstanceEntity> variableInstanceEntity = new VariableByExecutionIdMatcher();

  public MybatisVariableInstanceDataManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  @Override
  public Class<? extends VariableInstanceEntity> getManagedEntityClass() {
    return VariableInstanceEntityImpl.class;
  }

  @Override
  public VariableInstanceEntity create() {
    VariableInstanceEntityImpl variableInstanceEntity = new VariableInstanceEntityImpl();
    variableInstanceEntity.setRevision(0); // For backwards compatibility, variables / HistoricVariableUpdate assumes revision 0 for the first time
    return variableInstanceEntity;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByTaskId(String taskId) {
    return getDbSqlSession().selectList("selectVariablesByTaskId", taskId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByTaskIds(Set<String> taskIds) {
    return getDbSqlSession().selectList("selectVariablesByTaskIds", taskIds);
  }

  @Override
  public List<VariableInstanceEntity> findVariableInstancesByExecutionId(final String executionId) {
    return getList("selectVariablesByExecutionId", executionId, variableInstanceEntity, true);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByExecutionIds(Set<String> executionIds) {
    return getDbSqlSession().selectList("selectVariablesByExecutionIds", executionIds);
  }

  @Override
  public VariableInstanceEntity findVariableInstanceByExecutionAndName(String executionId, String variableName) {
    Map<String, String> params = new HashMap<String, String>(2);
    params.put("executionId", executionId);
    params.put("name", variableName);
    return (VariableInstanceEntity) getDbSqlSession().selectOne("selectVariableInstanceByExecutionAndName", params);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByExecutionAndNames(String executionId, Collection<String> names) {
    Map<String, Object> params = new HashMap<String, Object>(2);
    params.put("executionId", executionId);
    params.put("names", names);
    return getDbSqlSession().selectList("selectVariableInstancesByExecutionAndNames", params);
  }

  @Override
  public VariableInstanceEntity findVariableInstanceByTaskAndName(String taskId, String variableName) {
    Map<String, String> params = new HashMap<String, String>(2);
    params.put("taskId", taskId);
    params.put("name", variableName);
    return (VariableInstanceEntity) getDbSqlSession().selectOne("selectVariableInstanceByTaskAndName", params);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByTaskAndNames(String taskId, Collection<String> names) {
    Map<String, Object> params = new HashMap<String, Object>(2);
    params.put("taskId", taskId);
    params.put("names", names);
    return getDbSqlSession().selectList("selectVariableInstancesByTaskAndNames", params);
  }

}
