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
