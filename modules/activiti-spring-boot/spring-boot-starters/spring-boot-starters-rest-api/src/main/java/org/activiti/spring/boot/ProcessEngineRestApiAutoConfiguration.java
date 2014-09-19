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

import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.ext.servlet.ServerServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * Auto-configuration and starter for the Activiti REST APIs.
 * 
 *
 * @author Josh Long
 */
@Configuration
@AutoConfigureAfter( BasicProcessEngineAutoConfiguration.class)
public class ProcessEngineRestApiAutoConfiguration {

    @ConditionalOnExpression("${spring.activiti.restApiEnabled:true}")
    @Configuration
    @ConditionalOnClass({ActivitiRestServicesApplication.class,
            ServletRegistrationBean.class, ServerServlet.class})
    public static class RestServiceAutoConfiguration {

        @Autowired
        private ActivitiProperties activitiProperties;

        @Bean
        public ServletRegistrationBean activitiRestRegistration() {
            ServerServlet servlet = new ServerServlet();
            ServletRegistrationBean registration = new ServletRegistrationBean(servlet,
                    this.activitiProperties.getRestApiMapping());
            registration.addInitParameter("org.restlet.application", "org.activiti.rest.service.application.ActivitiRestServicesApplication");
            registration.setName(this.activitiProperties.getRestApiServletName());
            return registration;
        }
    }
}
