package org.activiti.core.common.spring.security.config;

import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.core.common.spring.security.LocalSpringSecurityManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActivitiSpringSecurityAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public SecurityManager securityManager() {
        return new LocalSpringSecurityManager();
    }

}
