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
 * BPMN configuration builder that handles BPMN parser, deployers, behavior factory,
 * listener factory, process definition caches, and knowledge base cache initialization.
 * This class encapsulates BPMN processing related configuration logic.
 */
public class BpmnConfigurator {

    private final ProcessEngineConfigurationImpl processEngineConfiguration;

    public BpmnConfigurator(ProcessEngineConfigurationImpl processEngineConfiguration) {
        this.processEngineConfiguration = processEngineConfiguration;
    }

    /**
     * Initialize BPMN-related components.
     */
    public void configure() {
        initBehaviorFactory();
        initListenerFactory();
        initBpmnParser();
        initProcessDefinitionCache();
        initProcessDefinitionInfoCache();
        initKnowledgeBaseCache();
        initDeployers();
    }

    protected void initBehaviorFactory() {
        processEngineConfiguration.initBehaviorFactory();
    }

    protected void initListenerFactory() {
        processEngineConfiguration.initListenerFactory();
    }

    protected void initBpmnParser() {
        processEngineConfiguration.initBpmnParser();
    }

    protected void initProcessDefinitionCache() {
        processEngineConfiguration.initProcessDefinitionCache();
    }

    protected void initProcessDefinitionInfoCache() {
        processEngineConfiguration.initProcessDefinitionInfoCache();
    }

    protected void initKnowledgeBaseCache() {
        processEngineConfiguration.initKnowledgeBaseCache();
    }

    protected void initDeployers() {
        processEngineConfiguration.initDeployers();
    }
}