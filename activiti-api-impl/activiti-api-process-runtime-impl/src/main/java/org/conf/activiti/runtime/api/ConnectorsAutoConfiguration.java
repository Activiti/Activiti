/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.conf.activiti.runtime.api;

import org.activiti.core.common.spring.connector.ConnectorDefinitionService;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti.core.common.model.connector.ConnectorDefinition;
import org.activiti.runtime.api.connector.ConnectorActionDefinitionFinder;
import org.activiti.runtime.api.connector.DefaultServiceTaskBehavior;
import org.activiti.runtime.api.connector.IntegrationContextBuilder;
import org.activiti.runtime.api.connector.VariablesMatchHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
public class ConnectorsAutoConfiguration {

    @Autowired
    private ConnectorDefinitionService connectorDefinitionService;

    @Bean
    public List<ConnectorDefinition> connectorDefinitions() throws IOException {
        return connectorDefinitionService.get();
    }

    @Bean
    public IntegrationContextBuilder integrationContextBuilder(VariablesMatchHelper variablesMatchHelper) {
        return new IntegrationContextBuilder(variablesMatchHelper);
    }

    @Bean(name = DefaultActivityBehaviorFactory.DEFAULT_SERVICE_TASK_BEAN_NAME)
    @ConditionalOnMissingBean(name = DefaultActivityBehaviorFactory.DEFAULT_SERVICE_TASK_BEAN_NAME)
    public DefaultServiceTaskBehavior defaultServiceTaskBehavior(ApplicationContext applicationContext,
                                                                 IntegrationContextBuilder integrationContextBuilder, ConnectorActionDefinitionFinder connectorActionDefinitionFinder, VariablesMatchHelper variablesMatchHelper) throws IOException {
        return new DefaultServiceTaskBehavior(applicationContext,
                integrationContextBuilder, connectorActionDefinitionFinder, variablesMatchHelper);
    }

    @Bean
    public ConnectorActionDefinitionFinder connectorActionDefinitionFinder() throws IOException {
        return new ConnectorActionDefinitionFinder(connectorDefinitions());
    }

    @Bean
    public VariablesMatchHelper variablesMatchHelper() {
        return new VariablesMatchHelper();
    }
}
