package org.activiti.cloud.services.identity.basic.config;

import org.activiti.cloud.services.identity.basic.BasicAuthorizationLookup;
import org.activiti.cloud.services.identity.basic.BasicIdentityLookup;
import org.activiti.runtime.api.identity.IdentityLookup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@ConditionalOnProperty(name = "activiti.cloud.services.identity.basic.enabled", matchIfMissing = true)
public class ActivitiBasicIdentityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BasicIdentityLookup.class)
    public BasicIdentityLookup basicIdentityLookup(InMemoryUserDetailsManager userDetailsService) {
        return new BasicIdentityLookup(userDetailsService);
    }

    @Bean
    @ConditionalOnMissingBean(BasicAuthorizationLookup.class)
    public BasicAuthorizationLookup basicUserRoleLookupProxy(IdentityLookup identityLookup) {
        return new BasicAuthorizationLookup(identityLookup);
    }
}
