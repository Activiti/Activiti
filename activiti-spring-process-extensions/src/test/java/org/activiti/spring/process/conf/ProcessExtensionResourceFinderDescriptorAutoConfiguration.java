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

package org.activiti.spring.process.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.common.util.DateFormatterProvider;
import org.activiti.engine.RepositoryService;
import org.activiti.spring.process.ProcessExtensionResourceFinderDescriptor;
import org.activiti.spring.process.ProcessExtensionResourceReader;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.ProcessVariablesInitiator;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.variable.VariableParsingService;
import org.activiti.spring.process.variable.VariableValidationService;
import org.activiti.spring.process.variable.types.DateVariableType;
import org.activiti.spring.process.variable.types.JavaObjectVariableType;
import org.activiti.spring.process.variable.types.JsonObjectVariableType;
import org.activiti.spring.process.variable.types.VariableType;
import org.activiti.spring.resources.DeploymentResourceLoader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ProcessExtensionResourceFinderDescriptorAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public ProcessExtensionResourceFinderDescriptor processExtensionResourceFinderDescriptor(
            @Value("${spring.activiti.process.extensions.dir:classpath:**/processes/}") String locationPrefix,
            @Value("${spring.activiti.process.extensions.suffix:**-extensions.json}") String locationSuffix) {
        return new ProcessExtensionResourceFinderDescriptor(true,
                locationPrefix,
                locationSuffix);
    }
}
