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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.RepositoryService;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ProcessExtensionsAutoConfiguration {

    @Bean
    public ProcessVariablesInitiator processVariablesInitiator(VariableParsingService variableParsingService,
                                                               VariableValidationService variableValidationService) {
        return new ProcessVariablesInitiator(variableParsingService,
                                             variableValidationService);
    }

    @Bean
    InitializingBean setProcessExtensionForProcessVariablesInitiator(ProcessVariablesInitiator processVariablesInitiator,
                                                                     ProcessExtensionService processExtensionService){
        return () -> processVariablesInitiator.setProcessExtensionService(processExtensionService);
    }

    @Bean
    public DeploymentResourceLoader<ProcessExtensionModel> processExtensionLoader(RepositoryService repositoryService){
        return new DeploymentResourceLoader<>(repositoryService);
    }

    @Bean
    public ProcessExtensionResourceReader processExtensionResourceReader(ObjectMapper objectMapper,
                                                                         Map<String, VariableType> variableTypeMap) {
        return new ProcessExtensionResourceReader(objectMapper, variableTypeMap);
    }

    @Bean
    public ProcessExtensionService processExtensionService(ProcessExtensionResourceReader processExtensionResourceReader,
                                                           RepositoryService repositoryService) {
        return new ProcessExtensionService(
                new DeploymentResourceLoader<>(repositoryService),
                processExtensionResourceReader,
                repositoryService);
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
