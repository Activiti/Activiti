/* Licensed under the Apache License, Version 2.0 (the "License");
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

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.spring.boot.actuate.endpoint.ProcessEngineEndpoint;
import org.activiti.spring.boot.actuate.endpoint.ProcessEngineMvcEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The idea behind this module is that Spring Security could
 * talk to the {@link org.activiti.engine.IdentityService}
 * as required.
 *
 * @author Josh Long
 */
@Configuration
@ConditionalOnClass (name = "org.springframework.boot.actuate.endpoint.AbstractEndpoint")
public class EndpointAutoConfiguration {

    @Bean
    public ProcessEngineEndpoint processEngineEndpoint(ProcessEngine engine) {
        return new ProcessEngineEndpoint(engine);
    }

    @Bean
    public ProcessEngineMvcEndpoint processEngineMvcEndpoint(
            ProcessEngineEndpoint engineEndpoint, RepositoryService repositoryService) {
        return new ProcessEngineMvcEndpoint(engineEndpoint, repositoryService);
    }
}
