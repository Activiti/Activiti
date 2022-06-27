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
