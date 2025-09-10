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
 * Session configuration builder that handles session factories,
 * JPA, event handlers, delegate interceptor, and process validator initialization.
 * This class encapsulates session and integration related configuration logic.
 */
public class SessionConfigurator {

    private final ProcessEngineConfigurationImpl processEngineConfiguration;

    public SessionConfigurator(ProcessEngineConfigurationImpl processEngineConfiguration) {
        this.processEngineConfiguration = processEngineConfiguration;
    }

    /**
     * Initialize session and integration components.
     */
    public void configure() {
        initSessionFactories();
        initJpa();
        initDelegateInterceptor();
        initEventHandlers();
        initProcessValidator();
        initDatabaseEventLogging();
    }

    protected void initSessionFactories() {
        processEngineConfiguration.initSessionFactories();
    }

    protected void initJpa() {
        processEngineConfiguration.initJpa();
    }

    protected void initDelegateInterceptor() {
        processEngineConfiguration.initDelegateInterceptor();
    }

    protected void initEventHandlers() {
        processEngineConfiguration.initEventHandlers();
    }

    protected void initProcessValidator() {
        processEngineConfiguration.initProcessValidator();
    }

    protected void initDatabaseEventLogging() {
        processEngineConfiguration.initDatabaseEventLogging();
    }
}