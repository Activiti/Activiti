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
package org.activiti.core.common.spring.security.policies.config;

import org.activiti.api.process.model.payloads.GetProcessDefinitionsPayload;
import org.activiti.api.process.model.payloads.GetProcessInstancesPayload;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManager;
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManagerImpl;
import org.activiti.core.common.spring.security.policies.SecurityPoliciesProcessDefinitionRestrictionApplier;
import org.activiti.core.common.spring.security.policies.SecurityPoliciesProcessInstanceRestrictionApplier;
import org.activiti.core.common.spring.security.policies.SecurityPoliciesRestrictionApplier;
import org.activiti.core.common.spring.security.policies.conf.SecurityPoliciesProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SecurityPoliciesProperties.class)
public class ActivitiSpringSecurityPoliciesAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProcessSecurityPoliciesManager processSecurityPoliciesManager(SecurityManager securityManager,
                                                                         SecurityPoliciesProperties securityPoliciesProperties,
                                                                         SecurityPoliciesRestrictionApplier<GetProcessDefinitionsPayload> processDefinitionRestrictionApplier,
                                                                         SecurityPoliciesRestrictionApplier<GetProcessInstancesPayload> processInstanceRestrictionApplier) {
        return new ProcessSecurityPoliciesManagerImpl(securityManager,
                                                      securityPoliciesProperties,
                                                      processDefinitionRestrictionApplier,
                                                      processInstanceRestrictionApplier);
    }

    @Bean
    @ConditionalOnMissingBean(name = "processInstanceRestrictionApplier")
    public SecurityPoliciesRestrictionApplier<GetProcessInstancesPayload> processInstanceRestrictionApplier() {
        return new SecurityPoliciesProcessInstanceRestrictionApplier();
    }

    @Bean
    @ConditionalOnMissingBean(name = "processDefinitionRestrictionApplier")
    public SecurityPoliciesRestrictionApplier<GetProcessDefinitionsPayload> processDefinitionRestrictionApplier () {
        return new SecurityPoliciesProcessDefinitionRestrictionApplier();
    }

}
