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
 * Core configuration builder that handles basic engine components like
 * expression manager, agenda factory, helpers, variable types, beans,
 * scripting engines, clock, business calendar manager, and ID generator.
 * This class encapsulates core engine configuration logic.
 */
public class CoreConfigurator {

    private final ProcessEngineConfigurationImpl processEngineConfiguration;

    public CoreConfigurator(ProcessEngineConfigurationImpl processEngineConfiguration) {
        this.processEngineConfiguration = processEngineConfiguration;
    }

    /**
     * Initialize core engine components.
     */
    public void configure() {
        initHistoryLevel();
        initExpressionManager();
        initAgendaFactory();
        initHelpers();
        initVariableTypes();
        initBeans();
        initScriptingEngines();
        initClock();
        initBusinessCalendarManager();
        initIdGenerator();
    }

    protected void initHistoryLevel() {
        processEngineConfiguration.initHistoryLevel();
    }

    protected void initExpressionManager() {
        processEngineConfiguration.initExpressionManager();
    }

    protected void initAgendaFactory() {
        processEngineConfiguration.initAgendaFactory();
    }

    protected void initHelpers() {
        processEngineConfiguration.initHelpers();
    }

    protected void initVariableTypes() {
        processEngineConfiguration.initVariableTypes();
    }

    protected void initBeans() {
        processEngineConfiguration.initBeans();
    }

    protected void initScriptingEngines() {
        processEngineConfiguration.initScriptingEngines();
    }

    protected void initClock() {
        processEngineConfiguration.initClock();
    }

    protected void initBusinessCalendarManager() {
        processEngineConfiguration.initBusinessCalendarManager();
    }

    protected void initIdGenerator() {
        processEngineConfiguration.initIdGenerator();
    }
}