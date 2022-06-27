/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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


package org.activiti.spring.process.conf;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.common.util.DateFormatterProvider;
import org.activiti.engine.RepositoryService;
import org.activiti.spring.process.CachingProcessExtensionService;
import org.activiti.spring.process.ProcessExtensionResourceReader;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.variable.VariableParsingService;
import org.activiti.spring.process.variable.VariableValidationService;
import org.activiti.spring.process.variable.types.DateVariableType;
import org.activiti.spring.process.variable.types.JavaObjectVariableType;
import org.activiti.spring.process.variable.types.JsonObjectVariableType;
import org.activiti.spring.process.variable.types.VariableType;
import org.activiti.spring.resources.DeploymentResourceLoader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class ProcessExtensionsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DeploymentResourceLoader<ProcessExtensionModel> deploymentResourceLoader() {
        return new DeploymentResourceLoader<>();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessExtensionResourceReader processExtensionResourceReader(ObjectMapper objectMapper,
                                                            Map<String, VariableType> variableTypeMap) {
        return new ProcessExtensionResourceReader(objectMapper, variableTypeMap);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessExtensionService processExtensionService(ProcessExtensionResourceReader processExtensionResourceReader,
                                                           DeploymentResourceLoader<ProcessExtensionModel> deploymentResourceLoader) {
        return new ProcessExtensionService(
                deploymentResourceLoader,
                processExtensionResourceReader);
    }

    @Bean
    InitializingBean initRepositoryServiceForProcessExtensionService(RepositoryService repositoryService,
                                                                     ProcessExtensionService processExtensionService) {
        return () -> processExtensionService.setRepositoryService(repositoryService);
    }

    @Bean
    InitializingBean initRepositoryServiceForDeploymentResourceLoader(RepositoryService repositoryService,
                                                                      DeploymentResourceLoader deploymentResourceLoader) {
        return () -> deploymentResourceLoader.setRepositoryService(repositoryService);
    }

    @Bean
    @ConditionalOnMissingBean(name = "variableTypeMap")
    public Map<String, VariableType> variableTypeMap(ObjectMapper objectMapper,
                                                     DateFormatterProvider dateFormatterProvider) {
        Map<String, VariableType> variableTypeMap = new HashMap<>();
        variableTypeMap.put("boolean", new JavaObjectVariableType(Boolean.class));
        variableTypeMap.put("string", new JavaObjectVariableType(String.class));
        variableTypeMap.put("integer", new JavaObjectVariableType(Integer.class));
        variableTypeMap.put("json", new JsonObjectVariableType(objectMapper));
        variableTypeMap.put("file", new JsonObjectVariableType(objectMapper));
        variableTypeMap.put("folder", new JsonObjectVariableType(objectMapper));
        variableTypeMap.put("date", new DateVariableType(Date.class, dateFormatterProvider));
        variableTypeMap.put("datetime", new DateVariableType(Date.class, dateFormatterProvider));
        variableTypeMap.put("array", new JsonObjectVariableType(objectMapper));
        return variableTypeMap;
    }

    @Bean
    public VariableValidationService variableValidationService(Map<String, VariableType> variableTypeMap) {
        return new VariableValidationService(variableTypeMap);
    }

    @Bean
    public VariableParsingService variableParsingService(Map<String, VariableType> variableTypeMap) {
        return new VariableParsingService(variableTypeMap);
    }

    @Bean
    @ConditionalOnMissingBean
    public CachingProcessExtensionService cachingProcessExtensionService(ProcessExtensionService processExtensionService) {
        return new CachingProcessExtensionService(processExtensionService);
    }
}
