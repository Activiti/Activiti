/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.engine.impl.persistence.entity.integration;

import java.util.List;
import org.activiti.engine.impl.IntegrationContextQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.AbstractEntityManager;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.integration.IntegrationContextDataManager;

public class IntegrationContextManagerImpl
    extends AbstractEntityManager<IntegrationContextEntity>
    implements IntegrationContextManager {

    private final IntegrationContextDataManager dataManager;

    public IntegrationContextManagerImpl(
        ProcessEngineConfigurationImpl processEngineConfiguration,
        IntegrationContextDataManager dataManager
    ) {
        super(processEngineConfiguration);
        this.dataManager = dataManager;
    }

    @Override
    protected DataManager<IntegrationContextEntity> getDataManager() {
        return dataManager;
    }

    @Override
    public List<IntegrationContextEntity> findByQueryCriteria(IntegrationContextQueryImpl query, Page page) {
        return dataManager.findByQueryCriteria(query, page);
    }

    @Override
    public long findCountByQueryCriteria(IntegrationContextQueryImpl query) {
        return dataManager.findCountByQueryCriteria(query);
    }
}
