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

package org.activiti.engine.impl.cfg.configurators;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * Service configuration builder that handles initialization of all process engine services
 * (Repository, Runtime, Task, History, Management, DynamicBpmn services).
 * This class encapsulates service-related configuration logic.
 */
public class ServiceConfigurator {

    private final ProcessEngineConfigurationImpl processEngineConfiguration;

    public ServiceConfigurator(ProcessEngineConfigurationImpl processEngineConfiguration) {
        this.processEngineConfiguration = processEngineConfiguration;
    }

    /**
     * Initialize all engine services.
     */
    public void configure() {
        initServices();
        initDataManagers();
        initEntityManagers();
        initHistoryManager();
    }

    protected void initServices() {
        processEngineConfiguration.initServices();
    }

    protected void initDataManagers() {
        processEngineConfiguration.initDataManagers();
    }

    protected void initEntityManagers() {
        processEngineConfiguration.initEntityManagers();
    }

    protected void initHistoryManager() {
        processEngineConfiguration.initHistoryManager();
    }
}