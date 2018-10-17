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

package org.activiti.application.conf;

import java.util.List;

import org.activiti.application.ApplicationDiscovery;
import org.activiti.application.ApplicationEntryDiscovery;
import org.activiti.application.ApplicationLoader;
import org.activiti.application.ApplicationReader;
import org.activiti.application.ProcessEntryDiscovery;
import org.activiti.application.deployer.ApplicationDeployer;
import org.activiti.application.deployer.ApplicationEntryDeployer;
import org.activiti.application.deployer.ProcessEntryDeployer;
import org.activiti.engine.RepositoryService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
public class ApplicationAutoConfiguration {

    @Bean
    public ApplicationEntryDiscovery processEntryDiscovery() {
        return new ProcessEntryDiscovery();
    }

    @Bean
    public ApplicationEntryDeployer processEntryDeployer(RepositoryService repositoryService) {
        return new ProcessEntryDeployer(repositoryService);
    }

    @Bean
    public InitializingBean deployApplications(ResourcePatternResolver resourceLoader,
                                               List<ApplicationEntryDiscovery> applicationEntryDiscoveries,
                                               List<ApplicationEntryDeployer> applicationEntryDeployers) {
        return () -> new ApplicationDeployer(new ApplicationLoader(new ApplicationDiscovery(resourceLoader),
                                                                   new ApplicationReader(applicationEntryDiscoveries)),
                                             applicationEntryDeployers).deploy();
    }
}
