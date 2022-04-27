/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.ModelQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.entity.ModelEntity;
import org.activiti.engine.repository.Model;


public interface ModelDataManager extends DataManager<ModelEntity> {

  List<Model> findModelsByQueryCriteria(ModelQueryImpl query, Page page);

  long findModelCountByQueryCriteria(ModelQueryImpl query);

  List<Model> findModelsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults);

  long findModelCountByNativeQuery(Map<String, Object> parameterMap);


}
