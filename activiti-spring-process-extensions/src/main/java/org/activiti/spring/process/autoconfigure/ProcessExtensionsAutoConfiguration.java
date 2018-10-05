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

package org.activiti.spring.process.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.cfg.AbstractProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.ProcessVariablesInitiator;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.Map;


@Configuration
public class ProcessExtensionsAutoConfiguration extends AbstractProcessEngineConfigurator {

    @Override
    public void configure(ProcessEngineConfigurationImpl processEngineConfiguration) {
        super.configure(processEngineConfiguration);
        processEngineConfiguration.setProcessInstanceHelper(processVariablesInitiator());
    }

    @Bean
    public ProcessVariablesInitiator processVariablesInitiator() {
        return new ProcessVariablesInitiator();
    }

    @Bean
    public Map<String, ProcessExtensionModel> processExtensionsMap(@Value("${activiti.process.extensions.dir:classpath:/processes/}")
                                                                             String processExtensionsRoot,
                                                                   @Value("${activiti.process.extensions.suffix:**-extensions.json}")
                                                                             String processExtensionsSuffix,
                                                                   ObjectMapper objectMapper,
                                                                   ResourcePatternResolver resourceLoader) throws IOException {
        ProcessExtensionService processExtensionService = new ProcessExtensionService(processExtensionsRoot, processExtensionsSuffix, objectMapper, resourceLoader);
        return processExtensionService.get();

    }
}
