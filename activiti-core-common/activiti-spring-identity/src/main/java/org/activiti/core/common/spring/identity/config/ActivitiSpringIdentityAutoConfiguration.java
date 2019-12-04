package org.activiti.core.common.spring.identity.config;

import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.core.common.spring.identity.ActivitiUserGroupManagerImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class ActivitiSpringIdentityAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public UserGroupManager userGroupManager(UserDetailsService userDetailsService) {
        return new ActivitiUserGroupManagerImpl(userDetailsService);
    }

}
