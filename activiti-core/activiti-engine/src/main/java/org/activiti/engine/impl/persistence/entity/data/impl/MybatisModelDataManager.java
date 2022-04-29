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

import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.ModelQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.ModelEntity;
import org.activiti.engine.impl.persistence.entity.ModelEntityImpl;
import org.activiti.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.engine.impl.persistence.entity.data.ModelDataManager;
import org.activiti.engine.repository.Model;


public class MybatisModelDataManager extends AbstractDataManager<ModelEntity> implements ModelDataManager {

  public MybatisModelDataManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  @Override
  public Class<? extends ModelEntity> getManagedEntityClass() {
    return ModelEntityImpl.class;
  }

  @Override
  public ModelEntity create() {
    return new ModelEntityImpl();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Model> findModelsByQueryCriteria(ModelQueryImpl query, Page page) {
    return getDbSqlSession().selectList("selectModelsByQueryCriteria", query, page);
  }

  @Override
  public long findModelCountByQueryCriteria(ModelQueryImpl query) {
    return (Long) getDbSqlSession().selectOne("selectModelCountByQueryCriteria", query);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Model> findModelsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectModelByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @Override
  public long findModelCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectModelCountByNativeQuery", parameterMap);
  }

}
