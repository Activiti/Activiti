/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.spring.boot;

import org.activiti.engine.impl.identity.Authentication;
import org.activiti.rest.common.filter.RestAuthenticator;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.Request;
import org.restlet.data.ClientInfo;
import org.restlet.ext.servlet.ServerServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Auto-configuration and starter for the Activiti REST APIs.
 *
 * @author Joram Barrez
 * @author Josh Long
 */
@Configuration
@EnableConfigurationProperties(ActivitiProperties.class)
@AutoConfigureAfter(SecurityAutoConfiguration.class)
@ConditionalOnClass(name = {"org.activiti.rest.service.application.ActivitiRestServicesApplication", "javax.servlet.http.HttpServlet"})
public class RestApiAutoConfiguration {

    public static final String ACTIVITI_REST_REGISTRATION_ID = "activitiRegistrationId";

    @Configuration
    @ConditionalOnMissingClass(name = "org.springframework.security.core.userdetails.UserDetailsService")
    public static class DefaultRestServiceAutoConfiguration {

        @Autowired
        private ActivitiProperties activitiProperties;

        @Bean
        @ConditionalOnMissingBean(name = ACTIVITI_REST_REGISTRATION_ID)
        public ServletRegistrationBean activitiRestRegistration() {
            ServerServlet servlet = new ServerServlet();
            ServletRegistrationBean registration = new ServletRegistrationBean(servlet,
                    this.activitiProperties.getRestApiMapping());
            registration.addInitParameter("org.restlet.application", ActivitiRestServicesApplication.class.getName());
            registration.setName(this.activitiProperties.getRestApiServletName());
            return registration;
        }
    } //


    @Configuration
    @ConditionalOnClass(name = "org.springframework.security.core.userdetails.UserDetailsService")
    public static class UserDetailsServiceAwareRestServiceAutoConfiguration {
        @Autowired
        private ActivitiProperties activitiProperties;

        @Bean
        @ConditionalOnMissingBean(name = ACTIVITI_REST_REGISTRATION_ID)
        public ServletRegistrationBean activitiRestRegistration() {
            ServerServlet servlet = new ServerServlet();
            ServletRegistrationBean registration = new ServletRegistrationBean(servlet,
                    this.activitiProperties.getRestApiMapping());
            registration.addInitParameter("org.restlet.application", DeferringActivitiRestServicesApplication.class.getName());
            registration.setName(this.activitiProperties.getRestApiServletName());
            return registration;
        }


        public static class DeferringActivitiRestServicesApplication
                extends ActivitiRestServicesApplication {

            public DeferringActivitiRestServicesApplication() {
                super();
                setRestAuthenticator(
                        new UserDetailsServiceRestAuthenticator());
            }
        } // ...

        public static class UserDetailsServiceRestAuthenticator
                implements RestAuthenticator {

            @Override
            public boolean requestRequiresAuthentication(Request request) {
                SecurityContext securityContext = SecurityContextHolder.getContext();
                if (null != securityContext) {
                    org.springframework.security.core.Authentication authentication = securityContext.getAuthentication();
                    if (null == authentication) {
                        return true;
                    }
                    String authenticationName = authentication.getName();
                    org.restlet.security.User restletUser = new org.restlet.security.User(authenticationName);
                    ClientInfo clientInfo = new ClientInfo();
                    clientInfo.setUser(restletUser);
                    request.setClientInfo(clientInfo);
                    Authentication.setAuthenticatedUserId(authenticationName);
                }
                return false;
            }

            @Override
            public boolean isRequestAuthorized(Request request) {
                return true;
            }
        }

    }

}
