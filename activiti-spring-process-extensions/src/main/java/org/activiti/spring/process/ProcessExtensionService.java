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

package org.activiti.spring.process;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.types.VariableType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ProcessExtensionService {

    private final ObjectMapper objectMapper;
    private Map<String, VariableType> variableTypeMap;
    private final RepositoryService repositoryService;
    
    //processDefinitionId => deploymentId, processDefinitionKey
    private Map<String, Entry<String, String>> procDefIdToDeployIdDefKey = new HashMap<>();
    private static final ProcessExtensionModel EMPTY_EXTENSIONS = new ProcessExtensionModel();
    

    //deploymentId => processDefinitionKey, ProcessExtensionModel
    private Map<String, Map<String,ProcessExtensionModel>> processExtensionModelDeploymentMap = new HashMap<>();
    
    public ProcessExtensionService(ObjectMapper objectMapper, 
                                   Map<String, VariableType> variableTypeMap,
                                   RepositoryService repositoryService) {

        this.objectMapper = objectMapper;
        this.variableTypeMap = variableTypeMap;
        this.repositoryService = repositoryService;
    }

    //Do this once for each deplymentId
    private Map<String,ProcessExtensionModel> getProcessExtensionsForDeploymentId(String deploymentId) {
        
        Map<String,ProcessExtensionModel> processExtensionModelMap = processExtensionModelDeploymentMap.get(deploymentId);
        
        if (processExtensionModelMap != null) {
            return processExtensionModelMap;
        }
           
        processExtensionModelMap = new HashMap<>();
        
        List <String> resourceNames = repositoryService.getDeploymentResourceNames(deploymentId);
        
        if (resourceNames != null && !resourceNames.isEmpty()) {
            
            List <String> processExtensionNames = resourceNames.stream()
                                                    .filter(s -> s.contains("-extensions.json"))
                                                    .collect(Collectors.toList());
            
            if (processExtensionNames != null && !processExtensionNames.isEmpty()) {
                for (String name:processExtensionNames) {
                    try {
                        ProcessExtensionModel processExtensionModel = read(repositoryService.getResourceAsStream(deploymentId, name));
                        if (processExtensionModel != null) {
                            processExtensionModelMap.put(processExtensionModel.getId(), processExtensionModel); 
                        }
                    } catch (Exception e)  {
                        
                    }
                }
            }
            
        }
        processExtensionModelDeploymentMap.put(deploymentId, processExtensionModelMap);
        return processExtensionModelMap;
    }
    
    private ProcessExtensionModel getExtensionsForProcessDefinitionKey(String processDefinitionKey) {
        ProcessExtensionModel processExtensionModel = null;
        
        if (processExtensionModelDeploymentMap != null) {
            for (Entry<String, Map<String, ProcessExtensionModel>> mapEntry : processExtensionModelDeploymentMap.entrySet()) {
                Map<String, ProcessExtensionModel> processExtensionModelMap = mapEntry.getValue();
                
                processExtensionModel = processExtensionModelMap.get(processDefinitionKey);
                if (processExtensionModel != null) return processExtensionModel;
            }
        }
        return processExtensionModel;
    }
    
    private ProcessExtensionModel getExtensionsFor(String deploymentId, String processDefinitionKey) {
        ProcessExtensionModel processExtensionModel = null;
        
        if (deploymentId != null) {
            Map<String,ProcessExtensionModel> processExtensionModelMap = getProcessExtensionsForDeploymentId(deploymentId);
            if (processExtensionModelMap != null)  {
                processExtensionModel = processExtensionModelMap.get(processDefinitionKey);
            } 
        } else {
            processExtensionModel = getExtensionsForProcessDefinitionKey(processDefinitionKey);
        }
     
        return processExtensionModel;
    }
    
    private ProcessExtensionModel read(InputStream inputStream) throws IOException {
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        ProcessExtensionModel mappedModel = objectMapper.readValue(inputStream,
                ProcessExtensionModel.class);
        return convertJsonVariables(mappedModel);
    }

    /**
     * Json variables need to be represented as JsonNode for engine to handle as Json
     * Do this for any var marked as json or whose type is not recognised from the extension file
     */
    private ProcessExtensionModel convertJsonVariables(ProcessExtensionModel processExtensionModel){
        if( processExtensionModel!=null && processExtensionModel.getExtensions()!=null
                && processExtensionModel.getExtensions().getProperties()!=null ){

            for(VariableDefinition variableDefinition:processExtensionModel.getExtensions().getProperties().values()){
                if(!variableTypeMap.keySet().contains(variableDefinition.getType())||variableDefinition.getType().equals("json")){
                    variableDefinition.setValue(objectMapper.convertValue(variableDefinition.getValue(), JsonNode.class));
                }
            }
        }
        return processExtensionModel;
    }

    public void cache(ProcessDefinition processDefinition) {
        if (procDefIdToDeployIdDefKey.get(processDefinition.getId()) == null) {
            procDefIdToDeployIdDefKey.put(processDefinition.getId(), 
                                          new AbstractMap.SimpleEntry<String, String>(
                                                            processDefinition.getDeploymentId(),
                                                            processDefinition.getKey()));
        }
    }

    public boolean hasExtensionsFor(ProcessDefinition processDefinition) {
        ProcessExtensionModel processExtensionModel = getExtensionsFor(processDefinition.getDeploymentId(),processDefinition.getKey());
        if (processExtensionModel != null) {
            cache(processDefinition);
        }
        return (processExtensionModel != null);
    }

    public boolean hasExtensionsFor(String processDefinitionId, String processDefinitionKey) {
        Entry<String, String> deployIdDefKey = procDefIdToDeployIdDefKey.get(processDefinitionId);
        ProcessExtensionModel processExtensionModel = null;
        if (deployIdDefKey != null) {
            processExtensionModel = getExtensionsFor(deployIdDefKey.getKey(), deployIdDefKey.getValue());
            
        } else {
            processExtensionModel = getExtensionsFor(null, processDefinitionKey);
        }
            
        return (processExtensionModel != null);
    }

    public ProcessExtensionModel getExtensionsFor(ProcessDefinition processDefinition) {
        ProcessExtensionModel processExtensionModel = getExtensionsFor(processDefinition.getDeploymentId(),processDefinition.getKey());
        
        if (processExtensionModel != null) {
            cache(processDefinition);
        }
        
        return processExtensionModel != null? processExtensionModel : EMPTY_EXTENSIONS;
    }

    public ProcessExtensionModel getExtensionsForId(String processDefinitionId) {
        Entry<String, String> deployIdDefKey = procDefIdToDeployIdDefKey.get(processDefinitionId);
        ProcessExtensionModel processExtensionModel = null;
        if (deployIdDefKey != null) { 
            processExtensionModel = getExtensionsFor(deployIdDefKey.getKey(), deployIdDefKey.getValue());
            
        }
        return processExtensionModel != null? processExtensionModel : EMPTY_EXTENSIONS;
    }

}
