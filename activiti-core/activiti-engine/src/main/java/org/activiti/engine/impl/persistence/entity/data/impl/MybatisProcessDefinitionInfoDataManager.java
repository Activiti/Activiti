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

package org.activiti.engine.impl.persistence.entity.data.impl;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntityImpl;
import org.activiti.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.engine.impl.persistence.entity.data.ProcessDefinitionInfoDataManager;

/**

 */
public class MybatisProcessDefinitionInfoDataManager
    extends AbstractDataManager<ProcessDefinitionInfoEntity>
    implements ProcessDefinitionInfoDataManager {

    public MybatisProcessDefinitionInfoDataManager(
        ProcessEngineConfigurationImpl processEngineConfiguration
    ) {
        super(processEngineConfiguration);
    }

    @Override
    public Class<? extends ProcessDefinitionInfoEntity> getManagedEntityClass() {
        return ProcessDefinitionInfoEntityImpl.class;
    }

    @Override
    public ProcessDefinitionInfoEntity create() {
        return new ProcessDefinitionInfoEntityImpl();
    }

    @Override
    public ProcessDefinitionInfoEntity findProcessDefinitionInfoByProcessDefinitionId(
        String processDefinitionId
    ) {
        return (ProcessDefinitionInfoEntity) getDbSqlSession()
            .selectOne(
                "selectProcessDefinitionInfoByProcessDefinitionId",
                processDefinitionId
            );
    }
}
