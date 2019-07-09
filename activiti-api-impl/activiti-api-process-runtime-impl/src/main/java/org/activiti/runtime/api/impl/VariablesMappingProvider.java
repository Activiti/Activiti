/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime.api.impl;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.Mapping;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.ProcessVariablesMapping;

public class VariablesMappingProvider {

    private ProcessExtensionService processExtensionService;

    public VariablesMappingProvider(ProcessExtensionService processExtensionService) {
        this.processExtensionService = processExtensionService;
    }

    public Object calculateMappedValue(Mapping inputMapping,
                                       DelegateExecution execution,
                                       ProcessExtensionModel extensions) {
    
        if (inputMapping != null) {
            if (Mapping.SourceMappingType.VALUE.equals(inputMapping.getType())) {
                return inputMapping.getValue();
            } else {
                if (Mapping.SourceMappingType.VARIABLE.equals(inputMapping.getType())) {
                    String name = inputMapping.getValue().toString();
                   //This is extra check
                    org.activiti.spring.process.model.VariableDefinition processVariableDefinition = extensions.getExtensions().getPropertyByName(name);
                    if (processVariableDefinition != null) {
                        return execution.getVariable(processVariableDefinition.getName());
                    }
                    //We may agree that modeller will check everything
                    //In this case we may use simply:
                    //return execution.getVariable(name);
                    
                } 
                //We have to check if this is needed?
                else {
                    if (Mapping.SourceMappingType.STATIC_VALUE.equals(inputMapping.getType())) {
                        return inputMapping.getValue();         
                    } 
                }
                
            }
        }
        return null;
    }

    public Map<String, Object> calculateInputVariables(DelegateExecution execution) {

        Map<String, Object> inboundVariables = null;
        boolean copyAllVariables = true;
        ProcessExtensionModel extensions = processExtensionService.getExtensionsForId(execution.getProcessDefinitionId());
        if (extensions.getExtensions().isTaskElementPresentInMappingSection(execution.getCurrentActivityId())) {
            ProcessVariablesMapping processVariablesMapping = extensions.getExtensions().getMappingForFlowElement(execution.getCurrentActivityId());
            extensions.getExtensions().isTaskElementPresentInMappingSection(execution.getCurrentActivityId());
            Map<String, Mapping> inputMappings = processVariablesMapping.getInputs();
            if (!inputMappings.isEmpty()) {
                inboundVariables = new HashMap<>();
                for (Map.Entry<String, Mapping> mapping : inputMappings.entrySet()) {
                    Object value = calculateMappedValue(mapping.getValue(),
                            execution,
                            extensions);
                    if (value != null) {
                        inboundVariables.put(mapping.getKey(),
                                value);
                    }
                }
            }
            else {
                copyAllVariables = false;
            }
        }
        //Nothing found - put all process variables if Task is empty if task is not empty
        if (inboundVariables == null) {
            if (copyAllVariables) {
                inboundVariables = new HashMap<>(execution.getVariables());
            } else {
                inboundVariables = new HashMap<>();
            }
        }

        return inboundVariables;
    }
    
    public Object calculateOutPutMappedValue(Mapping mapping,
                                             Map<String, Object> activitiCompleteVariables) {
    
        if (mapping != null) {
            if (Mapping.SourceMappingType.VALUE.equals(mapping.getType())) {
                return mapping.getValue();
            } else {
                if (Mapping.SourceMappingType.VARIABLE.equals(mapping.getType())) {
                    String name = mapping.getValue().toString();
                    
                    return activitiCompleteVariables != null ?
                           activitiCompleteVariables.get(name) :
                           null;     
                }
            }
            
        }
        return null;
    }
    
    public Map<String, Object> calculateOutPutVariables(boolean defaultCopyAllVariables,
                                                        String processDefinitionId, 
                                                        String activityId,
                                                        Map<String, Object> activitiCompleteVariables) {
        
        Map<String, Object> outboundVariables = new HashMap<>();    
        
        if (activitiCompleteVariables != null && !activitiCompleteVariables.isEmpty()) {
            ProcessExtensionModel extensions = processExtensionService.getExtensionsForId(processDefinitionId);      
            if (extensions != null) {
                ProcessVariablesMapping processVariablesMapping = extensions.getExtensions().getMappingForFlowElement(activityId);
                if (processVariablesMapping != null) {
                    Map<String, Mapping> outputMappings = processVariablesMapping.getOutputs();
                   
                    if (!outputMappings.isEmpty()) {
                          
                        for (Map.Entry<String, Mapping> mapping : outputMappings.entrySet()) {
                            
                            String name = mapping.getKey();
                            
                            //Check that we have this process variables in extensions
                            //TO DO: can we create a process variable if it is not defined in extension file?
                            org.activiti.spring.process.model.VariableDefinition processVariableDefinition = extensions.getExtensions().getPropertyByName(name);
                            if (processVariableDefinition != null) {
                                outboundVariables.put(name, calculateOutPutMappedValue(mapping.getValue(),
                                                                                       activitiCompleteVariables));                              
                            }                                                               
                        }     
                    }
                    
                }            
            }
           
            //Nothing found - put all completeVariables
            if (outboundVariables.isEmpty() && defaultCopyAllVariables) {
                outboundVariables = new HashMap<>(activitiCompleteVariables);      
            }
        }
        
        return outboundVariables;
    }
}
