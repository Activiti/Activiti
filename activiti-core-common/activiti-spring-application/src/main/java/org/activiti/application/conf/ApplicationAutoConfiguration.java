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
package org.activiti.application.conf;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Optional;

import org.activiti.application.ApplicationDiscovery;
import org.activiti.application.ApplicationEntryDiscovery;
import org.activiti.application.ApplicationService;
import org.activiti.application.ApplicationReader;
import org.activiti.application.deployer.ApplicationDeployer;
import org.activiti.application.deployer.ApplicationEntryDeployer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
public class ApplicationAutoConfiguration {

    @Bean
    public InitializingBean deployApplications(ResourcePatternResolver resourceLoader,
                                               @Autowired(required = false) List<ApplicationEntryDiscovery> applicationEntryDiscoveries,
                                               @Autowired(required = false) List<ApplicationEntryDeployer> applicationEntryDeployers,
                                               @Value("${spring.activiti.applicationsLocation:classpath:/applications/}") String applicationsLocation) {
        return () -> new ApplicationDeployer(new ApplicationService(new ApplicationDiscovery(resourceLoader,
                                                                                             applicationsLocation),
                                                                    new ApplicationReader(
                                                                           Optional.ofNullable(applicationEntryDiscoveries)
                                                                                   .orElse(emptyList()))),
                                             Optional.ofNullable(applicationEntryDeployers)
                                                     .orElse(emptyList())).deploy();
    }
}
