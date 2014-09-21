package org.activiti.spring.boot;

import org.activiti.engine.IdentityService;
import org.activiti.spring.security.IdentityServiceUserDetailsService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Installs a Spring Security adapter for the Activiti
 * {@link org.activiti.engine.IdentityService}.
 *
 * @author Josh Long
 */
@Configuration
@AutoConfigureAfter(BasicProcessEngineAutoConfiguration.class)
@AutoConfigureBefore(org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class)
public class SecurityAutoConfiguration {


    @Bean
    UserDetailsService userDetailsService(IdentityService identityService) {
        return new IdentityServiceUserDetailsService(identityService);
    }
}