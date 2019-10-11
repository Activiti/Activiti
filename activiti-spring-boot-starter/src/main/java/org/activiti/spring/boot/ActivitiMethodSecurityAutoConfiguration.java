package org.activiti.spring.boot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@ConditionalOnProperty(name = "spring.activiti.security.enabled", matchIfMissing = true)
@ConditionalOnClass(GlobalMethodSecurityConfiguration.class)
@ConditionalOnMissingBean(annotation = EnableGlobalMethodSecurity.class)
public class ActivitiMethodSecurityAutoConfiguration {

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true,
                                securedEnabled = true,
                                jsr250Enabled = true)
    public static class ActivitiMethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {
        
    }
}
