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

import java.util.List;

import org.activiti.core.common.model.connector.ConnectorDefinition;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti.runtime.api.connector.ConnectorActionDefinitionFinder;
import org.activiti.runtime.api.connector.DefaultServiceTaskBehavior;
import org.activiti.runtime.api.connector.InboundVariableValueProvider;
import org.activiti.runtime.api.connector.InboundVariablesProvider;
import org.activiti.runtime.api.connector.IntegrationContextBuilder;
import org.activiti.runtime.api.connector.OutboundVariablesProvider;
import org.activiti.runtime.api.impl.VariablesMappingProvider;
import org.activiti.spring.process.ProcessExtensionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConnectorsAutoConfiguration {

    @Bean
    public IntegrationContextBuilder integrationContextBuilder(ProcessExtensionService processExtensionService) {
        return new IntegrationContextBuilder(new InboundVariablesProvider(new InboundVariableValueProvider(processExtensionService)));
    }

    @Bean(name = DefaultActivityBehaviorFactory.DEFAULT_SERVICE_TASK_BEAN_NAME)
    @ConditionalOnMissingBean(name = DefaultActivityBehaviorFactory.DEFAULT_SERVICE_TASK_BEAN_NAME)
    public DefaultServiceTaskBehavior defaultServiceTaskBehavior(ApplicationContext applicationContext,
                                                                 ConnectorActionDefinitionFinder connectorActionDefinitionFinder,
                                                                 IntegrationContextBuilder integrationContextBuilder,
                                                                 OutboundVariablesProvider outboundVariablesProvider) {
        return new DefaultServiceTaskBehavior(applicationContext,
                                              integrationContextBuilder,
                                              connectorActionDefinitionFinder,
                                              outboundVariablesProvider);
    }

    @Bean
    public OutboundVariablesProvider outboundVariablesProvider(ConnectorActionDefinitionFinder connectorActionDefinitionFinder,
                                                               ProcessExtensionService processExtensionService) {
        return new OutboundVariablesProvider(processExtensionService,
                                             connectorActionDefinitionFinder);
    }

    @Bean
    public ConnectorActionDefinitionFinder connectorActionDefinitionFinder(List<ConnectorDefinition> connectorDefinitions) {
        return new ConnectorActionDefinitionFinder(connectorDefinitions);
    }


    @Bean
    public VariablesMappingProvider variablesMappingProvider(ProcessExtensionService processExtensionService){
        return new VariablesMappingProvider(processExtensionService);
    }
}
