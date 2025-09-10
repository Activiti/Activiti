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
 * Event configuration builder that handles event dispatcher initialization
 * and related event handling components.
 * This class encapsulates event handling related configuration logic.
 */
public class EventConfigurator {

    private final ProcessEngineConfigurationImpl processEngineConfiguration;

    public EventConfigurator(ProcessEngineConfigurationImpl processEngineConfiguration) {
        this.processEngineConfiguration = processEngineConfiguration;
    }

    /**
     * Initialize event-related components.
     */
    public void configure() {
        initEventDispatcher();
    }

    protected void initEventDispatcher() {
        processEngineConfiguration.initEventDispatcher();
    }
}