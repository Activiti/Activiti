/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

import org.activiti.engine.ProcessEngine;
import org.activiti.spring.boot.actuate.endpoint.ProcessEngineEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * The idea behind this module is that Spring Security could
 * talk to the org.activiti.engine.IdentityService
 * as required.
 */
@AutoConfiguration
public class EndpointAutoConfiguration {

    @Bean
    @ConditionalOnAvailableEndpoint
    public ProcessEngineEndpoint processEngineEndpoint(ProcessEngine engine) {
        return new ProcessEngineEndpoint(engine);
    }

}
