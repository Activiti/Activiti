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

package org.activiti.core.common.spring.connector.autoconfigure;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.core.common.model.connector.ConnectorDefinition;
import org.activiti.core.common.spring.connector.ConnectorDefinitionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
@ComponentScan(basePackages = "org.activiti.core.common.spring.connector")
public class ConnectorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnMissingClass(value = "org.springframework.http.converter.json.Jackson2ObjectMapperBuilder")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ConnectorDefinitionService connectorDefinitionService(@Value("${activiti.connectors.dir:classpath:/connectors/}") String connectorRoot, ObjectMapper objectMapper, ResourcePatternResolver resourceLoader) {
        return new ConnectorDefinitionService(connectorRoot, objectMapper, resourceLoader);
    }

    @Bean
    public List<ConnectorDefinition> connectorDefinitions(ConnectorDefinitionService connectorDefinitionService) throws IOException {
        List<ConnectorDefinition> connectorDefinitions = connectorDefinitionService.get();
        return connectorDefinitions == null? Collections.emptyList() : connectorDefinitions;
    }
}
