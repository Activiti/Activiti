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
import org.activiti.spring.process.variable.VariableParsingService;
import org.activiti.spring.process.variable.VariableValidationService;
import org.activiti.spring.process.variable.types.JsonObjectVariableType;
import org.activiti.spring.process.variable.types.DateVariableType;
import org.activiti.spring.process.variable.types.VariableType;
import org.activiti.spring.process.variable.types.JavaObjectVariableType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.ResourcePatternResolver;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
                                                                   ResourcePatternResolver resourceLoader,
                                                                   Map<String, VariableType> variableTypeMap) throws IOException {
        ProcessExtensionService processExtensionService = new ProcessExtensionService(processExtensionsRoot, processExtensionsSuffix, objectMapper, resourceLoader, variableTypeMap);
        return processExtensionService.get();

    }

    @Bean
    public Map<String, VariableType> variableTypeMap(ObjectMapper objectMapper){

        Map<String, VariableType> variableTypeMap = new HashMap<>();
        variableTypeMap.put("boolean", new JavaObjectVariableType(Boolean.class));
        variableTypeMap.put("string", new JavaObjectVariableType(String.class));
        variableTypeMap.put("integer", new JavaObjectVariableType(Integer.class));
        variableTypeMap.put("json", new JsonObjectVariableType(objectMapper));
        variableTypeMap.put("date", new DateVariableType(Date.class, new SimpleDateFormat(DateVariableType.defaultFormat)));
        return variableTypeMap;
    }

    @Bean
    public VariableValidationService variableValidationService(Map<String, VariableType> variableTypeMap){
        return new VariableValidationService(variableTypeMap);
    }

    @Bean
    public VariableParsingService variableParsingService(Map<String, VariableType> variableTypeMap){
        return new VariableParsingService(variableTypeMap);
    }
}
