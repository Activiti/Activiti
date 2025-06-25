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
package org.activiti.spring.resolver;

import jakarta.el.ELResolver;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
public class ELResolverAutoConfiguration {

    @ConditionalOnProperty(name = "spring.activiti.env-var-el-resolver.enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public EnvironmentVariableELResolver environmentVariableELResolver() {
        return new EnvironmentVariableELResolver();
    }

    @Bean
    public ProcessEngineConfigurationConfigurer environmentVariablesELResolverConfigurer(List<ELResolver> customELResolvers) {
        return processEngineConfiguration -> {
            if(customELResolvers!=null) {
                processEngineConfiguration.setCustomELResolvers(customELResolvers);
            }
        };
    }

}
