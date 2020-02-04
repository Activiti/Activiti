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

package org.activiti.runtime.api.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti.engine.impl.delegate.invocation.DefaultDelegateInterceptor;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.runtime.api.connector.DefaultServiceTaskBehavior;
import org.activiti.runtime.api.connector.IntegrationContextBuilder;
import org.activiti.runtime.api.impl.ExpressionResolver;
import org.activiti.runtime.api.impl.VariablesMappingProvider;
import org.activiti.spring.process.ProcessExtensionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConnectorsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExpressionManager expressionManager() {
        return new ExpressionManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExpressionResolver expressionResolver(ExpressionManager expressionManager, ObjectMapper objectMapper) {
        return new ExpressionResolver(expressionManager, objectMapper, new DefaultDelegateInterceptor());
    }

    @Bean
    @ConditionalOnMissingBean
    public IntegrationContextBuilder integrationContextBuilder(VariablesMappingProvider variablesMappingProvider) {
        return new IntegrationContextBuilder(variablesMappingProvider);
    }

    @Bean(name = DefaultActivityBehaviorFactory.DEFAULT_SERVICE_TASK_BEAN_NAME)
    @ConditionalOnMissingBean(name = DefaultActivityBehaviorFactory.DEFAULT_SERVICE_TASK_BEAN_NAME)
    public DefaultServiceTaskBehavior defaultServiceTaskBehavior(ApplicationContext applicationContext,
                                                                 IntegrationContextBuilder integrationContextBuilder,
                                                                 VariablesMappingProvider outboundVariablesProvider) {
        return new DefaultServiceTaskBehavior(applicationContext,
                                              integrationContextBuilder,
                                              outboundVariablesProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public VariablesMappingProvider variablesMappingProvider(ProcessExtensionService processExtensionService,
                                                             ExpressionResolver expressionResolver) {
        return new VariablesMappingProvider(processExtensionService,
                                            expressionResolver);
    }
}
