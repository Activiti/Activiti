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
package org.activiti.core.common.spring.security.config;

import org.activiti.api.runtime.shared.security.PrincipalGroupsProvider;
import org.activiti.api.runtime.shared.security.PrincipalIdentityProvider;
import org.activiti.api.runtime.shared.security.PrincipalRolesProvider;
import org.activiti.api.runtime.shared.security.SecurityContextPrincipalProvider;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.core.common.spring.security.AuthenticationPrincipalGroupsProvider;
import org.activiti.core.common.spring.security.AuthenticationPrincipalIdentityProvider;
import org.activiti.core.common.spring.security.AuthenticationPrincipalRolesProvider;
import org.activiti.core.common.spring.security.GrantedAuthoritiesGroupsMapper;
import org.activiti.core.common.spring.security.GrantedAuthoritiesResolver;
import org.activiti.core.common.spring.security.GrantedAuthoritiesRolesMapper;
import org.activiti.core.common.spring.security.LocalSpringSecurityContextPrincipalProvider;
import org.activiti.core.common.spring.security.LocalSpringSecurityManager;
import org.activiti.core.common.spring.security.SimpleGrantedAuthoritiesGroupsMapper;
import org.activiti.core.common.spring.security.SimpleGrantedAuthoritiesResolver;
import org.activiti.core.common.spring.security.SimpleGrantedAuthoritiesRolesMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActivitiSpringSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GrantedAuthoritiesResolver grantedAuthoritiesResolver() {
        return new SimpleGrantedAuthoritiesResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public GrantedAuthoritiesGroupsMapper grantedAuthoritiesGroupsMapper() {
        return new SimpleGrantedAuthoritiesGroupsMapper();
    };

    @Bean
    @ConditionalOnMissingBean
    public GrantedAuthoritiesRolesMapper grantedAuthoritiesRolesMapper() {
        return new SimpleGrantedAuthoritiesRolesMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityContextPrincipalProvider securityContextPrincipalProvider() {
        return new LocalSpringSecurityContextPrincipalProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public PrincipalIdentityProvider principalIdentityProvider() {
        return new AuthenticationPrincipalIdentityProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public PrincipalGroupsProvider principalGroupsProvider(GrantedAuthoritiesResolver grantedAuthoritiesResolver,
                                                           GrantedAuthoritiesGroupsMapper grantedAuthoritiesGroupsMapper) {
        return new AuthenticationPrincipalGroupsProvider(grantedAuthoritiesResolver,
                                                         grantedAuthoritiesGroupsMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public PrincipalRolesProvider principalRolessProvider(GrantedAuthoritiesResolver grantedAuthoritiesResolver,
                                                          GrantedAuthoritiesRolesMapper grantedAuthoritiesRolesMapper) {
        return new AuthenticationPrincipalRolesProvider(grantedAuthoritiesResolver,
                                                        grantedAuthoritiesRolesMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityManager securityManager(SecurityContextPrincipalProvider securityContextPrincipalProvider,
                                           PrincipalIdentityProvider principalIdentityProvider,
                                           PrincipalGroupsProvider principalGroupsProvider,
                                           PrincipalRolesProvider principalRolessProvider) {
        return new LocalSpringSecurityManager(securityContextPrincipalProvider,
                                              principalIdentityProvider,
                                              principalGroupsProvider,
                                              principalRolessProvider);
    }

}
