/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.runtime.api;

import static org.mockito.Mockito.mock;

import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManager;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProcessRuntimeTestApp {

    public static void main(String[] args) {
        SpringApplication.run(ProcessRuntimeTestApp.class);
    }

    @Bean
    public RepositoryService repositoryService() {
        return mock(RepositoryService.class);
    }

    @Bean
    public RuntimeService runtimeService() {
        return mock(RuntimeService.class);
    }
    
    @Bean
    public ManagementService managementService() {
        return mock(ManagementService.class);
    }

    @Bean
    public UserGroupManager userGroupManager() {
        return mock(UserGroupManager.class);
    }

    @Bean
    public SecurityManager securityManager() {
        return mock(SecurityManager.class);
    }

    @Bean
    public ProcessSecurityPoliciesManager securityPolicyManager() {
        return mock(ProcessSecurityPoliciesManager.class);
    }
}
